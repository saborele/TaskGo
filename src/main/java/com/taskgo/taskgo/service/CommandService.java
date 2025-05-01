package com.taskgo.taskgo.service;

import org.springframework.stereotype.Service;

@Service
public class CommandService {
    private final TikTokService tikTokService;

    public CommandService(TikTokService tikTokService) {
        this.tikTokService = tikTokService;
    }

    public String executeCommand(String command) {
        // Interpretar el comando (esto será más avanzado en el futuro con NLP)
        if (command.toLowerCase().contains("tiktok") && command.toLowerCase().contains("ultimos 5 videos")) {
            // Extraer el nombre de usuario del comando
            String username = extractUsername(command);
            if (username == null) {
                return "Error: No se especificó un usuario. Usa un formato como 'muestra los últimos 5 videos del usuario X en TikTok'.";
            }

            // Obtener los videos del usuario
            String result = tikTokService.getRecentVideos(username, 5);
            return result;
        }
        return "Error: Comando no reconocido. Ejemplo: 'muestra los últimos 5 videos del usuario X en TikTok'.";
    }

    private String extractUsername(String command) {
        // Buscar un patrón como "del usuario X"
        String[] parts = command.split("del usuario");
        if (parts.length < 2) {
            return null;
        }
        return parts[1].trim().split(" ")[0]; // Obtener el nombre de usuario
    }
}