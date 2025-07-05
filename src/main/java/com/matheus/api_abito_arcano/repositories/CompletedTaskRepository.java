package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.CompletedTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompletedTaskRepository extends JpaRepository<CompletedTask, UUID> {

    List<CompletedTask> findByDay_Date(LocalDate date);

    List<CompletedTask> findByTarefa_Id(UUID tarefaId);

    List<CompletedTask> findByDay_Id(UUID dayId);

    Optional<CompletedTask> findByTarefa_IdAndDay_Id(UUID tarefaId, UUID dayId);

}
