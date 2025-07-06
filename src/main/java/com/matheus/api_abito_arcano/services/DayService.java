package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.response.CompletedTaskResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.DayDetailResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.DayResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.TarefaResponseDTO;
import com.matheus.api_abito_arcano.exceptions.DayNotFoundException;
import com.matheus.api_abito_arcano.mappers.DayMapper;
import com.matheus.api_abito_arcano.models.Day;
import com.matheus.api_abito_arcano.models.Tarefa;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.repositories.DayRepository;
import com.matheus.api_abito_arcano.repositories.TarefaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DayService {

    private static final Logger logger = LoggerFactory.getLogger(TarefaService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private TarefaRepository tarefaRepository;

    public void createInitialDaysForUser(User user) {

        boolean alreadyHasDays = dayRepository.existsByUserId(user.getId());
        if (alreadyHasDays) return;

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth nextMonth = currentMonth.plusMonths(1);

        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = nextMonth.atEndOfMonth();

        List<Day> days = new ArrayList<>();

        while (!startDate.isAfter(endDate)) {
            Day day = new Day();
            day.setDate(startDate);
            day.setUser(user);

            day.setCurrent(startDate.isEqual(today));

            int diaDaSemana = startDate.getDayOfWeek().getValue();
            diaDaSemana = (diaDaSemana % 7) + 1;
            day.setDayOfWeek(diaDaSemana);

            days.add(day);
            startDate = startDate.plusDays(1);
        }

        dayRepository.saveAll(days);
    }

    public List<DayResponseDTO> buscarPorUsuarioAutenticado() {
        User user = userService.getUsuarioAutenticado();
        List<Day> dias = dayRepository.findByUserId(user.getId());

        return dias.stream()
                .map(DayMapper::toDTO)
                .sorted(Comparator.comparing(DayResponseDTO::date)) // opcional
                .toList();
    }

    public void verificarDiaAtualEDados(User user) {
        //LocalDate hoje = LocalDate.now();
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        Day diaAtual = dayRepository.findByUserIdAndDate(user.getId(), hoje);
        if (diaAtual != null && !diaAtual.isCurrent()) {
            List<Day> diasDoUsuario = dayRepository.findByUserId(user.getId());
            for (Day d : diasDoUsuario) {
                d.setCurrent(false);
            }
            dayRepository.saveAll(diasDoUsuario);

            diaAtual.setCurrent(true);
            dayRepository.save(diaAtual);
        }

        Day ultimoDia = dayRepository.findTopByUserIdOrderByDateDesc(user.getId());
        if (ultimoDia != null) {
            YearMonth ultimoMes = YearMonth.from(ultimoDia.getDate());
            YearMonth proximoMesEsperado = YearMonth.from(hoje).plusMonths(1);

            if (ultimoMes.isBefore(proximoMesEsperado)) {
                LocalDate dataInicio = ultimoMes.plusMonths(1).atDay(1);
                LocalDate dataFim = proximoMesEsperado.atEndOfMonth();

                List<Day> novosDias = new ArrayList<>();
                while (!dataInicio.isAfter(dataFim)) {
                    Day novoDia = new Day();
                    novoDia.setUser(user);
                    novoDia.setDate(dataInicio);
                    int diaSemana = dataInicio.getDayOfWeek().getValue() % 7 + 1;
                    novoDia.setDayOfWeek(diaSemana);
                    novoDia.setCurrent(false);
                    novosDias.add(novoDia);
                    dataInicio = dataInicio.plusDays(1);
                }

                dayRepository.saveAll(novosDias);
            }
        }
    }

    @Transactional
    public DayDetailResponseDTO buscarPorId(UUID id) {
        Day day = dayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dia não encontrado"));

        day.getTarefasPrevistas().size();
        day.getCompletedTasks().size();

        List<TarefaResponseDTO> tarefasDTO = day.getTarefasPrevistas().stream()
                .map(TarefaResponseDTO::new)
                .toList();

        List<CompletedTaskResponseDTO> completedTasksDTO = day.getCompletedTasks().stream()
                .map(CompletedTaskResponseDTO::new)
                .toList();

        return new DayDetailResponseDTO(
                day.getId(),
                day.getDate(),
                day.getDayOfWeek(),
                day.isCurrent(),
                tarefasDTO,
                completedTasksDTO
        );
    }

    public void addTaskToDayAndFutureDays(Tarefa tarefa, UUID userId, LocalDate fromDate) {
        List<Day> futureDays = dayRepository.findAllByUserIdAndDateGreaterThanEqualWithTarefas(userId, fromDate);

        for (Day day : futureDays) {
            int dayOfWeek = day.getDate().getDayOfWeek().getValue() % 7 + 1;
            if (tarefa.getDaysOfTheWeek().contains(dayOfWeek)) {
                day.getTarefasPrevistas().add(tarefa);
            }
        }

        dayRepository.saveAll(futureDays);
    }

    public void updateTaskFromDateAndFutureDays(Tarefa tarefa, List<Integer> diasAntigos, LocalDate fromDate) {
        UUID userId = tarefa.getUser().getId();

        List<Day> diasFuturos = dayRepository.findAllByUserIdAndDateGreaterThanEqualWithTarefas(userId, fromDate);
        UUID tarefaId = tarefa.getId();

        for (Day dia : diasFuturos) {
            int diaDaSemana = dia.getDate().getDayOfWeek().getValue() % 7 + 1;

            boolean estavaAntes = diasAntigos.contains(diaDaSemana);
            boolean estaAgora = tarefa.getDaysOfTheWeek().contains(diaDaSemana);

            if (estavaAntes && !estaAgora) {
                dia.getTarefasPrevistas().removeIf(t -> t.getId().equals(tarefaId));
            } else if (!estavaAntes && estaAgora) {
                if (dia.getTarefasPrevistas().stream().noneMatch(t -> t.getId().equals(tarefaId))) {
                    dia.getTarefasPrevistas().add(tarefa);
                }
            }
        }

        dayRepository.saveAll(diasFuturos);
    }


    public void deleteTaskFromDayAndFutureDays(UUID userId, UUID dayId, Tarefa tarefa) {
        Day fromDay = dayRepository.findByIdAndUserId(dayId, userId)
                .orElseThrow(() -> new DayNotFoundException(dayId));

        List<Day> dias = dayRepository.findAllByUserIdAndDateGreaterThanEqualWithTarefas(
                userId, fromDay.getDate());

        for (Day dia : dias) {
            dia.getTarefasPrevistas().removeIf(t -> t.getId().equals(tarefa.getId()));
        }

        dayRepository.saveAll(dias);
    }




    public List<Tarefa> removerVersoesFuturasDosDias(Tarefa originalTask, LocalDate cutoffDate, UUID userId) {
        List<Tarefa> todasVersoes = new ArrayList<>();
        todasVersoes.add(originalTask);

        List<Tarefa> outras = tarefaRepository.findAllByOriginalTask(originalTask);
        todasVersoes.addAll(outras);

        List<Day> diasFuturos = dayRepository.findAllByUserIdAndDateGreaterThanEqualWithTarefas(userId, cutoffDate);

        Set<UUID> idsRemovidos = new HashSet<>();

        for (Day dia : diasFuturos) {
            boolean mudou = dia.getTarefasPrevistas().removeIf(t -> {
                boolean corresponde = todasVersoes.stream().anyMatch(v -> v.getId().equals(t.getId()));
                if (corresponde) idsRemovidos.add(t.getId());
                return corresponde;
            });

            if (mudou) {
                dayRepository.save(dia);
            }
        }

        return todasVersoes.stream()
                .filter(t -> idsRemovidos.contains(t.getId()))
                .collect(Collectors.toList());
    }

    public void excluirVersoesSeNaoUsadasAntes(List<Tarefa> versoesRemovidas, LocalDate cutoffDate, UUID userId) {
        List<Day> diasPassados = dayRepository.findAllByUserIdAndDateLessThanWithTarefas(userId, cutoffDate);
        Set<UUID> usadasAntes = diasPassados.stream()
                .flatMap(d -> d.getTarefasPrevistas().stream())
                .map(Tarefa::getId)
                .collect(Collectors.toSet());

        List<Tarefa> deletar = versoesRemovidas.stream()
                .filter(t -> !usadasAntes.contains(t.getId()))
                .collect(Collectors.toList());

        tarefaRepository.deleteAll(deletar);
    }

    public boolean seraExcluidaSeNaoUsadaAntes(Tarefa task, LocalDate cutoffDate, UUID userId) {
        List<Day> diasPassados = dayRepository.findAllByUserIdAndDateLessThanWithTarefas(userId, cutoffDate);
        Set<UUID> usadasAntes = diasPassados.stream()
                .flatMap(d -> d.getTarefasPrevistas().stream())
                .map(Tarefa::getId)
                .collect(Collectors.toSet());

        return !usadasAntes.contains(task.getId());
    }


}
