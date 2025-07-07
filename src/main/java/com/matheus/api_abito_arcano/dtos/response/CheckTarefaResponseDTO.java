package com.matheus.api_abito_arcano.dtos.response;

public record CheckTarefaResponseDTO(
        CompletedTaskResponseDTO completedTask,
        ScoreResponseDTO score
) {}
