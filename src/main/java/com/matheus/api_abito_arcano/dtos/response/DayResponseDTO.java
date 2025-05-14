package com.matheus.api_abito_arcano.dtos.response;

import java.time.LocalDate;
import java.util.UUID;

public record DayResponseDTO(
        UUID id,
        LocalDate date,
        int dayOfWeek,
        boolean current
) {}