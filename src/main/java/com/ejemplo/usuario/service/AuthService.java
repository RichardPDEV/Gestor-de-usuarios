package com.ejemplo.usuario.service;

import com.ejemplo.usuario.dto.request.*;
import com.ejemplo.usuario.dto.response.AuthResponse;
import com.ejemplo.usuario.dto.response.MessageResponse;
import com.ejemplo.usuario.dto.response.UserResponse;
import com.ejemplo.usuario.entity.PasswordResetToken;
import com.ejemplo.usuario.entity.User;
import com.ejemplo.usuario.repository.PasswordResetTokenRepository;
import com.ejemplo.usuario.repository.UserRepository;
import com.ejemplo.usuario.security.UserDetailsServiceImpl;
import com.ejemplo.usuario.security.UserPrincipal;
import com.ejemplo.usuario.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validar que el usuario no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Crear usuario
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Asignar rol por defecto (ROLE_USER)
        user.getRoles().add(roleService.findByName("ROLE_USER"));

        user = userRepository.save(user);

        // Generar tokens
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtUtil.generateToken(userPrincipal);
        String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

        // Enviar email de verificación (opcional)
        // emailService.sendEmailVerificationEmail(user.getEmail(), verificationToken);

        // Construir respuesta
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getExpiration());
        response.setUser(userService.convertToResponse(user));

        return response;
    }

    public AuthResponse login(LoginRequest request) {
        // Determinar si es username o email
        String usernameOrEmail = request.getUsernameOrEmail();
        UserDetails userDetails;
        
        try {
            // Intentar primero como username
            userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
        } catch (UsernameNotFoundException e) {
            // Si no se encuentra, intentar como email
            try {
                userDetails = userDetailsService.loadUserByEmail(usernameOrEmail);
            } catch (UsernameNotFoundException ex) {
                throw new RuntimeException("Invalid username or password");
            }
        }

        // Autenticar con las credenciales
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtUtil.generateToken(userPrincipal);
        String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

        User user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getExpiration());
        response.setUser(userService.convertToResponse(user));

        return response;
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtUtil.generateToken(userPrincipal);
        String newRefreshToken = jwtUtil.generateRefreshToken(userPrincipal);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(jwtUtil.getExpiration());
        response.setUser(userService.convertToResponse(user));

        return response;
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Eliminar tokens anteriores
        passwordResetTokenRepository.deleteByUserId(user.getId());

        // Crear nuevo token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        // Enviar email
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return new MessageResponse("Password reset email sent", true);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse("Password reset successfully", true);
    }
}