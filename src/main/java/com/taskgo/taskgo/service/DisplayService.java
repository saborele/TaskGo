package com.taskgo.taskgo.service;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class DisplayService {
    private WebDriver driver;

    public void showVideo(String videoUrl, int currentVideo, int totalVideos) {
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("user-data-dir=/path/to/user/profile"); // Reemplaza con la ruta real
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            driver = new ChromeDriver(options);
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            // Mostrar un mensaje inicial
            driver.get("about:blank");
            ((JavascriptExecutor) driver).executeScript(
                    "document.body.innerHTML = '<h1>Mostrando video " + currentVideo + " de " + totalVideos + "</h1>';"
            );
            Thread.sleep(2000);

            // Abrir el video
            driver.get(videoUrl);
            Thread.sleep(5000); // Simular que el usuario está viendo el video
        } catch (Exception e) {
            System.out.println("Error al mostrar video: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}