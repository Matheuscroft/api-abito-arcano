package com.matheus.api_abito_arcano.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "TB_TAREFA")
@Data
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;
    private int score;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable( name = "days_of_the_week", joinColumns = @JoinColumn(name = "tarefa_id") )
    @Column(name = "day", columnDefinition = "INTEGER")
    private List<Integer> daysOfTheWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "id")
    private Area area;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subarea_id", referencedColumnName = "id")
    private Subarea subarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_task_id")
    private Tarefa originalTask;

    @Column(name = "is_latest_version")
    private boolean latestVersion = true;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<Integer> getDaysOfTheWeek() {
        return daysOfTheWeek;
    }

    public void setDaysOfTheWeek(List<Integer> daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Subarea getSubarea() {
        return subarea;
    }

    public void setSubarea(Subarea subarea) {
        this.subarea = subarea;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Tarefa getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(Tarefa originalTask) {
        this.originalTask = originalTask;
    }

    public boolean isLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(boolean latestVersion) {
        this.latestVersion = latestVersion;
    }
}