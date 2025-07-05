package com.matheus.api_abito_arcano.dtos;

import java.util.UUID;

public record CompletedTaskDTO(
        UUID tarefaId,
        UUID dayId
) {}