package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.TarefaDTO;
import com.matheus.api_abito_arcano.dtos.response.TarefaResponseDTO;
import com.matheus.api_abito_arcano.models.Tarefa;
import com.matheus.api_abito_arcano.repositories.TarefaRepository;
import com.matheus.api_abito_arcano.services.TarefaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    @Autowired
    private TarefaService tarefaService;

    @PostMapping
    public ResponseEntity<TarefaResponseDTO> createTask(
            @RequestBody @Valid TarefaDTO tarefaDto,
            @RequestParam UUID dayId) {
        Tarefa tarefa = tarefaService.createTask(tarefaDto, dayId);
        return ResponseEntity.ok(new TarefaResponseDTO(tarefa));
    }

    @GetMapping
    public ResponseEntity<List<TarefaResponseDTO>> getTasks() {
        List<TarefaResponseDTO> tarefas = tarefaService.getTasks();
        if (!tarefas.isEmpty()) {
            return ResponseEntity.ok(tarefas);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> getTaskById(@PathVariable UUID id) {
        TarefaResponseDTO tarefaDTO = tarefaService.getTaskById(id);

        if (tarefaDTO != null) {
            return ResponseEntity.ok(tarefaDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> updateTask(
            @PathVariable UUID id,
            @RequestBody @Valid TarefaDTO tarefaDTO,
            @RequestParam UUID dayId) {

        Tarefa tarefa = tarefaService.updateTask(id, tarefaDTO, dayId);
        return (tarefa != null) ? ResponseEntity.ok(new TarefaResponseDTO(tarefa)) : ResponseEntity.notFound().build();
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @RequestParam UUID dayId) {
        boolean deleted = tarefaService.deleteTask(id, dayId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllUserTasks() {
        tarefaService.deleteAllTasks();
        return ResponseEntity.noContent().build();
    }


}
