package com.agentsbackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ConditionalOnExpression("'${auth.email.enabled:false}' == 'true' and '${spring.mail.host:}' != ''")
public class SmtpAuthEmailService implements AuthEmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpAuthEmailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String smtpPassword;
    private final String publicBaseUrl;

    public SmtpAuthEmailService(JavaMailSender mailSender,
                                @Value("${spring.mail.from:${spring.mail.username:}}") String from,
                                @Value("${spring.mail.password:}") String smtpPassword,
                                @Value("${auth.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.smtpPassword = smtpPassword;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/$", "");
    }

    @Override
    public void sendEmailVerification(String email, String token) {
        send(email, "Verify your email", "Verify your email by submitting this one-time token to POST " +
                publicBaseUrl + "/auth/verify-email: " + token);
    }

    @Override
    public void sendPasswordReset(String email, String token) {
        send(email, "Password Reset Request", "Submit this one-time token to POST " + publicBaseUrl +
                "/auth/password/reset. It expires in 30 minutes: " + token);
    }

    @Override
    public void sendPasswordChanged(String email) {
        send(email, "Password changed", "Your account password was changed. Contact support if this was not you.");
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (RuntimeException exception) {
            Throwable rootCause = exception;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            String diagnostic = rootCause.getMessage();
            if (diagnostic == null || diagnostic.isBlank()) diagnostic = rootCause.getClass().getSimpleName();
            if (!from.isBlank()) diagnostic = diagnostic.replace(from, "[SMTP username redacted]");
            if (!smtpPassword.isBlank()) diagnostic = diagnostic.replace(smtpPassword, "[SMTP password redacted]");
            diagnostic = diagnostic.replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[email redacted]");
            if (diagnostic.length() > 240) diagnostic = diagnostic.substring(0, 240);
            LOGGER.warn("SMTP delivery failed ({}): {}", rootCause.getClass().getSimpleName(), diagnostic);
            throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Email delivery failed. Check the SMTP host, port, username, password, and provider settings.");
        }
    }
}
