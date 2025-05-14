package com.matheus.api_abito_arcano.dtos.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DayDetailResponseDTO(
        UUID id,
        LocalDate date,
        int dayOfWeek,
        boolean current,
        List<TarefaResponseDTO> tarefasPrevistas,
        List<CompletedTaskResponseDTO> completedTasks
) {}
