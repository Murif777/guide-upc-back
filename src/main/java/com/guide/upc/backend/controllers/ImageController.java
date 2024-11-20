package com.guide.upc.backend.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final ResourceLoader resourceLoader;

    public ImageController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostMapping(value = "/process-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> processImage(@RequestParam("image") MultipartFile file) {
        logger.debug("Received image processing request. File size: {} bytes", file.getSize());
        
        try {
            // Verificar que el archivo no está vacío
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Archivo vacío");
            }

            // Crear directorio temporal si no existe
            Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "guide-upc");
            Files.createDirectories(tempDir);

            // Guardar imagen temporal
            Path tempFile = tempDir.resolve("input.png");
            logger.debug("Saving temporary file to: {}", tempFile);
            Files.write(tempFile, file.getBytes());

            // Obtener ruta del script Python
            Resource pythonScript = resourceLoader.getResource("classpath:python/process_image.py");
            if (!pythonScript.exists()) {
                throw new RuntimeException("Script Python no encontrado");
            }
            File scriptFile = pythonScript.getFile();
            logger.debug("Python script path: {}", scriptFile.getAbsolutePath());

            // Ejecutar script Python
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                scriptFile.getAbsolutePath(),
                tempFile.toString()
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Leer la salida del proceso
            String output = new String(process.getInputStream().readAllBytes());
            logger.debug("Python script output: {}", output);
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Error executing Python script: " + output);
            }

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Imagen procesada exitosamente");
            response.put("results", output);

            // Limpiar archivos temporales
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing image", e);
            return ResponseEntity.status(500)
                .body(Collections.singletonMap("error", "Error processing image: " + e.getMessage()));
        }
    }
}