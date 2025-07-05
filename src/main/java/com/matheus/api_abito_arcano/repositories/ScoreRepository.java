package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {

    List<Score> findByDay_Date(LocalDate date);

    List<Score> findByArea_Id(UUID areaId);

    List<Score> findBySubarea_Id(UUID subareaId);

    Optional<Score> findByDay_IdAndArea_IdAndSubarea_IdAndUser_Id(
            UUID dayId,
            UUID areaId,
            UUID subareaId,
            UUID userId
    );

    Optional<Score> findByDay_IdAndArea_IdAndUser_IdAndSubareaIsNull(
            UUID dayId,
            UUID areaId,
            UUID userId
    );


}
