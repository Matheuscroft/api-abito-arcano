package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Subarea;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface SubareaRepository extends JpaRepository<Subarea, UUID> {

    List<Subarea> findByAreaId(UUID areaId);
}
