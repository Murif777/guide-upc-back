package com.guide.upc.backend.controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

@RestController
@RequestMapping("/api")
public class ImageController {

    private final ResourceLoader resourceLoader;

    public ImageController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostMapping("/process-image")
    public ResponseEntity<?> processImage(@RequestParam("image") MultipartFile file) {
        try {
            // Crear directorio temporal si no existe
            Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "guide-upc");
            Files.createDirectories(tempDir);

            // Guardar imagen temporal
            Path tempFile = tempDir.resolve("input.png");
            Files.write(tempFile, file.getBytes());

            // Obtener ruta del script Python
            Resource pythonScript = resourceLoader.getResource("classpath:python/process_image.py");
            File scriptFile = pythonScript.getFile();

            // Ejecutar script Python
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                scriptFile.getAbsolutePath(),
                tempFile.toString()
            );
            
            // Redirigir la salida de error a la salida estándar
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Leer la salida del proceso
            String output = new String(process.getInputStream().readAllBytes());
            
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Error executing Python script: " + output);
            }

            // Leer la imagen procesada
            Path outputPath = tempDir.resolve("output.png");
            byte[] outputBytes = Files.readAllBytes(outputPath);

            // Limpiar archivos temporales
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(outputPath);

            return ResponseEntity.ok(Collections.singletonMap("image", outputBytes));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing image: " + e.getMessage());
        }
    }
}