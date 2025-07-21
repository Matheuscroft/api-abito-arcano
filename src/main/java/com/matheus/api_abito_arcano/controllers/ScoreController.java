package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.response.ScoreResponseDTO;
import com.matheus.api_abito_arcano.services.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @GetMapping
    public ResponseEntity<List<ScoreResponseDTO>> listarScoresDoUsuario() {

        List<ScoreResponseDTO> scores = scoreService.listarScoresPorUsuario();
        return ResponseEntity.ok(scores);
    }
}
