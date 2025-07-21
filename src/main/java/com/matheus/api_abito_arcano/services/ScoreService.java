package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.response.ScoreResponseDTO;
import com.matheus.api_abito_arcano.models.*;
import com.matheus.api_abito_arcano.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private DayRepository dayRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private SubareaRepository subareaRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ScoreResponseDTO processarPontuacao(UUID tarefaId, UUID dayId, boolean concluir, User user) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId).orElseThrow();
        Day dia = dayRepository.findById(dayId).orElseThrow();

        Optional<Score> scoreOptional = (tarefa.getSubarea() != null)
                ? scoreRepository.findByDay_IdAndArea_IdAndSubarea_IdAndUser_Id(dayId, tarefa.getArea().getId(), tarefa.getSubarea().getId(), user.getId())
                : scoreRepository.findByDay_IdAndArea_IdAndUser_IdAndSubareaIsNull(dayId, tarefa.getArea().getId(), user.getId());


        Score score = scoreOptional.orElseGet(() -> {
            Score novo = new Score();
            novo.setDay(dia);
            novo.setArea(tarefa.getArea());
            novo.setSubarea(tarefa.getSubarea());
            novo.setUser(user);
            novo.setScore(0);
            return novo;
        });

        int novoValor = score.getScore() + (concluir ? tarefa.getScore() : -tarefa.getScore());
        score.setScore(Math.max(0, novoValor));
        Score salvo = scoreRepository.save(score);

        return new ScoreResponseDTO(
                salvo.getId(),
                salvo.getArea().getId(),
                salvo.getArea().getName(),
                salvo.getSubarea() != null ? salvo.getSubarea().getId() : null,
                salvo.getSubarea() != null ? salvo.getSubarea().getName() : null,
                salvo.getDay().getDate(),
                salvo.getScore()
        );

    }


    @Transactional(readOnly = true)
    public List<ScoreResponseDTO> listarScoresPorUsuario() {
        User user = userService.getUsuarioAutenticado();
        List<Score> scores = scoreRepository.findAllByUser_Id(user.getId());

        return scores.stream().map(score -> new ScoreResponseDTO(
                score.getId(),
                score.getArea().getId(),
                score.getArea().getName(),
                score.getSubarea() != null ? score.getSubarea().getId() : null,
                score.getSubarea() != null ? score.getSubarea().getName() : null,
                score.getDay().getDate(),
                score.getScore()
        )).collect(Collectors.toList());
    }

}
