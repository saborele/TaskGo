package com.taskgo.taskgo.service;

import org.springframework.stereotype.Service;

@Service
public class TikTokService {
    private final NavigationService navigationService;
    private final DisplayService displayService;

    public TikTokService(NavigationService navigationService, DisplayService displayService) {
        this.navigationService = navigationService;
        this.displayService = displayService;
    }

    public String getRecentVideos(String username, int count) {
        try {
            // Usar NavigationService para buscar videos del usuario en TikTok
            String url = "https://www.tiktok.com";
            String query = username;
            String result = navigationService.navegar(url, query);

            // Simular la obtención de videos (en una implementación real, extraerías los enlaces de la página)
            String[] simulatedVideos = new String[count];
            for (int i = 0; i < count; i++) {
                simulatedVideos[i] = "https://www.tiktok.com/@" + username + "/video/" + (i + 1);
            }

            // Mostrar los videos al usuario
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("Últimos ").append(count).append(" videos de ").append(username).append(" en TikTok:\n");
            for (int i = 0; i < simulatedVideos.length; i++) {
                resultBuilder.append("Video ").append(i + 1).append(": ").append(simulatedVideos[i]).append("\n");
                displayService.showVideo(simulatedVideos[i], i + 1, count);
            }
            return resultBuilder.toString();
        } catch (Exception e) {
            return "Error al obtener videos de TikTok: " + e.getMessage();
        }
    }
}