package com.matheus.api_abito_arcano.dtos.response;

import com.matheus.api_abito_arcano.models.Tarefa;
import java.util.List;
import java.util.UUID;

public record TarefaResponseDTO(
        UUID id,
        String titulo,
        int pontuacao,
        List<Integer> diasSemana,
        UUID areaId,
        UUID subareaId
) {
    public TarefaResponseDTO(Tarefa tarefa) {
        this(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getPontuacao(),
                tarefa.getDiasSemana(),
                tarefa.getArea().getId(),
                tarefa.getSubarea() != null ? tarefa.getSubarea().getId() : null
        );
    }
}
