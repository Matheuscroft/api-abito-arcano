// CompletedTaskController.java
package com.matheus.api_abito_arcano.controllers;

import com.matheus.api_abito_arcano.dtos.ScoreDTO;
import com.matheus.api_abito_arcano.dtos.response.CompletedTaskWithScoreDTO;
import com.matheus.api_abito_arcano.services.CompletedTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/completed")
public class CompletedTaskController {

    private static final Logger log = LoggerFactory.getLogger(CompletedTaskController.class);
    @Autowired
    private CompletedTaskService completedTaskService;

    @PostMapping("/check")
    public ResponseEntity<CompletedTaskWithScoreDTO> check(@RequestBody ScoreDTO dto) {
        log.info("[Controller] Recebido check para tarefa {}", dto.tarefaId());
        return ResponseEntity.ok(completedTaskService.checkTarefa(dto.tarefaId(), dto.dayId()));
    }

    @PostMapping("/uncheck")
    public ResponseEntity<CompletedTaskWithScoreDTO> uncheck(@RequestBody ScoreDTO dto) {
        return ResponseEntity.ok(completedTaskService.uncheckTarefa(dto.tarefaId(), dto.dayId()));
    }
}
