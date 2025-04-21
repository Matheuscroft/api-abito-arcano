package com.matheus.api_abito_arcano.services;


import com.matheus.api_abito_arcano.dtos.TarefaDTO;
import com.matheus.api_abito_arcano.dtos.response.TarefaResponseDTO;
import com.matheus.api_abito_arcano.exceptions.AreaNotFoundException;
import com.matheus.api_abito_arcano.exceptions.InvalidAreaException;
import com.matheus.api_abito_arcano.exceptions.SubareaNotFoundException;
import com.matheus.api_abito_arcano.exceptions.TarefaNotFoundException;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.models.Tarefa;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.SubareaRepository;
import com.matheus.api_abito_arcano.repositories.TarefaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TarefaService {

    private static final Logger logger = LoggerFactory.getLogger(TarefaService.class);

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private SubareaRepository subareaRepository;

    @Autowired
    private UserService userService;

    public Tarefa criarTarefa(TarefaDTO tarefaDto) {

        User user = userService.getUsuarioAutenticado();

        logger.info("Iniciando criação de tarefa: {}", tarefaDto.title());

        Area area = null;
        if (tarefaDto.areaId() != null) {
            logger.info("Buscando área com ID: {}", tarefaDto.areaId());
            Optional<Area> optionalArea = areaRepository.findByIdAndUserId(tarefaDto.areaId(), user.getId());
            if (optionalArea.isPresent()) {
                area = optionalArea.get();
            }
        }

        if (area == null) {
            logger.warn("Área não fornecida ou não encontrada, usando 'Sem Categoria'");
            area = areaRepository.findByNameAndUserId("Sem Categoria", user.getId())
                    .orElseThrow(() -> new AreaNotFoundException(UUID.randomUUID()));
        }

        Subarea subarea = null;
        if (tarefaDto.subareaId() != null) {
            logger.info("Buscando subárea com ID: {}", tarefaDto.subareaId());
            Optional<Subarea> optionalSubarea = subareaRepository.findByIdAndArea_User_Id(tarefaDto.subareaId(), user.getId());
            if (optionalSubarea.isPresent()) {
                subarea = optionalSubarea.get();

                logger.info("Subárea encontrada: {}", subarea.getName());

                if (!subarea.getArea().getId().equals(area.getId())) {
                    logger.error("A subárea '{}' pertence à área '{}' ({}) mas foi passada a área '{}' ({})",
                            subarea.getName(), subarea.getArea().getName(), subarea.getArea().getId(),
                            area.getName(), area.getId());
                    throw new InvalidAreaException("A subárea fornecida não pertence à área fornecida.");
                }

            } else {
                logger.error("Subárea com ID {} não encontrada!", tarefaDto.subareaId());
                throw new SubareaNotFoundException(tarefaDto.subareaId());
            }
        }

        Tarefa tarefa = new Tarefa();
        BeanUtils.copyProperties(tarefaDto, tarefa);
        tarefa.setArea(area);
        tarefa.setUser(user);

        if (subarea != null) {
            logger.info("Subárea encontrada: {} - Área associada: {}", subarea.getName(), subarea.getArea());
            tarefa.setSubarea(subarea);
        }

        logger.info("Salvando tarefa: {}", tarefa.getTitle());

        return tarefaRepository.save(tarefa);
    }

    public List<TarefaResponseDTO> listarTarefas() {
        User user = userService.getUsuarioAutenticado();
        List<Tarefa> tarefas = tarefaRepository.findByUserId(user.getId());
        return tarefas.stream().map(TarefaResponseDTO::new).toList();
    }

    public TarefaResponseDTO buscarPorId(UUID id) {
        User user = userService.getUsuarioAutenticado();

        Tarefa tarefa = tarefaRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new TarefaNotFoundException(id));

        return new TarefaResponseDTO(tarefa);
    }



    public Tarefa atualizarTarefa(UUID id, TarefaDTO tarefaDTO) {

        User user = userService.getUsuarioAutenticado();

        Optional<Tarefa> tarefaOptional = tarefaRepository.findByIdAndUserId(id, user.getId());
        if (tarefaOptional.isPresent()) {
            Tarefa tarefa = tarefaOptional.get();
            BeanUtils.copyProperties(tarefaDTO, tarefa, "areaId", "subareaId");

            if (tarefaDTO.areaId() == null) {
                tarefa.setArea(null);
            } else {
                Optional<Area> areaOptional = areaRepository.findById(tarefaDTO.areaId());
                if (areaOptional.isPresent()) {
                    tarefa.setArea(areaOptional.get());
                } else {
                    throw new AreaNotFoundException(tarefaDTO.areaId());
                }
            }

            if (tarefaDTO.subareaId() == null) {
                tarefa.setSubarea(null);
            } else {
                Optional<Subarea> subareaOptional = subareaRepository.findById(tarefaDTO.subareaId());
                if (subareaOptional.isPresent()) {
                    Subarea subarea = subareaOptional.get();

                    if (tarefa.getArea() != null && !subarea.getArea().getId().equals(tarefa.getArea().getId())) {
                        throw new InvalidAreaException("A subárea fornecida não pertence à área fornecida.");
                    }

                    tarefa.setSubarea(subarea);
                } else {
                    throw new SubareaNotFoundException(tarefaDTO.subareaId());
                }
            }

            return tarefaRepository.save(tarefa);
        }
        return null;
    }

    public boolean deletarTarefa(UUID id) {
        User user = userService.getUsuarioAutenticado();

        Optional<Tarefa> tarefaOptional = tarefaRepository.findByIdAndUserId(id, user.getId());

        if (tarefaOptional.isPresent()) {
            tarefaRepository.delete(tarefaOptional.get());
            return true;
        }

        return false;
    }

}
