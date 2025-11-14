package com.ejemplo.usuario.service;

import com.ejemplo.usuario.entity.Permission;
import com.ejemplo.usuario.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public Permission findByName(String name) {
        return permissionRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + name));
    }

    public List<Permission> findByResourceAndAction(String resource, String action) {
        return permissionRepository.findByResourceAndAction(resource, action);
    }

    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

}

