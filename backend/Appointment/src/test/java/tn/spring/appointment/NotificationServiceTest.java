package tn.spring.appointment; // Votre package actuel pour le test

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

// --- CET IMPORT ÉTAIT MANQUANT ---
import tn.spring.appointment.Services.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Intégration : Envoi d'email via MailSender")
    void testSendEmailTriggersMailSender() {
        // Given
        String to = "user@test.com";
        String subject = "Test";
        String body = "Hello";

        // When
        notificationService.sendEmail(to, subject, body);

        // Then
        // On vérifie que la méthode send() a été appelée 1 fois
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}