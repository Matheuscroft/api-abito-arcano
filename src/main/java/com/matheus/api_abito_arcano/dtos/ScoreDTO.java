package com.matheus.api_abito_arcano.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ScoreDTO(
        @NotNull UUID tarefaId,
        @NotNull UUID dayId
) {}
