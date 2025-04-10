package com.taskgo.taskgo.controller;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.taskgo.taskgo.model.Tarea;
import com.taskgo.taskgo.repository.TareaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TareaController {
    private final TareaRepository tareaRepository;

    public TareaController(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    @GetMapping("/tareas")
    public List<Tarea> getTareas() {
        return tareaRepository.findAll();
    }

    @PostMapping("/tareas")
    public Tarea createTarea(@RequestBody Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    @GetMapping("/navegar")
    public String navegar() throws Exception {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate("https://www.google.com");
            page.fill("input[name='q']", "noticias");
            page.press("input[name='q']", "Enter");
            return page.title();
        }
    }
}