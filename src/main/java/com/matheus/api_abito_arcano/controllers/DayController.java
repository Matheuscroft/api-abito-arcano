package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.DayDTO;
import com.matheus.api_abito_arcano.dtos.response.DayDetailResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.DayResponseDTO;
import com.matheus.api_abito_arcano.models.Day;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.services.DayService;
import com.matheus.api_abito_arcano.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/days")
public class DayController {

    @Autowired
    private DayService dayService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<DayResponseDTO>> listarDiasDoUsuario() {
        User user = userService.getUsuarioAutenticado();

        dayService.verificarDiaAtualEDados(user);

        List<DayResponseDTO> response = dayService.buscarPorUsuarioAutenticado();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<DayDetailResponseDTO> buscarPorId(@PathVariable UUID id) {
        DayDetailResponseDTO response = dayService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }



}
