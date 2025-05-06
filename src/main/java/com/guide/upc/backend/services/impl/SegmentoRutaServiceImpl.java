package com.guide.upc.backend.services.impl;

import com.guide.upc.backend.entities.SegmentoRuta;
import com.guide.upc.backend.repositories.SegmentoRutaRepository;
import com.guide.upc.backend.services.SegmentoRutaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class SegmentoRutaServiceImpl implements SegmentoRutaService {

    private final SegmentoRutaRepository segmentoRutaRepository;

    @Override
    public List<SegmentoRuta> obtenerTodosLosSegmentos() {
        return segmentoRutaRepository.findAll();
    }

    @Override
    public Map<String, Object> obtenerMejorRuta(String origen, String destino) {
        // Normalizar origen y destino para coincidir con el formato de la base de datos
        String origenNormalizado = normalizarNodo(origen);
        String destinoNormalizado = normalizarNodo(destino);
        
        List<SegmentoRuta> segmentos = segmentoRutaRepository.findAll();
        Map<String, Map<String, Map<String, Object>>> grafo = construirGrafoDesdeSegmentos(segmentos);
        return dijkstra(grafo, origenNormalizado, destinoNormalizado);
    }
    
    // Método para normalizar los nombres de los nodos (minúsculas y guiones bajos)
    private String normalizarNodo(String nodo) {
        return nodo != null ? nodo.toLowerCase().replace(" ", "_") : nodo;
    }

    private Map<String, Map<String, Map<String, Object>>> construirGrafoDesdeSegmentos(List<SegmentoRuta> segmentos) {
        Map<String, Map<String, Map<String, Object>>> grafo = new HashMap<>();
        
        for (SegmentoRuta segmento : segmentos) {
            // Los lugares ya vienen normalizados de la base de datos (minúsculas y con guiones bajos)
            String lugarInicio = segmento.getLugarInicio();
            String lugarFin = segmento.getLugarFin();
            
            // Verificación de seguridad para asegurar el formato correcto
            if (lugarInicio == null || lugarFin == null) {
                System.out.println("Advertencia: Segmento con lugar de inicio o fin nulo encontrado");
                continue;
            }
            
            if (!grafo.containsKey(lugarInicio)) {
                grafo.put(lugarInicio, new HashMap<>());
            }
            
            Map<String, Object> propiedades = new HashMap<>();
            propiedades.put("distance", segmento.getDistancia());
            propiedades.put("instructions", "Camina " + segmento.getDistancia() + " pasos hacia el " + segmento.getDireccion());
            
            grafo.get(lugarInicio).put(lugarFin, propiedades);
        }
        
        return grafo;
    }
    
    // Implementación de la cola de prioridad
    private static class NodoPrioridad {
        String elemento;
        double prioridad;
        
        public NodoPrioridad(String elemento, double prioridad) {
            this.elemento = elemento;
            this.prioridad = prioridad;
        }
    }
    
    private static class ColaPrioridad {
        private final List<NodoPrioridad> coleccion;
        
        public ColaPrioridad() {
            this.coleccion = new ArrayList<>();
        }
        
        public void encolar(String elemento, double prioridad) {
            NodoPrioridad nodoColeccion = new NodoPrioridad(elemento, prioridad);
            boolean agregado = false;
            
            for (int i = 0; i < coleccion.size(); i++) {
                if (nodoColeccion.prioridad < coleccion.get(i).prioridad) {
                    coleccion.add(i, nodoColeccion);
                    agregado = true;
                    break;
                }
            }
            
            if (!agregado) {
                coleccion.add(nodoColeccion);
            }
        }
        
        public NodoPrioridad desencolar() {
            return coleccion.remove(0);
        }
        
        public boolean estaVacia() {
            return coleccion.isEmpty();
        }
    }
    
    private Map<String, Object> dijkstra(Map<String, Map<String, Map<String, Object>>> grafo, String inicio, String fin) {
        if (grafo == null || grafo.isEmpty()) {
            System.out.println("El grafo está vacío o no está definido");
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("path", new ArrayList<>());
            resultado.put("instructions", new ArrayList<>());
            return resultado;
        }
        
        Map<String, Double> distancias = new HashMap<>();
        Map<String, String> anterior = new HashMap<>();
        ColaPrioridad cp = new ColaPrioridad();
        Map<String, List<String>> instrucciones = new HashMap<>();
        
        distancias.put(inicio, 0.0);
        cp.encolar(inicio, 0.0);
        
        for (String nodo : grafo.keySet()) {
            if (!nodo.equals(inicio)) {
                distancias.put(nodo, Double.POSITIVE_INFINITY);
            }
            anterior.put(nodo, null);
            instrucciones.put(nodo, new ArrayList<>());
        }
        
        while (!cp.estaVacia()) {
            NodoPrioridad actual = cp.desencolar();
            String nodoActual = actual.elemento;
            
            if (nodoActual.equals(fin)) break;
            
            if (!grafo.containsKey(nodoActual)) {
                System.out.println("No se encontró el nodo en el grafo: " + nodoActual);
                continue;
            }
            
            for (String vecino : grafo.get(nodoActual).keySet()) {
                if (!grafo.get(nodoActual).containsKey(vecino) || 
                    grafo.get(nodoActual).get(vecino) == null) {
                    System.out.println("No se encontró el vecino en el grafo: " + vecino + 
                                     " para el nodo: " + nodoActual);
                    continue;
                }
                
                // Manejar tanto Integer como Double
                Object distanciaObj = grafo.get(nodoActual).get(vecino).get("distance");
                double distancia;
                if (distanciaObj instanceof Integer) {
                    distancia = ((Integer) distanciaObj).doubleValue();
                } else if (distanciaObj instanceof Double) {
                    distancia = (Double) distanciaObj;
                } else {
                    System.out.println("Tipo de distancia no válido: " + (distanciaObj != null ? distanciaObj.getClass() : "null"));
                    continue;
                }
                
                double alternativa = distancias.get(nodoActual) + distancia;
                
                if (alternativa < distancias.getOrDefault(vecino, Double.POSITIVE_INFINITY)) {
                    distancias.put(vecino, alternativa);
                    anterior.put(vecino, nodoActual);
                    
                    List<String> instruccionesActualizadas = new ArrayList<>(instrucciones.get(nodoActual));
                    instruccionesActualizadas.add((String) grafo.get(nodoActual).get(vecino).get("instructions"));
                    instrucciones.put(vecino, instruccionesActualizadas);
                    
                    cp.encolar(vecino, alternativa);
                }
            }
        }
        
        List<String> camino = new ArrayList<>();
        String u = fin;
        
        if (anterior.get(u) == null) {
            System.out.println("No se encontró una ruta entre " + inicio + " y " + fin);
            System.out.println("Nodos disponibles en el grafo: " + String.join(", ", grafo.keySet()));
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("path", camino);
            resultado.put("instructions", new ArrayList<>());
            return resultado;
        }
        
        while (anterior.get(u) != null) {
            camino.add(0, u);
            u = anterior.get(u);
        }
        
        camino.add(0, inicio);
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("path", camino);
        resultado.put("instructions", instrucciones.getOrDefault(fin, new ArrayList<>()));
        return resultado;
    }
}