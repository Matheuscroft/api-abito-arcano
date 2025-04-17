package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.SubareaDTO;
import com.matheus.api_abito_arcano.dtos.response.AreaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaResponseDTO;
import com.matheus.api_abito_arcano.mappers.SubareaMapper;
import com.matheus.api_abito_arcano.models.Subarea;
import com.matheus.api_abito_arcano.services.SubareaService;
import com.matheus.api_abito_arcano.services.TarefaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subareas")
public class SubareaController {

    private static final Logger logger = LoggerFactory.getLogger(SubareaController.class);

    @Autowired
    private SubareaService subareaService;

    @PostMapping
    public ResponseEntity<SubareaResponseDTO> criarSubarea(@RequestBody @Valid SubareaDTO subareaDTO) {
        try {
            Subarea subarea = subareaService.criarSubarea(subareaDTO);
            return ResponseEntity.ok(SubareaMapper.toDTO(subarea));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<SubareaResponseDTO>> listarSubareas() {
        List<SubareaResponseDTO> subareaResponseDTOs = subareaService.buscarPorUsuarioAutenticado();
        return ResponseEntity.ok(subareaResponseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubareaResponseDTO> buscarPorId(@PathVariable UUID id) {
        Optional<Subarea> subarea = subareaService.buscarPorId(id);
        return subarea
                .map(s -> ResponseEntity.ok(SubareaMapper.toDTO(s)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/area/{areaId}")
    public ResponseEntity<List<SubareaResponseDTO>> buscarPorAreaId(@PathVariable UUID areaId) {
        List<Subarea> subareas = subareaService.buscarPorAreaId(areaId);
        List<SubareaResponseDTO> dtos = subareas.stream()
                .map(SubareaMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }


    @PutMapping("/{id}")
    public ResponseEntity<SubareaResponseDTO> atualizarSubarea(@PathVariable UUID id, @RequestBody @Valid SubareaDTO subareaDTO) {
        try {
            Subarea subarea = subareaService.atualizarSubarea(id, subareaDTO);
            return ResponseEntity.ok(SubareaMapper.toDTO(subarea));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarSubarea(@PathVariable UUID id) {
        boolean deletado = subareaService.deletarSubarea(id);

        if (deletado) {
            logger.info("Subárea deletada com sucesso.");
            return ResponseEntity.ok("Subárea deletada com sucesso.");
        } else {
            logger.warn("Subárea não encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Subárea não encontrada.");
        }
    }




}
