package com.ejemplo.usuario.config;

import com.ejemplo.usuario.entity.Permission;
import com.ejemplo.usuario.entity.Role;
import com.ejemplo.usuario.repository.PermissionRepository;
import com.ejemplo.usuario.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        initializePermissions();
        initializeRoles();
    }

    private void initializePermissions() {
        // Permisos para usuarios
        createPermissionIfNotExists("user:read", "Read users", "user", "read", "GET");
        createPermissionIfNotExists("user:write", "Write users", "user", "write", "POST");
        createPermissionIfNotExists("user:update", "Update users", "user", "update", "PUT");
        createPermissionIfNotExists("user:delete", "Delete users", "user", "delete", "DELETE");

        // Permisos para roles
        createPermissionIfNotExists("role:read", "Read roles", "role", "read", "GET");
        createPermissionIfNotExists("role:write", "Write roles", "role", "write", "POST");
    }

    private void createPermissionIfNotExists(String name, String description, 
                                            String resource, String action, String httpMethod) {
        if (!permissionRepository.findByName(name).isPresent()) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);
            permission.setResource(resource);
            permission.setAction(action);
            permission.setHttpMethod(httpMethod);
            permissionRepository.save(permission);
        }
    }

    private void initializeRoles() {
        // Rol de administrador
        if (!roleRepository.findByName("ROLE_ADMIN").isPresent()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrator role with all permissions");
            adminRole.setPermissions(new HashSet<>(permissionRepository.findAll()));
            roleRepository.save(adminRole);
        }

        // Rol de usuario
        if (!roleRepository.findByName("ROLE_USER").isPresent()) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Standard user role");
            Set<Permission> userPermissions = new HashSet<>();
            userPermissions.add(permissionRepository.findByName("user:read").orElseThrow());
            userRole.setPermissions(userPermissions);
            roleRepository.save(userRole);
        }
    }

}

