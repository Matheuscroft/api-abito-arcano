package com.matheus.api_abito_arcano.mappers;

import com.matheus.api_abito_arcano.dtos.response.AreaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaSimpleResponseDTO;
import com.matheus.api_abito_arcano.models.Area;

import java.util.stream.Collectors;

public class AreaMapper {

    public static AreaResponseDTO toDTO(Area area) {
        return new AreaResponseDTO(
                area.getId(),
                area.getName(),
                area.getColor(),
                area.getSubareas().stream()
                        .map(subarea -> new SubareaSimpleResponseDTO(subarea.getId(), subarea.getName()))
                        .collect(Collectors.toList())
        );
    }
}