package com.matheus.api_abito_arcano.services;


import com.matheus.api_abito_arcano.dtos.TarefaDTO;
import com.matheus.api_abito_arcano.dtos.response.TarefaResponseDTO;
import com.matheus.api_abito_arcano.exceptions.*;
import com.matheus.api_abito_arcano.models.*;
import com.matheus.api_abito_arcano.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TarefaService {

    private static final Logger logger = LoggerFactory.getLogger(TarefaService.class);

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DayService dayService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private SubareaService subareaService;

    @Autowired
    private CompletedTaskRepository completedTaskRepository;

    public Tarefa createTask(TarefaDTO tarefaDto, UUID dayId) {

        User user = userService.getUsuarioAutenticado();

        logger.info("Iniciando criação de tarefa: {}", tarefaDto.title());

        Area area = areaService.getValidOrDefaultArea(tarefaDto.areaId(), user.getId());
        Subarea subarea = subareaService.getValidSubarea(tarefaDto.subareaId(), area, user.getId());

        Tarefa tarefa = new Tarefa();
        BeanUtils.copyProperties(tarefaDto, tarefa);
        tarefa.setArea(area);
        tarefa.setUser(user);
        tarefa.setCreatedAt(LocalDateTime.now());
        tarefa.setOriginalTask(null);
        tarefa.setLatestVersion(true);

        if (subarea != null) {
            logger.info("Subárea encontrada: {} - Área associada: {}", subarea.getName(), subarea.getArea());
            tarefa.setSubarea(subarea);
        }

        logger.info("Salvando tarefa: {}", tarefa.getTitle());

        tarefa = tarefaRepository.save(tarefa);

        Day day = dayRepository.findByIdAndUserId(dayId, user.getId())
                .orElseThrow(() -> new RuntimeException("Dia com ID " + dayId + " não encontrado."));

        dayService.addTaskToDayAndFutureDays(tarefa, user.getId(), day.getDate());

        return tarefa;
    }

    public List<TarefaResponseDTO> getTasks() {
        User user = userService.getUsuarioAutenticado();
        List<Tarefa> tarefas = tarefaRepository.findByUserId(user.getId());
        return tarefas.stream().map(TarefaResponseDTO::new).toList();
    }

    public TarefaResponseDTO getTaskById(UUID id) {
        User user = userService.getUsuarioAutenticado();

        Tarefa tarefa = tarefaRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new TarefaNotFoundException(id));

        return new TarefaResponseDTO(tarefa);
    }


    public Tarefa updateTask(UUID id, TarefaDTO tarefaDTO, UUID dayId) {

        User user = userService.getUsuarioAutenticado();

        Optional<Tarefa> tarefaOptional = tarefaRepository.findByIdAndUserId(id, user.getId());
        if (tarefaOptional.isPresent()) {
            Tarefa oldTask = tarefaOptional.get();

            Tarefa tempNewTask = new Tarefa();
            BeanUtils.copyProperties(tarefaDTO, tempNewTask, "areaId", "subareaId");

            if (tarefaDTO.areaId() == null) {
                tempNewTask.setArea(null);
            } else {
                Area area = areaService.getAreaOrThrow(tarefaDTO.areaId());
                tempNewTask.setArea(area);
            }

            if (tarefaDTO.subareaId() == null) {
                tempNewTask.setSubarea(null);
            } else {
                Subarea subarea = subareaService.getSubareaOrThrow(tarefaDTO.subareaId(), tempNewTask.getArea());
                tempNewTask.setSubarea(subarea);
            }

            if (shouldCloneTask(oldTask, tempNewTask)) {
                return cloneTask(oldTask, tempNewTask, dayId, user);
            }

            List<Integer> oldDaysOfWeek = new ArrayList<>(oldTask.getDaysOfTheWeek());

            BeanUtils.copyProperties(tarefaDTO, oldTask, "areaId", "subareaId");

            oldTask.setArea(tempNewTask.getArea());
            oldTask.setSubarea(tempNewTask.getSubarea());

            oldTask = tarefaRepository.save(oldTask);

            Day day = dayRepository.findByIdAndUserId(dayId, user.getId())
                    .orElseThrow(() -> new RuntimeException("Dia com ID " + dayId + " não encontrado."));

            dayService.updateTaskFromDateAndFutureDays(oldTask, oldDaysOfWeek, day.getDate());

            return oldTask;
        }
        return null;
    }



    public boolean deleteTask(UUID tarefaId, UUID dayId) {
        User user = userService.getUsuarioAutenticado();
        logger.info("Deletando tarefa de id {} ", tarefaId);

        Optional<Tarefa> tarefaOptional = tarefaRepository.findByIdAndUserId(tarefaId, user.getId());

        if (tarefaOptional.isPresent()) {
            logger.info("Tarefa optional is present");
            Tarefa tarefa = tarefaOptional.get();

            Day fromDay = dayRepository.findByIdAndUserId(dayId, user.getId())
                    .orElseThrow(() -> new DayNotFoundException(dayId));

            List<CompletedTask> futureCompletions = completedTaskRepository
                    .findAllByTarefa_IdAndDay_DateGreaterThanEqual(tarefa.getId(), fromDay.getDate());

            if (!futureCompletions.isEmpty()) {
                logger.info("Deletando {} completedTasks futuras da tarefa {}", futureCompletions.size(), tarefa.getId());
                completedTaskRepository.deleteAll(futureCompletions);
            }



            dayService.deleteTaskFromDayAndFutureDays(user.getId(), dayId, tarefa);

            boolean stillReferenced = dayRepository
                    .existsByUserIdAndTarefasPrevistasContaining(user.getId(), tarefa);

            boolean hasRemainingCompletions = completedTaskRepository
                    .existsByTarefa_Id(tarefa.getId());

            if (!stillReferenced && !hasRemainingCompletions) {
                tarefaRepository.delete(tarefa);
                logger.info("Tarefa {} deletada completamente", tarefa.getId());
            }
            else {
                logger.info("Tarefa {} ainda presente em algum dia, não foi deletada do banco", tarefaId);
            }

            return true;
        }

        return false;
    }

    @Transactional
    public void deleteAllTasks() {
        User user = userService.getUsuarioAutenticado();
        List<Day> dias = dayRepository.findAllByUserId(user.getId());

        for (Day dia : dias) {
            if (!dia.getTarefasPrevistas().isEmpty()) {
                dia.getTarefasPrevistas().clear();
                logger.info("Limpando tarefas do dia {}", dia.getId());
            }
        }

        dayRepository.saveAll(dias);

        List<Tarefa> tarefas = tarefaRepository.findAllByUserId(user.getId());
        tarefaRepository.deleteAll(tarefas);

        logger.info("Todas as tarefas do usuário {} foram deletadas com sucesso", user.getId());
    }



    private boolean shouldCloneTask(Tarefa oldTask, Tarefa newTask) {
        boolean scoreChanged = !Objects.equals(oldTask.getScore(), newTask.getScore());

        UUID oldAreaId = oldTask.getArea() != null ? oldTask.getArea().getId() : null;
        UUID newAreaId = newTask.getArea() != null ? newTask.getArea().getId() : null;
        boolean areaChanged = !Objects.equals(oldAreaId, newAreaId);

        UUID oldSubareaId = oldTask.getSubarea() != null ? oldTask.getSubarea().getId() : null;
        UUID newSubareaId = newTask.getSubarea() != null ? newTask.getSubarea().getId() : null;
        boolean subareaChanged = !Objects.equals(oldSubareaId, newSubareaId);

        boolean titleChanged = hasSignificantTitleChange(oldTask.getTitle(), newTask.getTitle());

        logger.info("Verificando necessidade de clonar tarefa:");
        logger.info("- Score alterado? {} ({} → {})", scoreChanged, oldTask.getScore(), newTask.getScore());
        logger.info("- Área alterada? {} ({} → {})", areaChanged, oldAreaId, newAreaId);
        logger.info("- Subárea alterada? {} ({} → {})", subareaChanged, oldSubareaId, newSubareaId);
        logger.info("- Título significativamente alterado? {} ({} → {})", titleChanged, oldTask.getTitle(), newTask.getTitle());

        return scoreChanged || areaChanged || subareaChanged || titleChanged;
    }


    private boolean hasSignificantTitleChange(String oldTitle, String newTitle) {
        if (oldTitle == null || newTitle == null) return true;

        String n1 = oldTitle.trim().toLowerCase();
        String n2 = newTitle.trim().toLowerCase();

        if (n1.equals(n2)) {
            logger.info("Títulos iguais após normalização: '{}'", n1);
            return false;
        }

        if (Math.abs(n1.length() - n2.length()) > 10) {
            logger.info("Diferença de tamanho significativa entre títulos: {} → {}", n1.length(), n2.length());
            return true;
        }

        Set<String> oldWords = new HashSet<>(Arrays.asList(n1.split("\\s+")));
        Set<String> newWords = new HashSet<>(Arrays.asList(n2.split("\\s+")));

        int intersection = 0;
        for (String word : newWords) {
            if (oldWords.contains(word)) intersection++;
        }

        int total = Math.max(oldWords.size(), newWords.size());
        double similarity = (double) intersection / total;

        logger.info("Comparando palavras dos títulos:");
        logger.info("- Palavras antigas: {}", oldWords);
        logger.info("- Palavras novas:   {}", newWords);
        logger.info("- Similaridade: {}", similarity);

        return similarity < 0.5;
    }

    private Tarefa cloneTask(Tarefa oldTask, Tarefa newTaskData, UUID dayId, User user) {

        logger.info("Clonando tarefa '{}' para o dia {}", oldTask.getTitle(), dayId);

        oldTask.setLatestVersion(false);
        tarefaRepository.save(oldTask);

        Tarefa originalTask = (oldTask.getOriginalTask() != null) ? oldTask.getOriginalTask() : oldTask;

        LocalDate cutoffDate = dayRepository.findByIdAndUserId(dayId, user.getId())
                .orElseThrow(() -> new RuntimeException("Dia com ID " + dayId + " não encontrado."))
                .getDate();

        List<Tarefa> versoesRemovidas = dayService.removerVersoesFuturasDosDias(originalTask, cutoffDate, user.getId());

        boolean originalVaiSerExcluida = dayService.seraExcluidaSeNaoUsadaAntes(originalTask, cutoffDate, user.getId());
        logger.info("originalVaiSerExcluida? - '{}'", originalVaiSerExcluida);

        Tarefa newTask = new Tarefa();
        BeanUtils.copyProperties(newTaskData, newTask);
        newTask.setId(null);
        newTask.setUser(user);
        newTask.setCreatedAt(LocalDateTime.now());
        newTask.setLatestVersion(true);

        if (originalVaiSerExcluida) {
            newTask.setOriginalTask(null);
            logger.info("A tarefa original será excluída. Nova tarefa será a raiz da árvore.");
        } else {
            newTask.setOriginalTask(originalTask);
        }

        newTask = tarefaRepository.save(newTask);

        dayService.addTaskToDayAndFutureDays(newTask, user.getId(), cutoffDate);
        dayService.excluirVersoesSeNaoUsadasAntes(versoesRemovidas, cutoffDate, user.getId());


        return newTask;
    }


}
