package com.matheus.api_abito_arcano.dtos;

import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record TarefaDTO (@NotBlank String title,
                         @NotNull int score,
                         @NotNull List<Integer> daysOfTheWeek,
                         UUID areaId,
                         UUID subareaId
){
}
