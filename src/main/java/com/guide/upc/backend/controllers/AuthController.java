package com.guide.upc.backend.controllers;

import com.guide.upc.backend.config.UserAuthenticationProvider;
import com.guide.upc.backend.dtos.CredentialsDto;
import com.guide.upc.backend.dtos.SignUpDto;
import com.guide.upc.backend.dtos.UpdatePasswordDto;
import com.guide.upc.backend.dtos.UserDto;
import com.guide.upc.backend.entities.User;
import com.guide.upc.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
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

    @PutMapping("/update/{login}")
    public ResponseEntity<?> updateUser(
        @PathVariable String login,
        @RequestParam(required = true) String id,
        @RequestParam(required = true) String nombre,
        @RequestParam(required = true) String apellido,
        @RequestParam(required = false) MultipartFile foto
    ) {
        try {
            // Convertir el ID a Long de manera segura
            Long userId;
            try {
                userId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                return ResponseEntity
                    .badRequest()
                    
                    .body("ID de usuario inválido "+id);
            }

            // Validaciones básicas
            if (nombre == null || nombre.trim().isEmpty() ||
                apellido == null || apellido.trim().isEmpty() ) {
                return ResponseEntity
                    .badRequest()
                    .body("Todos los campos son requeridos");
            }

            User updatedUser = userService.updateUser(login, userId, nombre, apellido, foto);
        System.out.println("Usuario actualizado CONTROLLER: " + updatedUser);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar los datos del usuario: " + e.getMessage());
        }
    }
    
    @PutMapping("/update-password") 
    public ResponseEntity<?> updatePassword(@RequestBody @Valid UpdatePasswordDto updatePasswordDto) { 
        try { 
            userService.updatePassword(updatePasswordDto); 
            return ResponseEntity.ok("Contraseña actualizada correctamente"); 
        } catch (Exception e) { 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la contraseña: " + e.getMessage()); 
        } 
    }
    /* 
    @PostMapping("/request-password-reset") 
public ResponseEntity<?> requestPasswordReset(@RequestBody @Valid RequestPasswordResetDto requestPasswordResetDto) {        
        try { 
            userService.requestPasswordReset(requestPasswordResetDto.getEmail()); 
            return ResponseEntity.ok("Se ha enviado un enlace de restablecimiento de contraseña a su correo electrónico"); 
        } catch (Exception e) { 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al solicitar el restablecimiento de contraseña: " + e.getMessage()); 
        } 
    }
    @PutMapping("/reset-password") 
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequestDto passwordResetRequestDto) {        
        try { 
            userService.updatePasswordWithToken(passwordResetRequestDto); 
            return ResponseEntity.ok("Contraseña actualizada correctamente"); 
        } catch (Exception e) { 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la contraseña: " + e.getMessage()); 
        } 
    }
    */
    @GetMapping("/profile") 
    public ResponseEntity<UserDto> getUserProfile(Authentication authentication) { 
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Autenticación fallida");
            return ResponseEntity.status(401).build();
        }
    
        String userDtoString = authentication.getName();
        System.out.println("Usuario autenticado CONTROLLER: " + userDtoString);

        String login = extractLoginFromUserDto(userDtoString); 
        
        System.out.println("Login extraído: " + login);
        UserDto user = userService.findByLogin(login);
        
        if (user == null) {
            System.out.println("Perfil de usuario no encontrado");
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(user); 
    }
    private String extractLoginFromUserDto(String userDtoString) { 
        String loginPrefix = "login="; 
        int loginStart = userDtoString.indexOf(loginPrefix) + loginPrefix.length(); 
        int loginEnd = userDtoString.indexOf(',', loginStart); 
        if (loginEnd == -1) { loginEnd = userDtoString.indexOf(')', loginStart); 
        } 
        return userDtoString.substring(loginStart, loginEnd).trim(); 
    }

}
