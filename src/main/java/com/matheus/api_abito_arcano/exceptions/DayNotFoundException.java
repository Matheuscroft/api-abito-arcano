package com.matheus.api_abito_arcano.exceptions;

import java.util.UUID;

public class DayNotFoundException extends RuntimeException {
    public DayNotFoundException(UUID id) {
        super("Dia com ID " + id + " não encontrado.");
    }
}
