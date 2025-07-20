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

    @Autowired
    private CompletedTaskService completedTaskService;

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

    @Transactional
    public Tarefa updateTask(UUID id, TarefaDTO tarefaDTO, UUID dayId) {
        logger.info("[updateTask] Iniciando atualização da tarefa com id: {}", id);

        User user = userService.getUsuarioAutenticado();
        logger.info("[updateTask] Usuário autenticado: {}", user.getId());

        Optional<Tarefa> tarefaOptional = tarefaRepository.findByIdAndUserId(id, user.getId());
        if (tarefaOptional.isEmpty()) {
            logger.warn("[updateTask] Tarefa com ID {} não encontrada para o usuário {}", id, user.getId());
            return null;
        }

        Tarefa oldTask = tarefaOptional.get();
        logger.info("[updateTask] Tarefa encontrada: {} - '{}'", oldTask.getId(), oldTask.getTitle());

        Day day = dayRepository.findByIdAndUserId(dayId, user.getId())
                .orElseThrow(() -> new RuntimeException("Dia com ID " + dayId + " não encontrado."));
        logger.info("[updateTask] Dia base encontrado: {} (Data: {})", day.getId(), day.getDate());

        LocalDate cutoffDate = day.getDate();

        // ✅ CLONAGEM OBRIGATÓRIA CASO JÁ TENHA SIDO CONCLUÍDA EM ALGUM DIA FUTURO
        if (dayService.tarefaFoiConcluidaEmDiasFuturos(oldTask, cutoffDate, user.getId())) {
            logger.info("[updateTask] A tarefa {} já foi completada em dias futuros. Forçando clonagem mesmo sem alterações significativas.", oldTask.getId());

            Tarefa tempNewTask = prepararTarefaTemporaria(tarefaDTO);
            return cloneTask(oldTask, tempNewTask, cutoffDate, user, true);
        }

        // 🧪 Preparar a nova tarefa para comparação
        Tarefa tempNewTask = prepararTarefaTemporaria(tarefaDTO);

        // 🔄 Clonagem por alteração significativa
        if (shouldCloneTask(oldTask, tempNewTask)) {
            logger.info("[updateTask] Diferenças significativas detectadas. Clonando tarefa...");
            return cloneTask(oldTask, tempNewTask, cutoffDate, user, false);
        }

        // 🛠️ Atualização direta da tarefa original
        logger.info("[updateTask] Nenhuma diferença crítica. Atualizando tarefa existente.");

        List<Integer> oldDaysOfWeek = new ArrayList<>(oldTask.getDaysOfTheWeek());
        logger.info("[updateTask] Dias antigos da semana: {}", oldDaysOfWeek);

        BeanUtils.copyProperties(tarefaDTO, oldTask, "areaId", "subareaId");
        oldTask.setArea(tempNewTask.getArea());
        oldTask.setSubarea(tempNewTask.getSubarea());

        oldTask = tarefaRepository.save(oldTask);
        logger.info("[updateTask] Tarefa atualizada com sucesso. ID: {}", oldTask.getId());

        logger.info("[updateTask] Iniciando atualização dos dias futuros a partir de {}", cutoffDate);
        dayService.updateTaskFromDateAndFutureDays(oldTask, oldDaysOfWeek, cutoffDate);
        logger.info("[updateTask] Atualização da tarefa concluída. Retornando tarefa atualizada.");

        return oldTask;
    }




    public boolean deleteTask(UUID tarefaId, UUID dayId) {
        User user = userService.getUsuarioAutenticado();
        logger.info("[deleteTask] Iniciando exclusão da tarefa {} para o usuário {}", tarefaId, user.getId());

        Optional<Tarefa> tarefaOptional = tarefaRepository.findByIdAndUserId(tarefaId, user.getId());

        if (tarefaOptional.isEmpty()) {
            logger.warn("[deleteTask] Tarefa {} não encontrada para o usuário {}. Tentando deletar apenas completedTasks", tarefaId, user.getId());

            completedTaskService.deleteCompletedTasksFromDate(tarefaId, dayRepository.findDateById(dayId));
            return true;
        }

        Tarefa tarefa = tarefaOptional.get();

        Day fromDay = dayRepository.findByIdAndUserId(dayId, user.getId())
                .orElseThrow(() -> new DayNotFoundException(dayId));
        LocalDate fromDate = fromDay.getDate();

        logger.info("[deleteTask] fromDay id={}, date={}", fromDay.getId(), fromDate);

        dayService.deleteTaskFromDaysAndFromDate(user.getId(), fromDate, tarefa);
        completedTaskService.deleteCompletedTasksFromDate(tarefa.getId(), fromDate);

        logger.info("[deleteTask] Concluído: deleteTaskFromDaysAndFromDate e deleteCompletedTasksFromDate");

        boolean stillReferenced = dayRepository.existsByUserIdAndTarefasPrevistasContaining(user.getId(), tarefa);
        boolean hasRemainingCompletions = completedTaskRepository.existsByTarefa_Id(tarefa.getId());

        logger.info("[deleteTask] stillReferenced: {}", stillReferenced);
        logger.info("[deleteTask] hasRemainingCompletions: {}", hasRemainingCompletions);

        if (!stillReferenced && !hasRemainingCompletions) {
            tarefaRepository.delete(tarefa);
            logger.info("[deleteTask] Tarefa {} deletada completamente do banco", tarefa.getId());
        } else {
            logger.info("[deleteTask] Tarefa {} ainda referenciada em algum lugar, não foi deletada", tarefaId);
        }

        return true;
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

    private Tarefa cloneTask(Tarefa oldTask, Tarefa newTaskData, LocalDate cutoffDate, User user, boolean isCloningDueToFutureCompletion) {
        logger.info("[cloneTask] Clonando tarefa '{}' para o dia {}", oldTask.getTitle(), cutoffDate);

        // 1. Marcar a versão antiga como não sendo mais a mais recente
        oldTask.setLatestVersion(false);
        tarefaRepository.save(oldTask);

        // 2. Obter a raiz da árvore
        Tarefa originalTask = (oldTask.getOriginalTask() != null) ? oldTask.getOriginalTask() : oldTask;

        // 4. Remover versões futuras
        List<Tarefa> versoesRemovidas = dayService.removerVersoesFuturasDosDias(originalTask, cutoffDate, user.getId());

        // 5. Checar se a original será excluída
        boolean originalVaiSerExcluida = false;
        if (!isCloningDueToFutureCompletion) {
            originalVaiSerExcluida = dayService.seraExcluidaSeNaoUsadaAntes(originalTask, cutoffDate, user.getId());
            logger.info("[cloneTask] originalVaiSerExcluida? '{}'", originalVaiSerExcluida);
        } else {
            logger.info("[cloneTask] Clonagem forçada por completed future — não será avaliada exclusão da original.");
        }

        // 6. Criar nova instância da tarefa
        Tarefa newTask = new Tarefa();
        newTask.setTitle(newTaskData.getTitle());
        newTask.setScore(newTaskData.getScore());
        newTask.setDaysOfTheWeek(new ArrayList<>(newTaskData.getDaysOfTheWeek()));
        newTask.setType(newTaskData.getType());
        newTask.setUser(user);
        newTask.setCreatedAt(LocalDateTime.now());
        newTask.setLatestVersion(true);

        if (newTaskData.getArea() != null) {
            newTask.setArea(areaService.getAreaOrThrow(newTaskData.getArea().getId()));
        }

        if (newTaskData.getSubarea() != null) {
            newTask.setSubarea(subareaService.getSubareaOrThrow(newTaskData.getSubarea().getId(), newTask.getArea()));
        }

        if (!originalVaiSerExcluida) {
            newTask.setOriginalTask(originalTask);
        }

        // 7. Salvar nova tarefa
        newTask = tarefaRepository.save(newTask);

        // 8. Adicionar a nova tarefa aos dias futuros
        dayService.addTaskToDayAndFutureDays(newTask, user.getId(), cutoffDate);

        // 9. Limpar tarefas antigas se necessário
        if (!isCloningDueToFutureCompletion) {
            dayService.excluirVersoesSeNaoUsadasAntes(versoesRemovidas, cutoffDate, user.getId());
        }

        logger.info("[cloneTask] Tarefa clonada com sucesso. Nova tarefa ID: {}", newTask.getId());
        return newTask;
    }

    private Tarefa prepararTarefaTemporaria(TarefaDTO tarefaDTO) {
        Tarefa temp = new Tarefa();
        BeanUtils.copyProperties(tarefaDTO, temp, "areaId", "subareaId");

        if (tarefaDTO.areaId() != null) {
            Area area = areaService.getAreaOrThrow(tarefaDTO.areaId());
            temp.setArea(area);
        }

        if (tarefaDTO.subareaId() != null) {
            Subarea sub = subareaService.getSubareaOrThrow(tarefaDTO.subareaId(), temp.getArea());
            temp.setSubarea(sub);
        }

        return temp;
    }


}
