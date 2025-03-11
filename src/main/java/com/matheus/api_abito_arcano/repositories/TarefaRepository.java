package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository extends JpaRepository<Tarefa, UUID> {

    Optional<Tarefa> findById(UUID id);
}
