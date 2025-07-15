package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.CompletedTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompletedTaskRepository extends JpaRepository<CompletedTask, UUID> {

    List<CompletedTask> findByDay_Date(LocalDate date);

    List<CompletedTask> findByTarefa_Id(UUID tarefaId);

    List<CompletedTask> findByDay_Id(UUID dayId);

    Optional<CompletedTask> findByTarefa_IdAndDay_Id(UUID tarefaId, UUID dayId);

    List<CompletedTask> findAllByTarefa_IdAndDay_DateGreaterThanEqual(UUID tarefaId, LocalDate date);

    boolean existsByTarefa_Id(UUID tarefaId);

    @Query("SELECT ct FROM CompletedTask ct JOIN ct.day d WHERE ct.tarefa.id = :tarefaId AND d.date >= :date")
    List<CompletedTask> findAllByTarefaIdAndFromDate(@Param("tarefaId") UUID tarefaId, @Param("date") LocalDate date);


}
