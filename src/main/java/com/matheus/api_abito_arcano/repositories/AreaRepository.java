package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {

    Optional<Area> findByName(String name);
    List<Area> findByUserId(UUID userId);
    Optional<Area> findByNameAndUserId(String name, UUID userId);
    Optional<Area> findByIdAndUserId(UUID id, UUID userId);

}
