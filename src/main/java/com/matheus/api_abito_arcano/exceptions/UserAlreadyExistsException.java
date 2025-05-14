package com.matheus.api_abito_arcano.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String login) {
        super("Usuário com login '" + login + "' já existe.");
    }
}
