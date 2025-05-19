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
        // Validación de API Key
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("La API key de Gemini no está configurada");
        }
        
    }
    
    private final Map<String, String> cacheRespuestas = new ConcurrentHashMap<>();

    public String procesarConsulta(String consultaTexto) {
        consultaTexto = eliminarAcentos(consultaTexto.toLowerCase());
    
        
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
        List<String> palabrasClave = Arrays.asList("hasta", "hacia", "llegar");
        
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
        
        // Normalizar la consulta: minúsculas y eliminar puntuación
        String consultaNormalizada = consulta.toLowerCase().replaceAll("[.,;:!?]", "");
        
        // Primero, intentar buscar coincidencias de frases completas
        for (Lugar lugar : todosLosLugares) {
            String nombreLugar = lugar.getNombre().toLowerCase();
            if (consultaNormalizada.contains(nombreLugar)) {
                lugaresIdentificados.add(lugar.getNombre());
            }
        }
        
        // Si no encontramos coincidencias exactas, usamos un enfoque más sofisticado
        if (lugaresIdentificados.isEmpty()) {
            // Crear un mapa de lugares por su nombre normalizado
            Map<String, List<Lugar>> lugaresMap = new HashMap<>();
            for (Lugar lugar : todosLosLugares) {
                String[] palabrasLugar = lugar.getNombre().toLowerCase().split("\\s+");
                for (String palabra : palabrasLugar) {
                    if (palabra.length() >= 3) { // Solo palabras significativas
                        lugaresMap.computeIfAbsent(palabra, k -> new ArrayList<>()).add(lugar);
                    }
                }
            }
            
            // Buscar frases que puedan ser lugares usando patrones comunes
            String[] patrones = {"desde", "hacia", "hasta", "a la", "al", "en la", "en el"};
            for (String patron : patrones) {
                int index = consultaNormalizada.indexOf(patron);
                if (index != -1) {
                    String fragmento = consultaNormalizada.substring(index + patron.length()).trim();
                    // Cortar en la siguiente preposición, si existe
                    for (String otroPatron : patrones) {
                        int finFragmento = fragmento.indexOf(" " + otroPatron + " ");
                        if (finFragmento != -1) {
                            fragmento = fragmento.substring(0, finFragmento);
                        }
                    }
                    
                    // Buscar el lugar más largo que coincida con este fragmento
                    String mejorLugar = null;
                    int mejorLongitud = 0;
                    
                    for (Lugar lugar : todosLosLugares) {
                        String nombreLugar = lugar.getNombre().toLowerCase();
                        if (fragmento.contains(nombreLugar) && nombreLugar.length() > mejorLongitud) {
                            mejorLugar = lugar.getNombre();
                            mejorLongitud = nombreLugar.length();
                        }
                    }
                    
                    if (mejorLugar != null) {
                        lugaresIdentificados.add(mejorLugar);
                    }
                }
            }
            
            // Si aún no hay coincidencias, usar un enfoque de coincidencia parcial
            if (lugaresIdentificados.isEmpty()) {
                // Dividir la consulta en palabras
                String[] palabrasConsulta = consultaNormalizada.split("\\s+");
                
                // Crear un mapa para contar coincidencias por lugar
                Map<String, Integer> contadorCoincidencias = new HashMap<>();
                
                // Para cada palabra significativa en la consulta
                for (String palabra : palabrasConsulta) {
                    if (palabra.length() >= 3 && !esStopWord(palabra)) {
                        // Buscar lugares que contengan esta palabra
                        for (Lugar lugar : todosLosLugares) {
                            String nombreLugar = lugar.getNombre().toLowerCase();
                            if (nombreLugar.contains(palabra) || 
                                nombreLugar.split("\\s+")[0].equals(palabra)) { // Dar prioridad a palabras iniciales
                                contadorCoincidencias.merge(lugar.getNombre(), 1, Integer::sum);
                            }
                        }
                    }
                }
                
                // Ordenar lugares por número de coincidencias (de más a menos)
                List<Map.Entry<String, Integer>> listaOrdenada = new ArrayList<>(contadorCoincidencias.entrySet());
                listaOrdenada.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
                
                // Tomar los 2 lugares con más coincidencias, si hay suficientes
                int numLugaresASeleccionar = Math.min(2, listaOrdenada.size());
                for (int i = 0; i < numLugaresASeleccionar; i++) {
                    lugaresIdentificados.add(listaOrdenada.get(i).getKey());
                }
            }
        }
        
        System.out.println("Lugares identificados en la consulta: " + lugaresIdentificados);
        return lugaresIdentificados;
    }
    
    // Lista de palabras comunes que no deberían usarse para identificar lugares
    private boolean esStopWord(String palabra) {
        Set<String> stopWords = Set.of("desde", "hacia", "hasta", "quiero", "ir", "como", "llegar", "cual", "cuál", 
                                      "donde", "dónde", "para", "está", "esta", "este", "esos", "esas", "estos");
        return stopWords.contains(palabra);
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
                           .append("para llegar a ")
                           .append(normalizarNombreLugar(path.get(i+1)))
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

    private String eliminarAcentos(String texto) {
        if (texto == null) {
            return null;
        }
        
        String normalizado = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
        return normalizado.replaceAll("[^\\p{ASCII}]", "");
    }
}