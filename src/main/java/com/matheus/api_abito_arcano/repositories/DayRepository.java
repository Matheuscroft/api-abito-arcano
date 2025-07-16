package com.matheus.api_abito_arcano.repositories;

import com.matheus.api_abito_arcano.models.Day;
import com.matheus.api_abito_arcano.models.Tarefa;
import com.matheus.api_abito_arcano.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DayRepository extends JpaRepository<Day, UUID> {

    List<Day> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    Day findTopByUserIdOrderByDateDesc(UUID userId);
    Day findByUserIdAndDate(UUID userId, LocalDate date);

    @Query("""
    SELECT d FROM Day d
    LEFT JOIN FETCH d.tarefasPrevistas
    LEFT JOIN FETCH d.completedTasks
    WHERE d.id = :dayId
""")
    Optional<Day> findByIdWithTarefasAndCompletedTasks(@Param("dayId") UUID dayId);

    @Query("SELECT d FROM Day d LEFT JOIN FETCH d.tarefasPrevistas WHERE d.user.id = :userId AND d.date >= :data ORDER BY d.date")
    List<Day> findAllByUserIdAndDateGreaterThanEqualWithTarefas(@Param("userId") UUID userId, @Param("data") LocalDate data);

    @Query("SELECT d FROM Day d LEFT JOIN FETCH d.completedTasks WHERE d.user.id = :userId AND d.date >= :data ORDER BY d.date")
    List<Day> findAllByUserIdAndDateGreaterThanEqualWithCompletedTasks(@Param("userId") UUID userId, @Param("data") LocalDate data);


    Optional<Day> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndTarefasPrevistasContaining(UUID userId, Tarefa tarefa);

    List<Day> findAllByUserId(UUID userId);

    @Query("SELECT d FROM Day d JOIN d.tarefasPrevistas t WHERE t.id = :tarefaId AND d.user.id = :userId")
    Day findByTarefaIdAndUserId(@Param("tarefaId") UUID tarefaId, @Param("userId") UUID userId);

    @Query("SELECT d FROM Day d LEFT JOIN FETCH d.tarefasPrevistas WHERE d.user.id = :userId AND d.date < :cutoffDate")
    List<Day> findAllByUserIdAndDateLessThanWithTarefas(@Param("userId") UUID userId, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("""
    SELECT DISTINCT d FROM Day d 
    LEFT JOIN FETCH d.tarefasPrevistas 
    WHERE d.user.id = :userId 
      AND :tarefa MEMBER OF d.tarefasPrevistas 
      AND d.date >= :data
""")
    List<Day> findAllByUserIdAndTarefaWithTarefasFromDate(
            @Param("userId") UUID userId,
            @Param("tarefa") Tarefa tarefa,
            @Param("data") LocalDate data
    );

    LocalDate findDateById(UUID dayId);

}