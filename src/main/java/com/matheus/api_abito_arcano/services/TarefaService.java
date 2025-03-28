package com.matheus.api_abito_arcano.services;


import com.matheus.api_abito_arcano.dtos.TarefaDTO;
import com.matheus.api_abito_arcano.dtos.response.TarefaResponseDTO;
import com.matheus.api_abito_arcano.exceptions.AreaNotFoundException;
import com.matheus.api_abito_arcano.exceptions.InvalidAreaException;
import com.matheus.api_abito_arcano.exceptions.SubareaNotFoundException;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.models.Tarefa;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.SubareaRepository;
import com.matheus.api_abito_arcano.repositories.TarefaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public Tarefa criarTarefa(TarefaDTO tarefaDto) {

        logger.info("Iniciando criação de tarefa: {}", tarefaDto.title());

        Area area = null;
        if (tarefaDto.areaId() != null) {
            logger.info("Buscando área com ID: {}", tarefaDto.areaId());
            Optional<Area> optionalArea = areaRepository.findById(tarefaDto.areaId());
            if (optionalArea.isPresent()) {
                area = optionalArea.get();
            }
        }

        if (area == null) {
            logger.warn("Nenhuma área fornecida ou encontrada, usando 'Sem Categoria'");
            area = areaRepository.findByName("Sem Categoria")
                    .orElseThrow(() -> {
                        logger.error("Área 'Sem Categoria' não encontrada! Verifique se ela existe no banco de dados.");
                        return new AreaNotFoundException(UUID.randomUUID());
                    });
        }

        Subarea subarea = null;
        if (tarefaDto.subareaId() != null) {
            logger.info("Buscando subárea com ID: {}", tarefaDto.subareaId());
            Optional<Subarea> optionalSubarea = subareaRepository.findById(tarefaDto.subareaId());
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
        tarefa.setTitle(tarefaDto.title());
        tarefa.setScore(tarefaDto.score());
        tarefa.setDaysOfTheWeek(tarefaDto.daysOfTheWeek());
        tarefa.setArea(area);

        if (subarea != null) {
            logger.info("Subárea encontrada: {} - Área associada: {}", subarea.getName(), subarea.getArea());
            tarefa.setSubarea(subarea);
        }

        logger.info("Salvando tarefa: {}", tarefa.getTitle());

        return tarefaRepository.save(tarefa);
    }

    public List<TarefaResponseDTO> listarTarefas() {
        List<Tarefa> tarefas = tarefaRepository.findAll();

        List<TarefaResponseDTO> tarefaDTOs = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            tarefaDTOs.add(new TarefaResponseDTO(tarefa));
        }

        return tarefaDTOs;
    }
    public TarefaResponseDTO buscarPorId(UUID id) {
        Optional<Tarefa> tarefaOptional = tarefaRepository.findById(id);
        return tarefaOptional.map(TarefaResponseDTO::new).orElse(null);
    }

    public Tarefa atualizarTarefa(UUID id, TarefaDTO tarefaDTO) {
        Optional<Tarefa> tarefaOptional = tarefaRepository.findById(id);
        if (tarefaOptional.isPresent()) {
            Tarefa tarefa = tarefaOptional.get();
            tarefa.setTitle(tarefaDTO.title());
            tarefa.setScore(tarefaDTO.score());
            tarefa.setDaysOfTheWeek(tarefaDTO.daysOfTheWeek());

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
                    tarefa.setSubarea(subareaOptional.get());
                } else {
                    throw new SubareaNotFoundException(tarefaDTO.subareaId());
                }
            }

            return tarefaRepository.save(tarefa);
        }
        return null;
    }

    public boolean deletarTarefa(UUID id) {
        Optional<Tarefa> tarefaOptional = tarefaRepository.findById(id);
        if (tarefaOptional.isPresent()) {
            tarefaRepository.delete(tarefaOptional.get());
            return true;
        }
        return false;
    }
}
