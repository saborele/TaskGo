package com.taskgo.taskgo.service;

import org.springframework.stereotype.Service;

@Service
public class TikTokService {
    private final DisplayService displayService;

    public TikTokService(DisplayService displayService) {
        this.displayService = displayService;
    }

    public String getRecentVideos(String username, int count) {
        // Simulación: en el futuro, usaremos TikTokApi o una API real
        String[] simulatedVideos = {
                "https://www.tiktok.com/@user/video/1",
                "https://www.tiktok.com/@user/video/2",
                "https://www.tiktok.com/@user/video/3",
                "https://www.tiktok.com/@user/video/4",
                "https://www.tiktok.com/@user/video/5"
        };

        // Mostrar los videos al usuario
        StringBuilder result = new StringBuilder();
        result.append("Últimos ").append(count).append(" videos de ").append(username).append(" en TikTok:\n");
        for (int i = 0; i < Math.min(count, simulatedVideos.length); i++) {
            result.append("Video ").append(i + 1).append(": ").append(simulatedVideos[i]).append("\n");
            displayService.showVideo(simulatedVideos[i], i + 1, count);
        }
        return result.toString();
    }
}