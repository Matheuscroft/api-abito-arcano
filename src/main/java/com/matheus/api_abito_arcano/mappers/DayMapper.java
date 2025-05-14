package com.matheus.api_abito_arcano.mappers;

import com.matheus.api_abito_arcano.dtos.response.DayResponseDTO;
import com.matheus.api_abito_arcano.models.Day;

public class DayMapper {
    public static DayResponseDTO toDTO(Day day) {
        return new DayResponseDTO(
                day.getId(),
                day.getDate(),
                day.getDayOfWeek(),
                day.isCurrent()
        );
    }
}