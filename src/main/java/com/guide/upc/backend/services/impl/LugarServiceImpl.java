package com.guide.upc.backend.services.impl;

import com.guide.upc.backend.entities.Lugar;
import com.guide.upc.backend.exceptions.AppException;
import com.guide.upc.backend.repositories.LugarRepository;
import com.guide.upc.backend.services.LugarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class LugarServiceImpl implements LugarService {
    private final Path root = Paths.get("uploads"); // Directorio donde se guardarán las fotos

    @Autowired
    private LugarRepository lugarRepository;

    @Override
    public List<Lugar> findAll() {
        return lugarRepository.findAll();
    }

    @Override
    public Lugar findById(Long id) {
        return lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
    }

    @Override
    public Lugar save(Lugar lugar) {
        return lugarRepository.save(lugar);
    }

    @Override
    public Lugar update(Long id, Lugar lugar) {
        Lugar existingLugar = lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
        existingLugar.setNombre(lugar.getNombre());
        existingLugar.setDescripcion(lugar.getDescripcion());
        existingLugar.setFoto(lugar.getFoto());
        return lugarRepository.save(existingLugar);
    }

    @Override
    public void delete(Long id) {
        Lugar existingLugar = lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
        lugarRepository.delete(existingLugar);
    }

    @Override
    public Lugar updateLugarPic(Long id, MultipartFile foto) {
        // Primero buscamos el usuario por login
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new AppException("Lugar no encontrado", HttpStatus.NOT_FOUND));
        
        System.out.println("-- LUGAR ENCONTRADO: " + lugar);
        
        // Validar que el ID coincida con el usuario encontrado
        if (!lugar.getId().equals(id)) {
            throw new AppException("ID de lugar no coincide", HttpStatus.BAD_REQUEST);
        }
        // Manejar la foto solo si se proporciona una nueva
        if (foto != null && !foto.isEmpty()) {
            try {
                // Si existe una foto anterior, intentar eliminarla
                if (lugar.getFoto() != null && !lugar.getFoto().isEmpty()) {
                    try {
                        Path oldPhotoPath = Paths.get(lugar.getFoto());
                        Files.deleteIfExists(oldPhotoPath);
                    } catch (IOException e) {
                        System.err.println("No se pudo eliminar la foto anterior: " + e.getMessage());
                        // Continuamos con la actualización aunque no se pueda eliminar la foto anterior
                    }
                }
                String photoPath = savePhoto(foto);
                System.out.println("----FOTO PATH: "+photoPath);
                //lugar.setFoto(photoPath);
                System.out.println("----FOTO LUGAR: "+lugar.getFoto());
            } catch (IOException e) {
                throw new AppException("Error al guardar la foto: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        // Guardar los cambios
        try {
            Lugar updatedLugar = lugarRepository.save(lugar);
            System.out.println("--- LUGAR ACTUALIZADO SERVICE: " + updatedLugar);
            return updatedLugar;
        } catch (Exception e) {
            throw new AppException("Error al actualizar el lugar: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private String savePhoto(MultipartFile photo) throws IOException {
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
    }
}
