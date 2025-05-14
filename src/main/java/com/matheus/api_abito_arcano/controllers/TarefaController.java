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
    public ResponseEntity<TarefaResponseDTO> criarTarefa(
            @RequestBody @Valid TarefaDTO tarefaDto,
            @RequestParam UUID dayId) {
        Tarefa tarefa = tarefaService.criarTarefa(tarefaDto, dayId);
        return ResponseEntity.ok(new TarefaResponseDTO(tarefa));
    }

    @GetMapping
    public ResponseEntity<List<TarefaResponseDTO>> listarTarefas() {
        List<TarefaResponseDTO> tarefas = tarefaService.listarTarefas();
        if (!tarefas.isEmpty()) {
            return ResponseEntity.ok(tarefas);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> buscarPorId(@PathVariable UUID id) {
        TarefaResponseDTO tarefaDTO = tarefaService.buscarPorId(id);

        if (tarefaDTO != null) {
            return ResponseEntity.ok(tarefaDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> atualizarTarefa(
            @PathVariable UUID id,
            @RequestBody @Valid TarefaDTO tarefaDTO,
            @RequestParam UUID dayId) {

        Tarefa tarefa = tarefaService.atualizarTarefa(id, tarefaDTO, dayId);
        return (tarefa != null) ? ResponseEntity.ok(new TarefaResponseDTO(tarefa)) : ResponseEntity.notFound().build();
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTarefa(@PathVariable UUID id, @RequestParam UUID dayId) {
        boolean deleted = tarefaService.deletarTarefa(id, dayId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deletarTodasAsTarefasDoUsuario() {
        tarefaService.deletarTodasAsTarefas();
        return ResponseEntity.noContent().build();
    }


}
