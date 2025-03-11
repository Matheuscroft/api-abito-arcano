package com.matheus.api_abito_arcano.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "TB_AREA")
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String nome;
    private String cor;

    @OneToMany(mappedBy = "area")
    @JsonManagedReference
    private List<Subarea> subareas;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public List<Subarea> getSubareas() {
        return subareas;
    }

    public void setSubareas(List<Subarea> subareas) {
        this.subareas = subareas;
    }
}
