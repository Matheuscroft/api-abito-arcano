package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.SubareaDTO;
import com.matheus.api_abito_arcano.dtos.response.AreaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaSimpleResponseDTO;
import com.matheus.api_abito_arcano.exceptions.AreaNotFoundException;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.SubareaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubareaService {

    private static final Logger logger = LoggerFactory.getLogger(TarefaService.class);

    @Autowired
    private SubareaRepository subareaRepository;

    @Autowired
    private AreaRepository areaRepository;

    public Subarea criarSubarea(SubareaDTO subareaDTO) {

        Optional<Area> areaOptional = areaRepository.findById(subareaDTO.areaId());
        if (areaOptional.isEmpty()) {
            throw new AreaNotFoundException(subareaDTO.areaId());
        }

        Subarea subarea = new Subarea();
        subarea.setName(subareaDTO.name());
        subarea.setArea(areaOptional.get());

        return subareaRepository.save(subarea);
    }

    public List<SubareaResponseDTO> listarSubareas() {
        //return subareaRepository.findAll();

        List<Subarea> subareas = subareaRepository.findAll();

        return subareas.stream()
                .map(subarea -> new SubareaResponseDTO(
                        subarea.getId(),
                        subarea.getName(),
                        subarea.getArea().getId()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Subarea> buscarPorId(UUID id) {
        return subareaRepository.findById(id);
    }

    public List<Subarea> buscarPorAreaId(UUID areaId) {
        return subareaRepository.findByAreaId(areaId);
    }

    public Subarea atualizarSubarea(UUID id, SubareaDTO subareaDTO) {
        Optional<Subarea> subareaOptional = subareaRepository.findById(id);

        if (subareaOptional.isPresent()) {
            Subarea subarea = subareaOptional.get();
            subarea.setName(subareaDTO.name());

            return subareaRepository.save(subarea);
        }

        return null;
    }

    public boolean deletarSubarea(UUID id) {
        Optional<Subarea> subareaOptional = subareaRepository.findById(id);
        if (subareaOptional.isPresent()) {
            subareaRepository.delete(subareaOptional.get());
            logger.info("Subárea deletada: {}", id);
            return true;
        }

        logger.info("Subárea não encontrada: {}", id);
        return false;
    }
}
