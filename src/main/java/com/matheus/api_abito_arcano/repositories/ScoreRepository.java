package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {

    List<Score> findByDay_Date(LocalDate date);

    List<Score> findByArea_Id(UUID areaId);

    List<Score> findBySubarea_Id(UUID subareaId);
}
