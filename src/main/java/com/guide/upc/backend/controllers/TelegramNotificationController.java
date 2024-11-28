package com.guide.upc.backend.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
public class TelegramNotificationController {



    @PostMapping("/send")
    public ResponseEntity<String> sendTelegramNotification(@RequestBody Map<String, String> payload) {
        String location = payload.get("location");
        String message = "🆘 Solicitud de Ayuda\n\nUbicación: " + location;
        
        String telegramApiUrl = String.format("https://api.telegram.org/bot%s/sendMessage", "8164734613:AAGzKw7DMzZu0w7w-WShQAA5m8Ech-MwuTs");
        
        RestTemplate restTemplate = new RestTemplate();
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "7220485574");
        params.add("text", message);
        
        try {
            ResponseEntity<String> response =restTemplate.postForEntity(telegramApiUrl, params, String.class);
            System.out.println("Telegram API Response: " + response.getBody());
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error sending notification");
        }
    }
}
