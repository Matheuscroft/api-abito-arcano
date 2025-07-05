package com.matheus.api_abito_arcano.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CompletedTaskWithScoreDTO(
        UUID completedTaskId,
        UUID tarefaId,
        LocalDateTime completedAt,
        UUID scoreId,
        UUID areaId,
        String areaName,
        UUID subareaId,
        String subareaName,
        LocalDate date,
        int score
) {}
