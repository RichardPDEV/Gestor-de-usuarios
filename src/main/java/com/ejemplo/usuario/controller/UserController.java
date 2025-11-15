package com.ejemplo.usuario.controller;


import com.ejemplo.usuario.dto.response.UserResponse;
import com.ejemplo.usuario.security.annotation.RequiresPermission;  // ← NUEVO IMPORT
import com.ejemplo.usuario.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @RequiresPermission(resource = "user", action = "read", httpMethod = "GET")  // ← NUEVA ANOTACIÓN
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @RequiresPermission(resource = "user", action = "read", httpMethod = "GET")  // ← NUEVA ANOTACIÓN
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.convertToResponse(userService.findById(id)));
    }

    @GetMapping("/me")  // ← NUEVO MÉTODO (opcional pero recomendado)
    public ResponseEntity<UserResponse> getCurrentUser() {
        // El usuario actual puede ver su propia información sin necesidad de permisos especiales
        return ResponseEntity.ok(userService.getCurrentUser());
    }
}