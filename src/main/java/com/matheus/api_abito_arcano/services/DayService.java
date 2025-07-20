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
                .sorted(Comparator.comparing(DayResponseDTO::date))
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

                // ⚠️ Evita duplicar a tarefa no mesmo dia
                boolean jaContemTarefa = day.getTarefasPrevistas().stream()
                        .anyMatch(t -> t.getId().equals(tarefa.getId()));

                if (jaContemTarefa) continue;

                // ⚠️ Se a tarefa sendo adicionada é um clone, não adiciona no dia que já tiver completed da original
                if (tarefa.getOriginalTask() != null) {
                    UUID originalId = tarefa.getOriginalTask().getId();

                    boolean diaTemCompletedDaOriginal = day.getCompletedTasks().stream()
                            .anyMatch(ct -> ct.getTarefa().getId().equals(originalId));

                    if (diaTemCompletedDaOriginal) continue;
                }

                day.getTarefasPrevistas().add(tarefa);
            }
        }

        dayRepository.saveAll(futureDays);
    }


    @Transactional
    public void updateTaskFromDateAndFutureDays(Tarefa tarefa, List<Integer> diasAntigos, LocalDate fromDate) {
        UUID userId = tarefa.getUser().getId();
        UUID tarefaId = tarefa.getId();

        logger.info("[updateTaskFromDateAndFutureDays] Iniciando atualização da tarefa {} a partir da data {} para o usuário {}", tarefaId, fromDate, userId);

        List<Day> diasFuturos = dayRepository.findAllByUserIdAndDateGreaterThanEqualWithTarefas(userId, fromDate);
        logger.info("[updateTaskFromDateAndFutureDays] {} dias futuros encontrados para análise", diasFuturos.size());


        for (Day dia : diasFuturos) {

            LocalDate data = dia.getDate();
            int diaDaSemana = data.getDayOfWeek().getValue() % 7 + 1;

            logger.info(" - Analisando dia {} (dia da semana: {})", data, diaDaSemana);

            boolean hasCompletedTask = dia.getCompletedTasks().stream()
                    .anyMatch(ct -> ct.getTarefa().getId().equals(tarefaId));

            if (hasCompletedTask) {
                logger.info("   • Dia {} contém CompletedTask para a tarefa {}. Ignorando alterações neste dia.", data, tarefaId);
                continue;
            }

            boolean estavaAntes = diasAntigos.contains(diaDaSemana);
            boolean estaAgora = tarefa.getDaysOfTheWeek().contains(diaDaSemana);

            if(estavaAntes && !estaAgora) {
                logger.info("   • A tarefa {} estava prevista nesse dia, mas não está mais. Removendo do dia {}", tarefaId, data);
                dia.getTarefasPrevistas().removeIf(t -> t.getId().equals(tarefaId));
            } else if (!estavaAntes && estaAgora) {
                boolean jaPresente = dia.getTarefasPrevistas().stream().anyMatch(t -> t.getId().equals(tarefaId));
                if (!jaPresente) {
                    logger.info("   • A tarefa {} não estava nesse dia, mas agora está. Adicionando ao dia {}", tarefaId, data);
                    dia.getTarefasPrevistas().add(tarefa);
                } else {
                    logger.info("   • A tarefa {} já estava corretamente prevista no dia {}. Nada será feito.", tarefaId, data);
                }
            } else {
                logger.info("   • Sem mudanças necessárias para a tarefa {} no dia {}", tarefaId, data);
            }
        }

        dayRepository.saveAll(diasFuturos);
        logger.info("[updateTaskFromDateAndFutureDays] Atualização concluída.");
    }



    public void deleteTaskFromDaysAndFromDate(UUID userId, LocalDate fromDate, Tarefa tarefa) {

        List<Day> diasComTarefa = dayRepository.findAllByUserIdAndTarefaWithTarefasFromDate(userId, tarefa, fromDate);
        logger.info("[deleteTaskFromDayAndFutureDays] Dias que contêm a tarefa {} a partir de {}: {}",
                tarefa.getId(), fromDate, diasComTarefa.size());

        // Remove tarefa prevista
        for (Day dia : diasComTarefa) {
            boolean removed = dia.getTarefasPrevistas().removeIf(t -> t.getId().equals(tarefa.getId()));
            if (removed) {
                logger.info("Dia {} ({}) - tarefa removida de tarefasPrevistas", dia.getId(), dia.getDate());
            }
        }

        if (!diasComTarefa.isEmpty()) {
            logger.info("Salvando {} dias modificados no total", diasComTarefa.size());
            dayRepository.saveAll(diasComTarefa);
            logger.info("Todos os dias foram salvos com sucesso");
        } else {
            logger.info("Nenhum dia foi modificado, nada foi salvo");
        }

    }





    public List<Tarefa> removerVersoesFuturasDosDias(Tarefa originalTask, LocalDate cutoffDate, UUID userId) {
        logger.info("[removerVersoesFuturasDosDias] Iniciando remoção das versões futuras a partir de {}", cutoffDate);

        List<Tarefa> todasVersoes = new ArrayList<>();
        todasVersoes.add(originalTask);
        List<Tarefa> outras = tarefaRepository.findAllByOriginalTask(originalTask);
        todasVersoes.addAll(outras);

        logger.info("[removerVersoesFuturasDosDias] Total de versões encontradas: {}", todasVersoes.size());

        List<Day> diasFuturos = dayRepository.findAllByUserIdAndDateGreaterThanEqualWithTarefas(userId, cutoffDate);
        logger.info("[removerVersoesFuturasDosDias] Dias futuros analisados: {}", diasFuturos.size());

        Set<UUID> idsRemovidos = new HashSet<>();

        for (Day dia : diasFuturos) {
            logger.debug("[removerVersoesFuturasDosDias] Analisando dia {}", dia.getDate());

            boolean mudou = dia.getTarefasPrevistas().removeIf(t -> {
                boolean corresponde = todasVersoes.stream().anyMatch(v -> v.getId().equals(t.getId()));

                // 🚫 NÃO remover se essa tarefa foi completada neste dia
                boolean foiCompletada = dia.getCompletedTasks().stream()
                        .anyMatch(ct -> ct.getTarefa().getId().equals(t.getId()));

                if (corresponde && !foiCompletada) {
                    logger.info("[removerVersoesFuturasDosDias] Removendo tarefa {} do dia {}", t.getId(), dia.getDate());
                    idsRemovidos.add(t.getId());
                    return true;
                }

                return false;
            });

            if (mudou) {
                dayRepository.save(dia);
                logger.debug("[removerVersoesFuturasDosDias] Dia {} atualizado", dia.getDate());
            }
        }

        return todasVersoes.stream()
                .filter(t -> idsRemovidos.contains(t.getId()))
                .collect(Collectors.toList());
    }

    public void excluirVersoesSeNaoUsadasAntes(List<Tarefa> versoesRemovidas, LocalDate cutoffDate, UUID userId) {
        logger.info("[excluirVersoesSeNaoUsadasAntes] Verificando uso anterior a {}", cutoffDate);

        List<Day> diasPassados = dayRepository.findAllByUserIdAndDateLessThanWithTarefas(userId, cutoffDate);
        Set<UUID> usadasAntes = diasPassados.stream()
                .flatMap(d -> d.getTarefasPrevistas().stream())
                .map(Tarefa::getId)
                .collect(Collectors.toSet());

        List<Tarefa> deletar = versoesRemovidas.stream()
                .filter(t -> !usadasAntes.contains(t.getId()))
                .collect(Collectors.toList());

        logger.info("[excluirVersoesSeNaoUsadasAntes] Tarefas a deletar: {}", deletar.size());
        deletar.forEach(t -> logger.debug("[excluirVersoesSeNaoUsadasAntes] Deletando tarefa {}", t.getId()));

        tarefaRepository.deleteAll(deletar);
    }

    public boolean seraExcluidaSeNaoUsadaAntes(Tarefa task, LocalDate cutoffDate, UUID userId) {
        logger.info("[seraExcluidaSeNaoUsadaAntes] Verificando se tarefa {} será excluída antes de {}", task.getId(), cutoffDate);

        List<Day> diasPassados = dayRepository.findAllByUserIdAndDateLessThanWithTarefas(userId, cutoffDate);
        Set<UUID> usadasAntes = diasPassados.stream()
                .flatMap(d -> d.getTarefasPrevistas().stream())
                .map(Tarefa::getId)
                .collect(Collectors.toSet());

        boolean seraExcluida = !usadasAntes.contains(task.getId());
        logger.info("[seraExcluidaSeNaoUsadaAntes] Resultado: {}", seraExcluida);

        return seraExcluida;
    }

    public boolean tarefaFoiConcluidaEmDiasFuturos(Tarefa tarefa, LocalDate fromDate, UUID userId) {
        logger.info("[tarefaFoiConcluidaEmDiasFuturos] Checando se tarefa {} foi completada após {}", tarefa.getId(), fromDate);

        List<Day> diasFuturos = dayRepository.findAllByUserIdAndDateGreaterThanEqualWithTarefas(userId, fromDate);

        boolean foiConcluida = diasFuturos.stream()
                .flatMap(d -> d.getCompletedTasks().stream())
                .anyMatch(ct -> ct.getTarefa().getId().equals(tarefa.getId()));

        if (foiConcluida) {
            logger.info("[tarefaFoiConcluidaEmDiasFuturos] A tarefa {} foi completada em dias futuros a partir de {}", tarefa.getId(), fromDate);
        } else {
            logger.info("[tarefaFoiConcluidaEmDiasFuturos] Nenhum completed encontrado da tarefa {} a partir de {}", tarefa.getId(), fromDate);
        }

        return foiConcluida;
    }



}
