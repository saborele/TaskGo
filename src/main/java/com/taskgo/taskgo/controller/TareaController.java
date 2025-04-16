package com.taskgo.taskgo.controller;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState; // Importar el enum correcto
import com.taskgo.taskgo.config.NavigationSession;
import com.taskgo.taskgo.model.Tarea;
import com.taskgo.taskgo.repository.TareaRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TareaController {
    private final TareaRepository tareaRepository;
    private final Map<String, NavigationSession> navigationSessions = new HashMap<>();

    public TareaController(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
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
        tarea.setFavorita(false); // Aseguramos que favorita sea false por defecto
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

    @GetMapping("/navegar")
    public String navegar(
            @RequestParam(defaultValue = "https://www.google.com") String url,
            @RequestParam(defaultValue = "noticias") String query,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false, defaultValue = "false") boolean continueAfterIntervention) {
        // Validación básica de parámetros
        if (url == null || url.trim().isEmpty()) {
            return "Error: La URL no puede estar vacía.";
        }
        if (query == null || query.trim().isEmpty()) {
            return "Error: El término de búsqueda no puede estar vacío.";
        }

        NavigationSession session;
        String currentSessionId;

        if (continueAfterIntervention && sessionId != null) {
            // Continuar con una sesión existente
            session = navigationSessions.get(sessionId);
            if (session == null) {
                return "Error: Sesión no encontrada. Por favor, inicia una nueva navegación.";
            }
            currentSessionId = sessionId;
        } else {
            // Crear una nueva sesión
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false) // Modo visible para que el usuario pueda interactuar
                    .setSlowMo(100));   // Retraso por acción

            Page page = browser.newPage();
            page.setExtraHTTPHeaders(java.util.Map.of(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                    "Accept-Language", "es-ES,es;q=0.9"
            ));

            // Listener para pop-ups dinámicos (alertas, confirmaciones)
            page.onDialog(dialog -> {
                System.out.println("Pop-up dinámico detectado: " + dialog.message());
                // No aceptamos automáticamente, dejamos que el usuario lo maneje
            });

            // Navegar a la URL
            try {
                page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            } catch (Exception e) {
                browser.close();
                playwright.close();
                return "Error al navegar a la URL: " + e.getMessage();
            }

            // Esperar a que la página esté lista
            page.waitForLoadState(LoadState.NETWORKIDLE);

            currentSessionId = UUID.randomUUID().toString();
            session = new NavigationSession(playwright, browser, page, url, query);
            navigationSessions.put(currentSessionId, session);
        }

        Page page = session.getPage();

        try {
            // Detectar pop-ups o CAPTCHAs
            boolean userInterventionRequired = false;
            String interventionMessage = null;

            if (!continueAfterIntervention) {
                // Detectar pop-up de Google (o cualquier pop-up)
                if (detectPopup(page)) {
                    userInterventionRequired = true;
                    interventionMessage = "Pop-up detectado. Por favor, resuélvelo manualmente (por ejemplo, haz clic en 'Aceptar todo' o 'Rechazar todo') y espera.";
                }

                // Detectar CAPTCHA
                if (page.querySelector("div#recaptcha") != null || page.querySelector("iframe[src*='recaptcha']") != null) {
                    userInterventionRequired = true;
                    interventionMessage = "CAPTCHA detectado. Por favor, resuélvelo manualmente y espera.";
                }

                if (userInterventionRequired) {
                    // Devolver un mensaje especial para que el frontend lo maneje
                    return "USER_INTERVENTION_REQUIRED:" + interventionMessage + "|SESSION_ID:" + currentSessionId;
                }
            }

            // Intentar encontrar el campo de búsqueda de forma dinámica
            String searchFieldSelector = findSearchField(page);
            if (searchFieldSelector == null) {
                // No cerrar el navegador, permitir al usuario continuar manualmente
                return "USER_INTERVENTION_REQUIRED:No se encontró un campo de búsqueda en la página. Por favor, ingresa el término de búsqueda manualmente y espera.|SESSION_ID:" + currentSessionId;
            }

            // Esperar a que el campo esté visible y rellenarlo
            try {
                System.out.println("Esperando a que el campo de búsqueda sea visible. Selector: " + searchFieldSelector);
                Page.WaitForSelectorOptions options = new Page.WaitForSelectorOptions();
                options.timeout = 15000.0; // Tiempo de espera en milisegundos (15 segundos)
                options.state = WaitForSelectorState.VISIBLE; // Usar el enum correcto
                page.waitForSelector(searchFieldSelector, options);

                ElementHandle searchField = page.querySelector(searchFieldSelector);
                if (searchField == null || !searchField.isVisible() || searchField.isDisabled()) {
                    System.out.println("El campo de búsqueda no es interactuable (no visible o deshabilitado). Selector: " + searchFieldSelector);
                    return "USER_INTERVENTION_REQUIRED:El campo de búsqueda no es interactuable. Por favor, ingresa el término de búsqueda manualmente y espera.|SESSION_ID:" + currentSessionId;
                }
                System.out.println("Campo de búsqueda visible y listo para interactuar. Selector: " + searchFieldSelector);
                page.fill(searchFieldSelector, query);
            } catch (Exception e) {
                System.out.println("Error al esperar o rellenar el campo de búsqueda: " + e.getMessage());
                return "USER_INTERVENTION_REQUIRED:No se pudo interactuar con el campo de búsqueda. Por favor, ingresa el término de búsqueda manualmente y espera.|SESSION_ID:" + currentSessionId;
            }

            // Simular interacción humana
            page.mouse().move(100, 100); // Mover el mouse
            page.evaluate("window.scrollTo(0, 200)"); // Desplazamiento suave
            Thread.sleep(new Random().nextInt(500) + 500); // Retraso aleatorio
            page.press(searchFieldSelector, "Enter");

            // Esperar a que los resultados carguen
            page.waitForLoadState(LoadState.NETWORKIDLE);

            String result = page.title();
            navigationSessions.remove(currentSessionId);
            session.close();
            return result;
        } catch (Exception e) {
            navigationSessions.remove(currentSessionId);
            session.close();
            return "Error al navegar: " + e.getMessage() + " (Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "Desconocida") + ")";
        }
    }

    // Método auxiliar para detectar pop-ups
    private boolean detectPopup(Page page) {
        String[] popupSelectors = {
                "button[id*='accept']",
                "button[class*='accept']",
                "button[id*='cookie']",
                "button[class*='cookie']",
                "button[aria-label*='accept']",
                "button[aria-label*='Aceptar']",
                "button[id*='agree']",
                "button[class*='agree']",
                "button[id*='ok']",
                "button[class*='ok']",
                "button[id*='close']",
                "button[class*='close']",
                "button[aria-label*='close']",
                "button[aria-label*='Cerrar']"
        };

        String[] popupButtonTexts = {
                "Aceptar todo",
                "Accept all",
                "Aceptar",
                "Accept",
                "OK",
                "Agree",
                "Cerrar",
                "Close",
                "I Understand",
                "Got it"
        };

        for (String selector : popupSelectors) {
            if (page.querySelector(selector) != null) {
                System.out.println("Pop-up detectado con selector CSS: " + selector);
                return true;
            }
        }

        List<ElementHandle> buttons = page.querySelectorAll("button");
        for (ElementHandle button : buttons) {
            String buttonText = button.innerText().trim();
            for (String text : popupButtonTexts) {
                if (buttonText.equalsIgnoreCase(text)) {
                    System.out.println("Pop-up detectado con texto: " + text);
                    return true;
                }
            }
        }

        for (Frame frame : page.frames()) {
            for (String selector : popupSelectors) {
                if (frame.querySelector(selector) != null) {
                    System.out.println("Pop-up en iframe detectado con selector CSS: " + selector);
                    return true;
                }
            }

            List<ElementHandle> frameButtons = frame.querySelectorAll("button");
            for (ElementHandle button : frameButtons) {
                String buttonText = button.innerText().trim();
                for (String text : popupButtonTexts) {
                    if (buttonText.equalsIgnoreCase(text)) {
                        System.out.println("Pop-up en iframe detectado con texto: " + text);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Método auxiliar para encontrar el campo de búsqueda dinámicamente
    private String findSearchField(Page page) {
        String[] possibleSelectors = {
                "input[name='q']",
                "input[name='s']",
                "input[name='search']",
                "input[id='search']",
                "input[id='q']",
                "input[type='text']",
                "input[type='search']",
                "input[placeholder*='search']",
                "input[placeholder*='buscar']",
                "input[placeholder*='Buscar']",
                "input[aria-label*='search']",
                "input[aria-label*='buscar']",
                "input[aria-label*='Buscar']",
                "input[class*='search']",
                "input[id*='query']",
                "input[name*='query']"
        };

        for (String selector : possibleSelectors) {
            try {
                ElementHandle element = page.querySelector(selector);
                if (element != null && element.isVisible()) {
                    System.out.println("Campo de búsqueda encontrado con selector: " + selector);
                    return selector;
                } else {
                    System.out.println("Selector probado pero no encontrado o no visible: " + selector);
                }
            } catch (Exception e) {
                System.out.println("Error al probar selector " + selector + ": " + e.getMessage());
            }
        }

        for (Frame frame : page.frames()) {
            for (String selector : possibleSelectors) {
                try {
                    ElementHandle element = frame.querySelector(selector);
                    if (element != null && element.isVisible()) {
                        System.out.println("Campo de búsqueda encontrado en iframe con selector: " + selector);
                        return selector;
                    } else {
                        System.out.println("Selector probado en iframe pero no encontrado o no visible: " + selector);
                    }
                } catch (Exception e) {
                    System.out.println("Error al probar selector en iframe " + selector + ": " + e.getMessage());
                }
            }
        }

        try {
            page.waitForFunction(
                    "() => document.querySelector('input[type=\"text\"]:not([type=\"hidden\"])') || document.querySelector('input[type=\"search\"]:not([type=\"hidden\"])')",
                    new Page.WaitForFunctionOptions().setTimeout(10000)
            );
            ElementHandle element = page.querySelector("input[type='text']:not([type='hidden']), input[type='search']:not([type='hidden'])");
            if (element != null && element.isVisible()) {
                System.out.println("Campo de búsqueda encontrado con JavaScript dinámico.");
                return "input[type='text']:not([type='hidden']), input[type='search']:not([type='hidden'])";
            }
        } catch (Exception e) {
            System.out.println("Error al buscar con JavaScript dinámico: " + e.getMessage());
        }

        System.out.println("No se encontró ningún campo de búsqueda en la página.");
        return null;
    }
}