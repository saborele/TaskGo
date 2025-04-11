package com.taskgo.taskgo.controller;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.taskgo.taskgo.model.Tarea;
import com.taskgo.taskgo.repository.TareaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

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
    public String navegar(@RequestParam(defaultValue = "https://www.google.com") String url,
                          @RequestParam(defaultValue = "noticias") String query) {
        try (Playwright playwright = Playwright.create()) {
            // Configurar el navegador para parecer más humano
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false) // Modo visible
                    .setSlowMo(100));   // Retraso por acción

            Page page = browser.newPage();
            page.setExtraHTTPHeaders(java.util.Map.of("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"));

            // Navegar a la URL
            page.navigate(url);

            // Esperar a que la página esté lista
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            // Intentar encontrar el campo de búsqueda de forma dinámica
            String searchFieldSelector = findSearchField(page);
            if (searchFieldSelector == null) {
                return "No se encontró un campo de búsqueda en la página.";
            }

            // Esperar a que el campo esté visible y rellenarlo
            page.waitForSelector(searchFieldSelector, new Page.WaitForSelectorOptions().setTimeout(10000));
            page.fill(searchFieldSelector, query);

            // Simular interacción humana
            page.mouse().move(100, 100); // Mover el mouse
            Thread.sleep(new Random().nextInt(500) + 500); // Retraso aleatorio
            page.press(searchFieldSelector, "Enter");

            // Verificar CAPTCHA
            if (page.querySelector("div#recaptcha") != null) {
                return "CAPTCHA detectado. Resuélvelo manualmente o usa un servicio como 2Captcha.";
            }

            // Esperar a que los resultados carguen
            page.waitForLoadState(LoadState.NETWORKIDLE);

            return page.title();
        } catch (Exception e) {
            return "Error al navegar: " + e.getMessage();
        }
    }

    // Método auxiliar para encontrar el campo de búsqueda dinámicamente
    private String findSearchField(Page page) {
        // Lista de selectores comunes para campos de búsqueda
        String[] possibleSelectors = {
                "input[name='q']",              // Google
                "input[type='text']",           // Input genérico
                "input[type='search']",         // Tipo búsqueda
                "input[placeholder*='search']", // Placeholder con "search" (insensible a mayúsculas)
                "input[placeholder*='buscar']", // Para sitios en español
                "input[id='search']",           // ID común
                "input[name='search']"          // Name común
        };

        // Probar cada selector
        for (String selector : possibleSelectors) {
            if (page.querySelector(selector) != null) {
                return selector;
            }
        }

        // Si no encuentra nada, intentar con JavaScript dinámico
        try {
            page.waitForFunction(
                    "() => document.querySelector('input[type=\"text\"]') || document.querySelector('input[type=\"search\"]')",
                    new Page.WaitForFunctionOptions().setTimeout(5000)
            );
            return "input[type='text'], input[type='search']"; // Selector combinado
        } catch (Exception e) {
            return null; // No se encontró ningún campo
        }
    }
}