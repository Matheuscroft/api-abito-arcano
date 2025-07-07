package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.response.CheckTarefaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.CompletedTaskResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.CompletedTaskWithScoreDTO;
import com.matheus.api_abito_arcano.dtos.response.UncheckTarefaResponseDTO;
import com.matheus.api_abito_arcano.models.CompletedTask;
import com.matheus.api_abito_arcano.models.Day;
import com.matheus.api_abito_arcano.models.Tarefa;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.repositories.CompletedTaskRepository;
import com.matheus.api_abito_arcano.repositories.DayRepository;
import com.matheus.api_abito_arcano.repositories.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CompletedTaskService {

    private static final Logger log = LoggerFactory.getLogger(CompletedTaskService.class);
    @Autowired
    private CompletedTaskRepository completedTaskRepository;
    @Autowired
    private TarefaRepository tarefaRepository;
    @Autowired
    private DayRepository dayRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ScoreService scoreService;

    @Transactional
    public CheckTarefaResponseDTO checkTarefa(UUID tarefaId, UUID dayId) {
        log.info("[checkTarefa] Iniciando check da tarefa {} para o dia {}", tarefaId, dayId);

        User user = userService.getUsuarioAutenticado();
        log.info("[checkTarefa] Usuário autenticado: {}", user.getId());

        Tarefa tarefa = tarefaRepository.findById(tarefaId).orElseThrow();
        Day day = dayRepository.findById(dayId).orElseThrow();

        log.info("[checkTarefa] Tarefa: {}, Dia: {}", tarefa.getTitle(), day.getDate());

        CompletedTask completed = new CompletedTask();
        completed.setTarefa(tarefa);
        completed.setDay(day);
        completed.setUser(user);
        completed.setCompletedAt(LocalDateTime.now());
        completed.setScore(tarefa.getScore());

        log.info("[checkTarefa] Salvando CompletedTask...");
        CompletedTask saved = completedTaskRepository.save(completed);
        log.info("[checkTarefa] CompletedTask salvo com ID {}", saved.getId());

        day.getCompletedTasks().add(saved);
        dayRepository.save(day);

        var score = scoreService.processarPontuacao(tarefaId, dayId, true, user);
        log.info("[checkTarefa] Score atualizado: {}", score.score());

        return new CheckTarefaResponseDTO(
                new CompletedTaskResponseDTO(saved),
                score
        );
    }

    @Transactional
    public UncheckTarefaResponseDTO uncheckTarefa(UUID tarefaId, UUID dayId) {
        User user = userService.getUsuarioAutenticado();

        CompletedTask completed = completedTaskRepository
                .findByTarefa_IdAndDay_Id(tarefaId, dayId)
                .stream()
                .findFirst()
                .orElseThrow();

        UUID completedId = completed.getId();
        LocalDateTime completedAt = completed.getCompletedAt();

        CompletedTaskResponseDTO dto = new CompletedTaskResponseDTO(completed);
        completedTaskRepository.delete(completed);

        var score = scoreService.processarPontuacao(tarefaId, dayId, false, user);

        return new UncheckTarefaResponseDTO(dto, score);
    }

}
