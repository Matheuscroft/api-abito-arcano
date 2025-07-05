package com.matheus.api_abito_arcano.dtos.response;

import com.matheus.api_abito_arcano.models.CompletedTask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CompletedTaskResponseDTO(
        UUID id,
        UUID tarefaId,
        String title,
        LocalDateTime createdAt,
        LocalDate completedAt,
        int score,
        UUID areaId,
        UUID subareaId,
        String type,
        List<Integer> daysOfTheWeek,
        boolean isLatestVersion,
        UUID originalTaskId
) {
    public CompletedTaskResponseDTO(CompletedTask task) {
        this(
                task.getId(),
                task.getTarefa().getId(),
                task.getTarefa().getTitle(),
                task.getTarefa().getCreatedAt(),
                task.getCompletedAt(),
                task.getTarefa().getScore(),
                task.getTarefa().getArea().getId(),
                task.getTarefa().getSubarea() != null ? task.getTarefa().getSubarea().getId() : null,
                task.getTarefa().getType(),
                task.getTarefa().getDaysOfTheWeek(),
                task.getTarefa().isLatestVersion(),
                task.getTarefa().getOriginalTask() != null ? task.getTarefa().getOriginalTask().getId() : null
        );
    }
}
