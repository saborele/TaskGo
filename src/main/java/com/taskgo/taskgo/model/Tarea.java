package com.taskgo.taskgo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Tarea {
    @Id
    private Long id;
    private String descripcion;
    private String fecha;

    // Constructor vacío (requerido por JPA)
    public Tarea() {
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}