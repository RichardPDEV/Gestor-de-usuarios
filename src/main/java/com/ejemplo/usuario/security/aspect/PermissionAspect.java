package com.ejemplo.usuario.security.aspect;

import com.ejemplo.usuario.entity.User;
import com.ejemplo.usuario.repository.UserRepository;
import com.ejemplo.usuario.security.PermissionEvaluatorImpl;
import com.ejemplo.usuario.security.UserPrincipal;
import com.ejemplo.usuario.security.annotation.RequiresPermission;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    private final PermissionEvaluatorImpl permissionEvaluator;
    private final UserRepository userRepository;

    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resource = requiresPermission.resource();
        String httpMethod = requiresPermission.httpMethod();
        
        if (!httpMethod.isEmpty()) {
            if (!permissionEvaluator.hasPermissionForMethod(user, resource, httpMethod)) {
                throw new RuntimeException("Access denied: No permission for " + httpMethod + " on " + resource);
            }
        } else {
            String action = requiresPermission.action();
            if (!permissionEvaluator.hasPermission(user, resource, action)) {
                throw new RuntimeException("Access denied: No permission for " + action + " on " + resource);
            }
        }
    }
}