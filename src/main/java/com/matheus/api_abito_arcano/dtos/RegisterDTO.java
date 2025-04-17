package com.matheus.api_abito_arcano.dtos;

import com.matheus.api_abito_arcano.models.UserRole;

public record RegisterDTO(String login, String password, UserRole role){
}