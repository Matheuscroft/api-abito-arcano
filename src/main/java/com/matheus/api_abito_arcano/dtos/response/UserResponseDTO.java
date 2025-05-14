package com.matheus.api_abito_arcano.dtos.response;

import java.util.UUID;

public record UserResponseDTO (
        UUID id,
        String login,
        String role
        ) {


}
