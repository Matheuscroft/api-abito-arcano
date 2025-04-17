package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Subarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface SubareaRepository extends JpaRepository<Subarea, UUID> {

    List<Subarea> findByAreaId(UUID areaId);

    @Query("SELECT s FROM Subarea s WHERE s.area.user.id = :userId")
    List<Subarea> findByUserId(@Param("userId") UUID userId);

    Optional<Subarea> findByIdAndArea_User_Id(UUID subareaId, UUID userId);

}
