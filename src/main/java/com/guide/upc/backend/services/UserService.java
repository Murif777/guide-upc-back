package com.guide.upc.backend.services;

import com.guide.upc.backend.dtos.CredentialsDto;
import com.guide.upc.backend.dtos.SignUpDto;
import com.guide.upc.backend.dtos.UserDto;
import com.guide.upc.backend.entities.User;
import com.guide.upc.backend.exceptions.AppException;
import com.guide.upc.backend.mappers.UserMapper;
import com.guide.upc.backend.repositories.UserRepository;

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
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private final Path root = Paths.get("uploads"); // Directorio donde se guardarán las fotos

    public UserDto login(CredentialsDto credentialsDto) {
        User user = userRepository.findByLogin(credentialsDto.getLogin())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getContraseña()), user.getContraseña())) {
            System.out.println("Datos del usuario al iniciar sesión: " + userMapper.toUserDto(user));
            return userMapper.toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

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

    public UserDto findByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        return userMapper.toUserDto(user);
    }

    // Método actualizado para no permitir cambiar el campo login
    public UserDto updateUser(Long userId, String nombre, String apellido, String contraseña, MultipartFile foto) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setContraseña(passwordEncoder.encode(CharBuffer.wrap(contraseña)));

        if (foto != null && !foto.isEmpty()) {
            String photoPath = savePhoto(foto); // Guardar la foto y obtener la ruta
            user.setFoto(photoPath);// Actualizar la entidad User con la URL de la foto
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDto(updatedUser);
    }

    private String savePhoto(MultipartFile photo) throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        String fileName = System.currentTimeMillis() + "-" + photo.getOriginalFilename();
        Path filePath = root.resolve(fileName);
        Files.copy(photo.getInputStream(), filePath);
        return filePath.toString(); // Devolver la ruta del archivo
    }
}
