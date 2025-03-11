package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.TarefaDTO;
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
    public ResponseEntity<Tarefa> criarTarefa(@RequestBody @Valid TarefaDTO tarefaDto) {

        return ResponseEntity.ok(tarefaService.criarTarefa(tarefaDto));
    }

    @GetMapping
    public ResponseEntity<List<Tarefa>> listarTarefas() {
        List<Tarefa> tarefas = tarefaService.listarTarefas();
        if (!tarefas.isEmpty()) {
            return ResponseEntity.ok(tarefas);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(@PathVariable UUID id) {
        return tarefaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizarTarefa(@PathVariable UUID id, @RequestBody @Valid TarefaDTO tarefaDTO) {
        Tarefa tarefa = tarefaService.atualizarTarefa(id, tarefaDTO);
        return (tarefa != null) ? ResponseEntity.ok(tarefa) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTarefa(@PathVariable UUID id) {
        return tarefaService.deletarTarefa(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
