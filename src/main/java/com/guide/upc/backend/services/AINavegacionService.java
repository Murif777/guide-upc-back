package com.guide.upc.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.guide.upc.backend.entities.Lugar;
import com.guide.upc.backend.repositories.LugarRepository;

import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AINavegacionService {

    @Autowired
    private LugarRepository lugaresRepository;

    @Autowired
    private SegmentoRutaService rutasRepository;

    @Value("${GEMINI_API_URL}")
    private String GEMINI_API_URL;

    @Value("${GEMINI_API_KEY}") 
    private String API_KEY;
    @PostConstruct
    public void init() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("La API key de Gemini no está configurada");
        }
        System.out.println("API Key de Gemini cargada correctamente");
    }
    
    private final Map<String, String> cacheRespuestas = new ConcurrentHashMap<>();

    public String procesarConsulta(String consultaTexto) {
        consultaTexto = consultaTexto.toLowerCase();
        
        if (cacheRespuestas.containsKey(consultaTexto)) {
            return cacheRespuestas.get(consultaTexto);
        }

        Set<String> lugaresRelevantes = identificarLugaresEnConsulta(consultaTexto);
        
        List<Lugar> lugares = lugaresRelevantes.isEmpty() ? 
                lugaresRepository.findAll() : 
                lugaresRepository.findByNombreIn(lugaresRelevantes);
        
        // Extraer origen y destino para la ruta
        String origen = determinarOrigenDesdeConsulta(consultaTexto, lugaresRelevantes);
        String destino = determinarDestinoDesdeConsulta(consultaTexto, lugaresRelevantes);
        
        Map<String, Object> informacionRuta = null;
        if (esConsultaDeRuta(consultaTexto) && origen != null && destino != null) {
            informacionRuta = rutasRepository.obtenerMejorRuta(origen, destino);
        }

        String contexto = construirContextoOptimizado(lugares, informacionRuta, consultaTexto);
        String promptFinal = generarPromptParaIA(consultaTexto, contexto);
        String respuestaIA = llamarAPIGemini(promptFinal);
        
        cacheRespuestas.put(consultaTexto, respuestaIA);

        return respuestaIA;
    }
    
    private String determinarOrigenDesdeConsulta(String consulta, Set<String> lugaresIdentificados) {
        // Implementación básica para extraer el origen de la consulta
        // Esta lógica podría ser mejorada con NLP más avanzado
        List<String> palabrasClave = Arrays.asList("desde", "partiendo de", "saliendo de", "estoy en");
        
        for (String palabra : palabrasClave) {
            int index = consulta.indexOf(palabra);
            if (index != -1) {
                String subcadena = consulta.substring(index + palabra.length()).trim();
                for (String lugar : lugaresIdentificados) {
                    if (subcadena.contains(lugar.toLowerCase())) {
                        return lugar;
                    }
                }
            }
        }
        
        // Si no se encuentra explícitamente, usa el primer lugar mencionado si hay al menos dos
        if (lugaresIdentificados.size() >= 2) {
            return lugaresIdentificados.iterator().next();
        }
        
        return null;
    }
    
    private String determinarDestinoDesdeConsulta(String consulta, Set<String> lugaresIdentificados) {
        // Implementación básica para extraer el destino de la consulta
        List<String> palabrasClave = Arrays.asList("hasta", "hacia", "llegar a", "ir a");
        
        for (String palabra : palabrasClave) {
            int index = consulta.indexOf(palabra);
            if (index != -1) {
                String subcadena = consulta.substring(index + palabra.length()).trim();
                for (String lugar : lugaresIdentificados) {
                    if (subcadena.contains(lugar.toLowerCase())) {
                        return lugar;
                    }
                }
            }
        }
        
        // Si no se encuentra explícitamente y hay al menos dos lugares, usa el segundo
        if (lugaresIdentificados.size() >= 2) {
            Iterator<String> iterator = lugaresIdentificados.iterator();
            iterator.next(); // Saltar el primero (origen)
            return iterator.next(); // Retornar el segundo (destino)
        }
        
        return null;
    }
    
    private Set<String> identificarLugaresEnConsulta(String consulta) {
        Set<String> lugaresIdentificados = new HashSet<>();
        List<Lugar> todosLosLugares = lugaresRepository.findAll();
        
        for (Lugar lugar : todosLosLugares) {
            if (consulta.contains(lugar.getNombre().toLowerCase())) { 
                lugaresIdentificados.add(lugar.getNombre());
            }
        }
        return lugaresIdentificados;
    }

    private String construirContextoOptimizado(List<Lugar> lugares, Map<String, Object> informacionRuta, String consulta) {
        StringBuilder contexto = new StringBuilder();
        contexto.append("Contexto sobre la universidad:\n");

        // Añadir información de lugares relevantes
        for (Lugar lugar : lugares) {
            contexto.append("- ").append(lugar.getNombre())
                   .append(": ").append(lugar.getDescripcion());
        }

        // Añadir información de rutas si es relevante y si hay información de ruta disponible
        if (esConsultaDeRuta(consulta) && informacionRuta != null) {
            contexto.append("\nRuta recomendada:\n");
            
            // Obtener el camino de la ruta
            @SuppressWarnings("unchecked")
            List<String> path = (List<String>) informacionRuta.get("path");
            
            if (path != null && path.size() > 1) {
                contexto.append("- Ruta: ");
                for (int i = 0; i < path.size(); i++) {
                    contexto.append(path.get(i));
                    if (i < path.size() - 1) {
                        contexto.append(" → ");
                    }
                }
                contexto.append("\n");
            }
            
            // Obtener las instrucciones de la ruta
            @SuppressWarnings("unchecked")
            List<String> instructions = (List<String>) informacionRuta.get("instructions");
            
            if (instructions != null && !instructions.isEmpty()) {
                contexto.append("\nInstrucciones paso a paso:\n");
                for (int i = 0; i < instructions.size(); i++) {
                    contexto.append(i + 1).append(". ").append(instructions.get(i)).append("\n");
                }
            }
        }

        return contexto.toString();
    }

    private boolean esConsultaDeRuta(String consulta) {
        String[] palabrasClave = {"como llegar", "ruta", "camino", "ir a", "llegar a", "desde", "hasta","ir hacia"};
        consulta = consulta.toLowerCase();
        
        for (String palabra : palabrasClave) {
            if (consulta.contains(palabra)) {
                return true;
            }
        }
        return false;
    }

    private String generarPromptParaIA(String consulta, String contexto) {
        return String.format("""
            Actúa como un asistente experto el cual guia y afrece informacion a personas con discapacidad visual de la Universidad. Usa el siguiente contexto para responder la consulta del usuario.
            
            %s
            
            Consulta del usuario: %s
            
            Responde de manera clara, corta, precisa y amigable. Si la consulta es sobre una ruta, incluye instrucciones paso a paso.
            Si no tienes suficiente información, indícalo amablemente. es importante que solo respondas de acuerdo a la informacion suministrada de acuerdo al contexto sin emojis y sin extenderte mucho
            si la respuesta a una consulta contiene varias opciones indicalas de manera breve 
            """, contexto, consulta);
    }

    private String llamarAPIGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", API_KEY);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(Collections.singletonMap("text", prompt)));
        requestBody.put("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL, request, Map.class);
            // Procesar la respuesta de Gemini (ajustar según la estructura real de la respuesta)
            return extraerRespuestaDeGemini(response.getBody());
        } catch (Exception e) {
            return "Lo siento, hubo un error al procesar tu consulta. Por favor, intenta nuevamente."+e.getMessage()+API_KEY;
        }
    }

    private String extraerRespuestaDeGemini(Map responseBody) {
        try {
            Map<String, Object> candidates = (Map<String, Object>) ((List) responseBody.get("candidates")).get(0);
            Map<String, Object> content = (Map<String, Object>) candidates.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            return "Error al procesar la respuesta de la IA.";
        }
    }

    private <T> List<T> limitarDatos(List<T> lista, int limite) {
        return lista.stream()
                   .limit(limite)
                   .toList();
    }
}