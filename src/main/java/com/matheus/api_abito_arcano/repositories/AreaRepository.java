package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {

    Optional<Area> findByNome(String nome);
}
