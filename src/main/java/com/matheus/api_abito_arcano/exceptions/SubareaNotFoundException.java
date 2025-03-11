package com.matheus.api_abito_arcano.exceptions;

import java.util.UUID;

public class SubareaNotFoundException extends RuntimeException {
    public SubareaNotFoundException(UUID id) {
        super("Subárea com ID " + id + " não encontrada.");
    }
}