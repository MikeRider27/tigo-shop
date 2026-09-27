package com.tigo.shop.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Envía el correo de recuperación de forma asíncrona, así el tiempo de respuesta
 * del endpoint no delata si el email existe. En local los correos llegan a Mailpit.
 */
@Component
public class PasswordResetMailer {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailer.class);

    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String from;

    public PasswordResetMailer(JavaMailSender mailSender,
                               @Value("${app.frontend-url}") String frontendUrl,
                               @Value("${app.mail-from}") String from) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.from = from;
    }

    @Async
    public void sendResetLink(String to, String firstName, String rawToken, Duration ttl) {
        String link = frontendUrl + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Tigo Sports - Recupera tu contraseña");
        message.setText("""
                Hola %s,

                Recibimos una solicitud para restablecer tu contraseña.
                Ingresa al siguiente enlace (válido por %d minutos):

                %s

                Si no solicitaste este cambio, ignora este mensaje.
                """.formatted(firstName, ttl.toMinutes(), link));
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("No se pudo enviar el correo de recuperación", e);
        }
    }
}
