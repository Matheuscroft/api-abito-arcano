package com.matheus.api_abito_arcano.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubareaDTO(
        @NotBlank String name,
        @NotNull UUID areaId
) {
}
