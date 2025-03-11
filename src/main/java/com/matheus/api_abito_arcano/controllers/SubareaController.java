package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.SubareaDTO;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.services.SubareaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/subareas")
public class SubareaController {

    @Autowired
    private SubareaService subareaService;

    @PostMapping
    public ResponseEntity<Subarea> criarSubarea(@RequestBody @Valid SubareaDTO subareaDTO) {
        try {
            return ResponseEntity.ok(subareaService.criarSubarea(subareaDTO));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Subarea>> listarSubareas() {
        return ResponseEntity.ok(subareaService.listarSubareas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subarea> buscarPorId(@PathVariable UUID id) {
        Optional<Subarea> subarea = subareaService.buscarPorId(id);
        return subarea.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subarea> atualizarSubarea(@PathVariable UUID id, @RequestBody @Valid SubareaDTO subareaDTO) {
        try {
            Subarea subarea = subareaService.atualizarSubarea(id, subareaDTO);
            return (subarea != null) ? ResponseEntity.ok(subarea) : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSubarea(@PathVariable UUID id) {
        return subareaService.deletarSubarea(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
