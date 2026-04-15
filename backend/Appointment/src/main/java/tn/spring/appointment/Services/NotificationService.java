package tn.spring.appointment.Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    // Injection des valeurs depuis application.properties
    @Value("${twilio.account_sid}") private String sid;
    @Value("${twilio.auth_token}") private String token;
    @Value("${twilio.from_number}") private String from;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void initTwilio() {
        Twilio.init(sid, token);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("khaliloessouri10@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ Email envoyé à " + to);
        } catch (Exception e) {
            System.err.println("❌ Erreur Email: " + e.getMessage());
        }
    }

    public void sendSms(String to, String text) {
        try {
            // Force le format international pour la Tunisie si nécessaire
            String formattedPhone = to.startsWith("+") ? to : "+216" + to;

            Message.creator(
                    new PhoneNumber(formattedPhone),
                    new PhoneNumber(from),
                    text
            ).create();
            System.out.println("✅ SMS envoyé à " + formattedPhone);
        } catch (Exception e) {
            System.err.println("❌ Erreur SMS (Twilio): " + e.getMessage());
        }
    }
}