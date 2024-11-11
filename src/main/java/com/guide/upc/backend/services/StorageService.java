package com.guide.upc.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService {

    private final Path rootLocation = Paths.get("upload-dir");

    public String store(MultipartFile file) throws IOException {
        if (Files.notExists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
        Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()));
        return rootLocation.resolve(file.getOriginalFilename()).toString();
    }
}
