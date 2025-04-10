package com.taskgo.taskgo.repository;

import com.taskgo.taskgo.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
}