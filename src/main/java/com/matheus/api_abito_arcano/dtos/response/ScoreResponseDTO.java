package com.matheus.api_abito_arcano.dtos.response;

import java.time.LocalDate;
import java.util.UUID;

public record ScoreResponseDTO(
        UUID scoreId,
        UUID areaId,
        String areaName,
        UUID subareaId,
        String subareaName,
        LocalDate date,
        int score
) {}
