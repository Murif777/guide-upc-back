package com.guide.upc.backend.controllers;

import com.guide.upc.backend.config.UserAuthenticationProvider;
import com.guide.upc.backend.dtos.CredentialsDto;
import com.guide.upc.backend.dtos.SignUpDto;
import com.guide.upc.backend.dtos.UserDto;
import com.guide.upc.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.net.URI;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final UserAuthenticationProvider userAuthenticationProvider;

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        UserDto userDto = userService.login(credentialsDto);
        userDto.setToken(userAuthenticationProvider.createToken(userDto.getLogin()));
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid SignUpDto user) {
        System.out.println("Datos recibidos para registro controller: " + user);
        UserDto createdUser = userService.register(user);
        System.out.println("foto generado: " + user.getFoto());
        createdUser.setToken(userAuthenticationProvider.createToken(user.getLogin()));
        System.out.println("usuario creado controller " + createdUser);
        return ResponseEntity.created(URI.create("/usuarios/" + createdUser.getId())).body(createdUser);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUser(
        @PathVariable Long id,
        @RequestParam String nombre,
        @RequestParam String apellido,
        @RequestParam String contraseña,
        @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {
        try {
            UserDto updatedUser = userService.updateUser(id, nombre, apellido, contraseña, foto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar los datos del usuario", e);
        }
    }
    @GetMapping("/profile") 
    public ResponseEntity<UserDto> getUserProfile(Authentication authentication) { 
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Autenticación fallida");
            return ResponseEntity.status(401).build();
        }
    
        String login = authentication.getName();
        System.out.println("Usuario autenticado CONTROLLER: " + login);
    
        UserDto userDto = userService.findByLogin(login);
        
        if (userDto == null) {
            System.out.println("Perfil de usuario no encontrado");
            return ResponseEntity.status(404).build();
        }
    
        // Establece el token en el `UserDto`
        String token = userAuthenticationProvider.createToken(userDto.getLogin());
        userDto.setToken(token);  // Asegúrate de que este método esté presente en UserDto.
    
        System.out.println("Perfil a enviar CONTROLLER: " + userDto);
        return ResponseEntity.ok(userDto); 
    }
    

}
