package tn.spring.clubevent.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPassEmail(
            String to,
            String userName,
            String eventTitle,
            LocalDateTime eventDate,
            String location,
            String passCode,
            byte[] pdfBytes
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your Event Pass – " + eventTitle);

            String dateStr = eventDate != null
                    ? eventDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm"))
                    : "TBD";

            String html = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9fafb; padding: 24px; border-radius: 12px;">
                      <div style="background: #1e40af; border-radius: 8px 8px 0 0; padding: 24px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 24px;">🎫 Event Pass Confirmed</h1>
                      </div>
                      <div style="background: white; padding: 32px; border-radius: 0 0 8px 8px; border: 1px solid #e5e7eb;">
                        <p style="color: #374151; font-size: 16px;">Hello <strong>%s</strong>,</p>
                        <p style="color: #374151;">Your registration for <strong>%s</strong> has been confirmed. Please find your event pass attached.</p>
                        <div style="background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 16px; margin: 24px 0;">
                          <p style="margin: 4px 0; color: #1e40af;"><strong>Event:</strong> %s</p>
                          <p style="margin: 4px 0; color: #1e40af;"><strong>Date:</strong> %s</p>
                          <p style="margin: 4px 0; color: #1e40af;"><strong>Location:</strong> %s</p>
                          <p style="margin: 4px 0; color: #1e40af;"><strong>Pass Code:</strong> <code style="font-size: 14px;">%s</code></p>
                        </div>
                        <p style="color: #6b7280; font-size: 14px;">Present your pass (attached PDF) at the event entrance.</p>
                        <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                        <p style="color: #9ca3af; font-size: 12px; text-align: center;">This is an automated message. Please do not reply.</p>
                      </div>
                    </div>
                    """.formatted(userName, eventTitle, eventTitle, dateStr, location != null ? location : "TBD", passCode);

            helper.setText(html, true);
            helper.addAttachment("EventPass-" + passCode + ".pdf", () ->
                    new java.io.ByteArrayInputStream(pdfBytes), "application/pdf");

            mailSender.send(message);
            log.info("Event pass email sent to {}", to);
        } catch (MessagingException e) {
            log.warn("Failed to send pass email to {}: {}", to, e.getMessage());
        }
    }
}

