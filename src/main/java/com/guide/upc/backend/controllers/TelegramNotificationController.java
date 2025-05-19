package com.guide.upc.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
public class TelegramNotificationController {

    @Value("${TELEGRAM_BOT_TOKEN}")
    private String TELEGRAM_BOT_TOKEN;
    
    @Value("${TELEGRAM_CHAT_ID}")
    private String CHAT_ID;
    
    @PostMapping("/send")
    public ResponseEntity<String> sendTelegramNotification(@RequestBody Map<String, String> payload) {
        String location = payload.get("location");
        String message = "🆘 Solicitud de Ayuda\n\nUbicación: " + location;
        
        try {
            // Crear una instancia de RestTemplate con configuración personalizada
            RestTemplate restTemplate = createRestTemplate();
            
            // Construir la URL de la API de Telegram
            String telegramApiUrl = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage";
            
            // Preparar los parámetros
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("chat_id", CHAT_ID);
            params.add("text", message);
            
            // Realizar la petición y capturar la respuesta
            ResponseEntity<String> response = restTemplate.postForEntity(telegramApiUrl, params, String.class);
            
            // Registrar la respuesta y devolver éxito
            System.out.println("Telegram API Response: " + response.getBody());
            return ResponseEntity.ok("Notification sent successfully");
            
        } catch (RestClientException e) {
            // Registrar el error específico de la API
            System.out.println("Error de comunicación con la API de Telegram: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Error al enviar la notificación: " + e.getMessage());
        } catch (Exception e) {
            // Registrar cualquier otro error
            System.out.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error inesperado al procesar la solicitud");
        }
    }
    
    /**
     * Crea una instancia de RestTemplate con la configuración adecuada
     */
    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        
        // Configurar timeouts más largos
        requestFactory.setConnectTimeout(5000); // 5 segundos
        requestFactory.setReadTimeout(10000);   // 10 segundos
        
        // Descomenta y configura si necesitas un proxy
        /*
        // Configurar proxy si es necesario
        String proxyHost = "proxy.example.com";
        int proxyPort = 8080;
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        requestFactory.setProxy(proxy);
        */
        
        return new RestTemplate(requestFactory);
    }
}