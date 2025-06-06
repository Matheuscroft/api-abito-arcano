package com.matheus.api_abito_arcano.dtos.response;

import com.matheus.api_abito_arcano.models.Tarefa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TarefaResponseDTO(
        UUID id,
        String title,
        int score,
        List<Integer> daysOfTheWeek,
        UUID areaId,
        UUID subareaId,
        LocalDateTime createdAt,
        UUID originalTaskId,
        boolean isLatestVersion
) {
    public TarefaResponseDTO(Tarefa tarefa) {
        this(
                tarefa.getId(),
                tarefa.getTitle(),
                tarefa.getScore(),
                tarefa.getDaysOfTheWeek(),
                tarefa.getArea().getId(),
                tarefa.getSubarea() != null ? tarefa.getSubarea().getId() : null,
                tarefa.getCreatedAt(),
                tarefa.getOriginalTask() != null ? tarefa.getOriginalTask().getId() : null,
                tarefa.isLatestVersion()
        );
    }
}
