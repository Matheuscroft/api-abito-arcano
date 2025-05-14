package com.matheus.api_abito_arcano.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DayDTO(
        @NotNull LocalDate date
) {}