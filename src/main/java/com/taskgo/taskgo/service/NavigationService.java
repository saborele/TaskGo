package com.taskgo.taskgo.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class NavigationService {
    private WebDriver driver;

    public String navegar(String url, String query) {
        try {
            // Configurar Chrome para evitar detección de bots
            ChromeOptions options = new ChromeOptions();
            options.addArguments("user-data-dir=/path/to/user/profile"); // Reemplaza con la ruta real del perfil de Chrome del usuario
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled"); // Oculta que es un navegador automatizado
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            // Iniciar el navegador
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            // Ocultar la firma de Selenium (webdriver)
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            // Navegar a la URL
            driver.get(url);

            // Simular comportamiento humano: desplazamiento y movimiento del mouse
            Actions actions = new Actions(driver);
            actions.moveByOffset(100, 100).perform();
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 200)");
            Thread.sleep(new Random().nextInt(500) + 500);

            // Manejar pop-ups de cookies
            try {
                WebElement acceptButton = findAcceptButton();
                if (acceptButton != null && acceptButton.isDisplayed()) {
                    actions.moveToElement(acceptButton).pause(new Random().nextInt(500) + 300).click().perform();
                    Thread.sleep(new Random().nextInt(500) + 500);
                }
            } catch (Exception e) {
                System.out.println("No se encontró pop-up de cookies o no se pudo interactuar: " + e.getMessage());
            }

            // Buscar el campo de búsqueda
            WebElement searchField = findSearchField();
            if (searchField == null) {
                return "USER_INTERVENTION_REQUIRED:No se encontró un campo de búsqueda. Por favor, ingresa el término de búsqueda manualmente y espera.";
            }

            // Simular interacción humana al rellenar el campo
            actions.moveToElement(searchField).pause(new Random().nextInt(500) + 300).click().perform();
            searchField.sendKeys(query);
            Thread.sleep(new Random().nextInt(500) + 500);

            // Enviar la búsqueda
            searchField.submit();

            // Esperar a que los resultados carguen y simular más comportamiento humano
            Thread.sleep(new Random().nextInt(1000) + 1000);
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 400)");
            actions.moveByOffset(50, 50).perform();

            // Obtener el título de la página
            String result = driver.getTitle();
            return result;
        } catch (Exception e) {
            return "Error al navegar: " + e.getMessage();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private WebElement findAcceptButton() {
        String[] acceptSelectors = {
                "//button[contains(text(), 'Aceptar') or contains(text(), 'Accept') or contains(text(), 'Aceptar todo') or contains(text(), 'Accept all')]",
                "//button[contains(text(), 'OK') or contains(text(), 'Agree') or contains(text(), 'I Understand') or contains(text(), 'Got it')]",
                "//button[@id='accept' or @class='accept' or @id='cookie-accept' or @class='cookie-accept']",
                "//button[contains(@aria-label, 'accept') or contains(@aria-label, 'Aceptar') or contains(@aria-label, 'close') or contains(@aria-label, 'Cerrar')]"
        };

        for (String selector : acceptSelectors) {
            try {
                WebElement element = driver.findElement(By.xpath(selector));
                if (element != null && element.isDisplayed()) {
                    return element;
                }
            } catch (Exception e) {
                // Ignorar y probar el siguiente selector
            }
        }
        return null;
    }

    private WebElement findSearchField() {
        String[] searchSelectors = {
                "input[name='q']",
                "input[name='search']",
                "input[type='search']",
                "input[id='search']",
                "input[placeholder*='search']",
                "input[placeholder*='buscar']",
                "input[placeholder*='Buscar']",
                "input[aria-label*='search']",
                "input[aria-label*='buscar']",
                "input[aria-label*='Buscar']"
        };

        for (String selector : searchSelectors) {
            try {
                WebElement element = driver.findElement(By.cssSelector(selector));
                if (element != null && element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
            } catch (Exception e) {
                // Ignorar y probar el siguiente selector
            }
        }
        return null;
    }
}