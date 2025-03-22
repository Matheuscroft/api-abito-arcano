package com.matheus.api_abito_arcano.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "TB_TAREFA")
@Data
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String titulo;
    private int pontuacao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable( name = "dias_semana", joinColumns = @JoinColumn(name = "tarefa_id") )
    @Column(name = "dia", columnDefinition = "INTEGER")
    private List<Integer> diasSemana;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "id")
    private Area area;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subarea_id", referencedColumnName = "id")
    private Subarea subarea;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }

    public List<Integer> getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(List<Integer> diasSemana) {
        this.diasSemana = diasSemana;
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
}