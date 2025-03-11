package com.matheus.api_abito_arcano.dtos;

import jakarta.validation.constraints.NotBlank;

public record AreaDTO(
        @NotBlank String nome,
        @NotBlank String cor
) {
}
