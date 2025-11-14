package com.ejemplo.usuario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@example.com");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Recuperación de Contraseña";
        String text = "Para recuperar tu contraseña, haz clic en el siguiente enlace:\n\n"
                + "http://localhost:8080/api/auth/reset-password?token=" + token
                + "\n\nEste enlace expirará en 24 horas.";
        sendEmail(to, subject, text);
    }

    public void sendEmailVerificationEmail(String to, String token) {
        String subject = "Verificación de Email";
        String text = "Para verificar tu email, haz clic en el siguiente enlace:\n\n"
                + "http://localhost:8080/api/auth/verify-email?token=" + token
                + "\n\nEste enlace expirará en 24 horas.";
        sendEmail(to, subject, text);
    }

}

