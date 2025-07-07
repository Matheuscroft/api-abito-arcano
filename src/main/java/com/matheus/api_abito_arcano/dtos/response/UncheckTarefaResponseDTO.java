package com.matheus.api_abito_arcano.dtos.response;

public record UncheckTarefaResponseDTO(
        CompletedTaskResponseDTO previouslyCompletedTask,
        ScoreResponseDTO score
) {}
