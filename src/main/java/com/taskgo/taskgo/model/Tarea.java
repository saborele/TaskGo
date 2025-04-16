package com.taskgo.taskgo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Tarea {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String descripcion;
    private String fecha;
    private boolean completada;
    private boolean favorita;

    // Constructor vacío (requerido por JPA)
    public Tarea() {
        this.completada = false; // Inicializamos completada a false por defecto
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

    public boolean isCompletada() {
        return completada;
    }

    public boolean setCompletada(boolean b) {
        return completada=b;
    }

    public boolean isFavorita() {
        return true;
    }

    public void setFavorita(boolean b) {
            this.favorita=true;
    }
}