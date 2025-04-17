package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.SubareaDTO;
import com.matheus.api_abito_arcano.dtos.response.AreaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaSimpleResponseDTO;
import com.matheus.api_abito_arcano.exceptions.AreaNotFoundException;
import com.matheus.api_abito_arcano.mappers.SubareaMapper;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.models.User;
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

    @Autowired
    private AreaService areaService;

    public Subarea criarSubarea(SubareaDTO subareaDTO) {

        User user = areaService.getUsuarioAutenticado();
        Optional<Area> areaOptional = areaRepository.findByIdAndUserId(subareaDTO.areaId(), user.getId());

        if (areaOptional.isEmpty()) {
            throw new AreaNotFoundException(subareaDTO.areaId());
        }

        Subarea subarea = new Subarea();
        subarea.setName(subareaDTO.name());
        subarea.setArea(areaOptional.get());

        return subareaRepository.save(subarea);
    }

    public List<SubareaResponseDTO> listarSubareas() {

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
        User user = areaService.getUsuarioAutenticado();
        return subareaRepository.findByIdAndArea_User_Id(id, user.getId());
    }

    public List<Subarea> buscarPorAreaId(UUID areaId) {
        User user = areaService.getUsuarioAutenticado();

        Optional<Area> areaOptional = areaRepository.findByIdAndUserId(areaId, user.getId());
        if (areaOptional.isEmpty()) {
            throw new IllegalStateException("Área não encontrada ou não pertence ao usuário.");
        }

        return subareaRepository.findByAreaId(areaId);
    }


    public Subarea atualizarSubarea(UUID id, SubareaDTO subareaDTO) {
        User user = areaService.getUsuarioAutenticado();

        Optional<Subarea> subareaOptional = subareaRepository.findByIdAndArea_User_Id(id, user.getId());
        if (subareaOptional.isEmpty()) {
            throw new IllegalStateException("Subárea não encontrada ou não pertence ao usuário.");
        }

        Subarea subarea = subareaOptional.get();

        Optional<Area> areaOptional = areaRepository.findByIdAndUserId(subareaDTO.areaId(), user.getId());
        if (areaOptional.isEmpty()) {
            throw new IllegalStateException("Área fornecida não pertence ao usuário.");
        }

        if (!subarea.getArea().getId().equals(subareaDTO.areaId())) {
            throw new IllegalStateException("A subárea não pertence à área informada.");
        }

        subarea.setName(subareaDTO.name());

        return subareaRepository.save(subarea);
    }



    public boolean deletarSubarea(UUID id) {
        User user = areaService.getUsuarioAutenticado();
        Optional<Subarea> subareaOptional = subareaRepository.findByIdAndArea_User_Id(id, user.getId());

        if (subareaOptional.isPresent()) {
            Subarea subarea = subareaOptional.get();
            Area area = subarea.getArea();

            area.getSubareas().remove(subarea);
            areaRepository.save(area);

            logger.info("Subárea removida da lista da área: {}", subarea.getId());
            return true;
        }

        logger.info("Subárea não encontrada ou não pertence ao usuário: {}", id);
        return false;
    }



    public List<SubareaResponseDTO> buscarPorUsuarioAutenticado() {
        User user = areaService.getUsuarioAutenticado();
        List<Subarea> subareas = subareaRepository.findByUserId(user.getId());

        return subareas.stream()
                .map(SubareaMapper::toDTO)
                .collect(Collectors.toList());
    }

}
