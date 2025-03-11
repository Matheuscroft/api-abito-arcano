package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.SubareaDTO;
import com.matheus.api_abito_arcano.exceptions.AreaNotFoundException;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.SubareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubareaService {

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
        subarea.setNome(subareaDTO.nome());
        subarea.setArea(areaOptional.get());

        return subareaRepository.save(subarea);
    }

    public List<Subarea> listarSubareas() {
        return subareaRepository.findAll();
    }

    public Optional<Subarea> buscarPorId(UUID id) {
        return subareaRepository.findById(id);
    }

    public Subarea atualizarSubarea(UUID id, SubareaDTO subareaDTO) {
        Optional<Subarea> subareaOptional = subareaRepository.findById(id);
        if (subareaOptional.isPresent()) {
            Subarea subarea = subareaOptional.get();
            subarea.setNome(subareaDTO.nome());

            Optional<Area> areaOptional = areaRepository.findById(subareaDTO.areaId());
            if (areaOptional.isEmpty()) {
                throw new IllegalStateException("Área com o ID fornecido não encontrada.");
            }
            subarea.setArea(areaOptional.get());

            return subareaRepository.save(subarea);
        }
        return null;
    }

    public boolean deletarSubarea(UUID id) {
        Optional<Subarea> subareaOptional = subareaRepository.findById(id);
        if (subareaOptional.isPresent()) {
            subareaRepository.delete(subareaOptional.get());
            return true;
        }
        return false;
    }
}
