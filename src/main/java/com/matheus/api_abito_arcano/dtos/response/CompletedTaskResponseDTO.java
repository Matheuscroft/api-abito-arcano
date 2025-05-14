package com.matheus.api_abito_arcano.dtos.response;

import com.matheus.api_abito_arcano.models.CompletedTask;

import java.time.LocalDate;
import java.util.UUID;

public record CompletedTaskResponseDTO(
        UUID id,
        UUID tarefaId,
        LocalDate completedAt
) {
    public CompletedTaskResponseDTO(CompletedTask task) {
        this(
                task.getId(),
                task.getTarefa().getId(),
                task.getCompletedAt()
        );
    }
}
