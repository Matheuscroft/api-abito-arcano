package com.matheus.api_abito_arcano.exceptions;

import java.util.UUID;

public class AreaNotFoundException extends RuntimeException {
    public AreaNotFoundException(UUID id) {
        super("Área com ID " + id + " não encontrada.");
    }
}