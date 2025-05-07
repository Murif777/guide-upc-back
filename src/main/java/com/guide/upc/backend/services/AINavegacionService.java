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
            System.out.println("----------Información de ruta: " + origen + " a " + destino );
        }

        String contexto = construirContextoOptimizado(lugares, informacionRuta, consultaTexto);
        String promptFinal = generarPromptParaIA(consultaTexto, contexto);
        System.out.println("Prompt enviado a Gemini: " + promptFinal);
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
        
        // Convertir la consulta a minúsculas y dividirla en palabras
        String[] palabrasConsulta = consulta.toLowerCase().split("\\s+");
        
        for (Lugar lugar : todosLosLugares) {
            String nombreLugar = lugar.getNombre().toLowerCase();
            
            // Verificar coincidencia exacta primero
            if (nombreLugar.contains(consulta.toLowerCase())) {
                lugaresIdentificados.add(lugar.getNombre());
                continue;
            }

            // Si no hay coincidencia exacta, buscar por palabras
            for (String palabraConsulta : palabrasConsulta) {
                // Solo considerar palabras de 3 o más caracteres para evitar falsos positivos
                if (palabraConsulta.length() >= 3 && nombreLugar.contains(palabraConsulta)) {
                    lugaresIdentificados.add(lugar.getNombre());
                    break;
                }
            }
        }
        
        //System.out.println("Lugares identificados en la consulta: " + lugaresIdentificados);
        return lugaresIdentificados;
    }

    private String normalizarNombreLugar(String nombre) {
        return nombre != null ? nombre.replace("_", " ") : nombre;
    }

    private String construirContextoOptimizado(List<Lugar> lugares, Map<String, Object> informacionRuta, String consulta) {
        StringBuilder contexto = new StringBuilder();
        contexto.append("Contexto sobre la universidad:\n");

        // Añadir información de lugares relevantes
        boolean incluirDescripcion = consulta.contains("descripcion");
        for (Lugar lugar : lugares) {
            contexto.append("- ").append(lugar.getNombre());
            if (incluirDescripcion) {
                contexto.append(": ").append(lugar.getDescripcion());
            }
            contexto.append("\n");
        }

        // Añadir información de rutas si es relevante y si hay información de ruta disponible
        if (esConsultaDeRuta(consulta) && informacionRuta != null) {
            contexto.append("\nRuta recomendada:\n");
            
            @SuppressWarnings("unchecked")
            List<String> path = (List<String>) informacionRuta.get("path");
            
            @SuppressWarnings("unchecked")
            List<String> instructions = (List<String>) informacionRuta.get("instructions");
            
            if (path != null && instructions != null && path.size() > 1 && instructions.size() > 0) {
                for (int i = 0; i < path.size() - 1; i++) {
                    contexto.append("desde ")
                           .append(normalizarNombreLugar(path.get(i)))
                           .append(" ")
                           .append(instructions.get(i))
                           .append(" ")
                           .append("luego ");
                }
                // Agregar el destino final
                contexto.append(normalizarNombreLugar(path.get(path.size() - 1)));
                contexto.append("\n");
            }
        }

        return contexto.toString();
    }

    private boolean esConsultaDeRuta(String consulta) {
        String[] palabrasClave = {"como llegar", "ruta", "camino", "ir", "desde", "hasta","hacia","desde", "hacia","al", "a la","ala"};
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
            
            Responde de forma clara, breve, precisa y amable. No incluyas saludos ni emojis; mantén siempre una actitud servicial y disponible.
            Si la consulta es sobre una ruta, ofrece instrucciones paso a paso, describiendo con detalle cada referencia, pensando en que el usuario tiene discapacidad visual.
            Si falta información, indícalo amablemente.
            Limítate exclusivamente a la información proporcionada en el contexto.Si la respuesta incluye varias opciones, preséntalas de manera muy concisa.
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