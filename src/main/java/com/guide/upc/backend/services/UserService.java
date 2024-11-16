package com.guide.upc.backend.services;

import com.guide.upc.backend.dtos.CredentialsDto;
import com.guide.upc.backend.dtos.SignUpDto;
import com.guide.upc.backend.dtos.UserDto;
import com.guide.upc.backend.entities.User;
import com.guide.upc.backend.exceptions.AppException;
import com.guide.upc.backend.mappers.UserMapper;
import com.guide.upc.backend.repositories.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private final Path root = Paths.get("uploads"); // Directorio donde se guardarán las fotos

    @Transactional()
    public UserDto login(CredentialsDto credentialsDto) {
        User user = userRepository.findByLogin(credentialsDto.getLogin())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getContraseña()), user.getContraseña())) {
            System.out.println("Datos del usuario al iniciar sesión: " + userMapper.toUserDto(user));
            return userMapper.toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }
    @Transactional
    public UserDto register(SignUpDto userDto) {
        System.out.println("datos generado service: " + userDto);
        System.out.println("Contraseña recibida: " + userDto.getContraseña().toString());
        Optional<User> optionalUser = userRepository.findByLogin(userDto.getLogin());

        if (optionalUser.isPresent()) {
            throw new AppException("Login already exists", HttpStatus.BAD_REQUEST);
        }

        User user = userMapper.signUpToUser(userDto);
        user.setContraseña(passwordEncoder.encode(CharBuffer.wrap(userDto.getContraseña())));

        User savedUser = userRepository.save(user);
        System.out.println("Datos del usuario al registrarse: " + userMapper.toUserDto(savedUser));
        return userMapper.toUserDto(savedUser);
    }

    @Transactional
    public UserDto findByLogin(String login) {

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
                System.out.println("login SERVICE USERDTO: "+user);
        return userMapper.toUserDto(user);
    }
   


        // Método actualizado para no permitir cambiar el campo login
        @Transactional
public User updateUser(String login, Long id, String nombre, String apellido, MultipartFile foto) throws IOException {
        // Primero buscamos el usuario por login
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        
        System.out.println("-- USUARIO ENCONTRADO: " + user);
        
        // Validar que el ID coincida con el usuario encontrado
        if (!user.getId().equals(id)) {
            throw new AppException("ID de usuario no coincide", HttpStatus.BAD_REQUEST);
        }

        // Actualizar los campos
        user.setNombre(nombre != null ? nombre : user.getNombre());
        user.setApellido(apellido != null ? apellido : user.getApellido());

        // Manejar la foto solo si se proporciona una nueva
        if (foto != null && !foto.isEmpty()) {
            try {
                // Si existe una foto anterior, intentar eliminarla
                if (user.getFoto() != null && !user.getFoto().isEmpty()) {
                    try {
                        Path oldPhotoPath = Paths.get(user.getFoto());
                        Files.deleteIfExists(oldPhotoPath);
                    } catch (IOException e) {
                        System.err.println("No se pudo eliminar la foto anterior: " + e.getMessage());
                        // Continuamos con la actualización aunque no se pueda eliminar la foto anterior
                    }
                }

                String photoPath = savePhoto(foto);
                System.out.println("----FOTO PATH: "+photoPath);
                user.setFoto(photoPath);
            } catch (IOException e) {
                throw new AppException("Error al guardar la foto: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // Guardar los cambios
        try {
            User updatedUser = userRepository.save(user);
            System.out.println("--- USUARIO ACTUALIZADO SERVICE: " + updatedUser);
            return updatedUser;
        } catch (Exception e) {
            throw new AppException("Error al actualizar el usuario: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
