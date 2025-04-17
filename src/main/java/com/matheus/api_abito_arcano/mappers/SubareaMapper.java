package com.matheus.api_abito_arcano.mappers;

import com.matheus.api_abito_arcano.dtos.response.SubareaResponseDTO;
import com.matheus.api_abito_arcano.models.Subarea;

public class SubareaMapper {

    public static SubareaResponseDTO toDTO(Subarea subarea) {
        return new SubareaResponseDTO(
                subarea.getId(),
                subarea.getName(),
                subarea.getArea().getId()
        );
    }
}