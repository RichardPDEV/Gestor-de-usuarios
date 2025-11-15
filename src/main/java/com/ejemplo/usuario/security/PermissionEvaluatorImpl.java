package com.ejemplo.usuario.security;

import com.ejemplo.usuario.entity.Permission;
import com.ejemplo.usuario.entity.Role;
import com.ejemplo.usuario.entity.User;
import com.ejemplo.usuario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionEvaluatorImpl implements PermissionEvaluator {
    private final UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String permissionName = (String) permission;
        String resource = (String) targetDomainObject;

        return hasPermission(user, resource, permissionName);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, targetType, permission);
    }

    public boolean hasPermission(User user, String resource, String action) {
        Set<Role> roles = user.getRoles();

        for (Role role : roles) {
            Set<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                if (permission.getResource().equals(resource) && 
                    permission.getAction().equals(action)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasPermissionForMethod(User user, String resource, String httpMethod) {
        Set<Role> roles = user.getRoles();

        for (Role role : roles) {
            Set<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                if (permission.getResource().equals(resource) && 
                    permission.getHttpMethod().equals(httpMethod)) {
                    return true;
                }
            }
        }

        return false;
    }
}