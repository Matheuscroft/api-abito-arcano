package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.AreaDTO;
import com.matheus.api_abito_arcano.dtos.SubareaDTO;
import com.matheus.api_abito_arcano.dtos.response.AreaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaResponseDTO;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.services.AreaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/areas")
public class AreaController {

    @Autowired
    private AreaService areaService;

    @PostMapping
    public ResponseEntity<Area> criarArea(@RequestBody @Valid AreaDTO areaDTO) {
        return ResponseEntity.ok(areaService.criarArea(areaDTO));
    }

    @GetMapping
    public ResponseEntity<List<AreaResponseDTO>> listarAreas() {
        List<AreaResponseDTO> areaResponseDTOs = areaService.listarAreas();
        return ResponseEntity.ok(areaResponseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Area> buscarPorId(@PathVariable UUID id) {
        return areaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Area> atualizarArea(@PathVariable UUID id, @RequestBody @Valid AreaDTO areaDTO) {
        Area area = areaService.atualizarArea(id, areaDTO);
        return (area != null) ? ResponseEntity.ok(area) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarArea(@PathVariable UUID id) {
        return areaService.deletarArea(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
