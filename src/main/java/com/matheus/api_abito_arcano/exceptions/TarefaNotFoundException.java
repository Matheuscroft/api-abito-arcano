package com.matheus.api_abito_arcano.exceptions;

import java.util.UUID;

public class TarefaNotFoundException extends RuntimeException {
    public TarefaNotFoundException(UUID id) {
        super("Tarefa com ID " + id + " não encontrada.");
    }
}