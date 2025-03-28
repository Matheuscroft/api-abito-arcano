package com.matheus.api_abito_arcano.dtos.response;


import java.util.List;
import java.util.UUID;

public record AreaResponseDTO(
        UUID id,
        String name,
        String color,
        List<SubareaSimpleResponseDTO> subareas
) {
}
