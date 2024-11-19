package com.guide.upc.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {


    private final Path root = Paths.get("uploads");

    public String savePhoto(MultipartFile photo) throws IOException {
        // Asegurar que el directorio existe
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        // Generar un nombre único para el archivo
        String originalFilename = photo.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID().toString() + extension;

        // Crear la ruta completa
        Path filePath = root.resolve(fileName);

        // Copiar el archivo
        try {
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new IOException("No se pudo guardar la foto: " + e.getMessage());
        }
    } // Directorio donde se guardarán las fotos

}
