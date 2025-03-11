package com.matheus.api_abito_arcano.services;


import com.matheus.api_abito_arcano.dtos.TarefaDTO;
import com.matheus.api_abito_arcano.exceptions.AreaNotFoundException;
import com.matheus.api_abito_arcano.exceptions.InvalidAreaException;
import com.matheus.api_abito_arcano.exceptions.SubareaNotFoundException;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.models.Tarefa;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.SubareaRepository;
import com.matheus.api_abito_arcano.repositories.TarefaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TarefaService {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private SubareaRepository subareaRepository;

    public Tarefa criarTarefa(TarefaDTO tarefaDto) {

        Area area = null;
        if (tarefaDto.areaId() != null) {
            Optional<Area> optionalArea = areaRepository.findById(tarefaDto.areaId());
            if (optionalArea.isPresent()) {
                area = optionalArea.get();
            }
        }

        if (area == null) {
            area = areaRepository.findByNome("Sem Categoria")
                    .orElseThrow(() -> new AreaNotFoundException(UUID.randomUUID()));
        }

        Subarea subarea = null;
        if (tarefaDto.subareaId() != null) {
            Optional<Subarea> optionalSubarea = subareaRepository.findById(tarefaDto.subareaId());
            if (optionalSubarea.isPresent()) {
                subarea = optionalSubarea.get();

                if (!subarea.getArea().equals(area)) {
                    throw new InvalidAreaException("A subárea fornecida não pertence à área fornecida.");
                }
            } else {
                throw new SubareaNotFoundException(tarefaDto.subareaId());
            }
        }

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(tarefaDto.titulo());
        tarefa.setPontuacao(tarefaDto.pontuacao());
        tarefa.setDiasSemana(tarefaDto.diasSemana());
        tarefa.setArea(area);

        if (subarea != null) {
            tarefa.setSubarea(subarea);
        }

        return tarefaRepository.save(tarefa);
    }

    public List<Tarefa> listarTarefas() {
        return tarefaRepository.findAll();
    }

    public Optional<Tarefa> buscarPorId(UUID id) {
        return tarefaRepository.findById(id);
    }

    public Tarefa atualizarTarefa(UUID id, TarefaDTO tarefaDTO) {
        Optional<Tarefa> tarefaOptional = tarefaRepository.findById(id);
        if (tarefaOptional.isPresent()) {
            Tarefa tarefa = tarefaOptional.get();
            tarefa.setTitulo(tarefaDTO.titulo());
            tarefa.setPontuacao(tarefaDTO.pontuacao());
            tarefa.setDiasSemana(tarefaDTO.diasSemana());
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
