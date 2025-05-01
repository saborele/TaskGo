package com.taskgo.taskgo.controller;

import com.taskgo.taskgo.model.Tarea;
import com.taskgo.taskgo.repository.TareaRepository;
import com.taskgo.taskgo.service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TareaController {
    private final TareaRepository tareaRepository;
    private final CommandService commandService;

    @Autowired
    public TareaController(TareaRepository tareaRepository, CommandService commandService) {
        this.tareaRepository = tareaRepository;
        this.commandService = commandService;
    }

    @GetMapping("/tareas")
    public List<Tarea> getTareas() {
        return tareaRepository.findAll();
    }

    @PostMapping("/tareas")
    public Tarea createTarea(@RequestBody Tarea tarea) {
        if (tarea.getDescripcion() == null || tarea.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la tarea no puede estar vacía.");
        }
        tarea.setFecha(String.valueOf(LocalDateTime.now()));
        tarea.setCompletada(false);
        tarea.setFavorita(false);
        return tareaRepository.save(tarea);
    }

    @PutMapping("/tareas/{id}")
    public String updateTarea(@PathVariable Long id, @RequestBody Tarea updatedTarea) {
        try {
            Tarea tarea = tareaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tarea con ID " + id + " no encontrada."));
            if (updatedTarea.getDescripcion() == null || updatedTarea.getDescripcion().trim().isEmpty()) {
                return "Error: La descripción no puede estar vacía.";
            }
            tarea.setDescripcion(updatedTarea.getDescripcion());
            tareaRepository.save(tarea);
            return "Tarea con ID " + id + " actualizada.";
        } catch (Exception e) {
            return "Error al actualizar la tarea: " + e.getMessage();
        }
    }

    @PutMapping("/tareas/{id}/toggle")
    public String toggleTarea(@PathVariable Long id) {
        try {
            Tarea tarea = tareaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tarea con ID " + id + " no encontrada."));
            tarea.setCompletada(!tarea.isCompletada());
            tareaRepository.save(tarea);
            return "Estado de la tarea con ID " + id + " cambiado a " + (tarea.isCompletada() ? "completada" : "no completada") + ".";
        } catch (Exception e) {
            return "Error al cambiar el estado de la tarea: " + e.getMessage();
        }
    }

    @PutMapping("/tareas/{id}/favorita")
    public String toggleFavorita(@PathVariable Long id) {
        try {
            Tarea tarea = tareaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tarea con ID " + id + " no encontrada."));
            tarea.setFavorita(!tarea.isFavorita());
            tareaRepository.save(tarea);
            return "Tarea con ID " + id + " marcada como " + (tarea.isFavorita() ? "favorita" : "no favorita") + ".";
        } catch (Exception e) {
            return "Error al marcar la tarea como favorita: " + e.getMessage();
        }
    }

    @PutMapping("/tareas/{id}/completar")
    public String completeTarea(@PathVariable Long id) {
        try {
            Tarea tarea = tareaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tarea con ID " + id + " no encontrada."));
            if (tarea.isCompletada()) {
                return "La tarea con ID " + id + " ya está completada.";
            }
            tarea.setCompletada(true);
            tareaRepository.save(tarea);
            return "Tarea con ID " + id + " marcada como completada.";
        } catch (Exception e) {
            return "Error al completar la tarea: " + e.getMessage();
        }
    }

    @PutMapping("/tareas/completar-todas")
    public String completeAllTareas() {
        try {
            List<Tarea> pendingTareas = tareaRepository.findAll().stream()
                    .filter(tarea -> !tarea.isCompletada())
                    .collect(Collectors.toList());
            pendingTareas.forEach(tarea -> tarea.setCompletada(true));
            tareaRepository.saveAll(pendingTareas);
            return "Todas las tareas pendientes marcadas como completadas (" + pendingTareas.size() + " tareas).";
        } catch (Exception e) {
            return "Error al marcar todas las tareas como completadas: " + e.getMessage();
        }
    }

    @DeleteMapping("/tareas/completadas")
    public String deleteCompletedTareas() {
        try {
            List<Tarea> completedTareas = tareaRepository.findAll().stream()
                    .filter(Tarea::isCompletada)
                    .collect(Collectors.toList());
            tareaRepository.deleteAll(completedTareas);
            return "Tareas completadas eliminadas (" + completedTareas.size() + " tareas).";
        } catch (Exception e) {
            return "Error al eliminar tareas completadas: " + e.getMessage();
        }
    }

    @DeleteMapping("/tareas/{id}")
    public String deleteTarea(@PathVariable Long id) {
        try {
            Tarea tarea = tareaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tarea con ID " + id + " no encontrada."));
            tareaRepository.delete(tarea);
            return "Tarea con ID " + id + " eliminada.";
        } catch (Exception e) {
            return "Error al eliminar la tarea: " + e.getMessage();
        }
    }

    @PostMapping("/execute")
    public String executeCommand(@RequestBody String command) {
        return commandService.executeCommand(command);
    }
}