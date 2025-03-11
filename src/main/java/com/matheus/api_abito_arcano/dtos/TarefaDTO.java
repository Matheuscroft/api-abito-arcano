package com.matheus.api_abito_arcano.dtos;

import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record TarefaDTO (@NotBlank String titulo,
                         @NotNull int pontuacao,
                         @NotNull List<Integer> diasSemana,
                         UUID areaId,
                         UUID subareaId
){
}
