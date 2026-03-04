package tn.spring.quiz.Services;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("chahabsarah@gmail.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);
        System.out.println("Mail Send...");
    }



    public void sendCertificateEmail(String toEmail, byte[] pdfBytes)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("chahabsarah@gmail.com");
        helper.setTo(toEmail);
        helper.setSubject("Your Course Certificate");
        helper.setText("Congratulations! Please find your certificate attached.");

        helper.addAttachment("certificate.pdf",
                () -> new java.io.ByteArrayInputStream(pdfBytes));

        mailSender.send(message);
    }
}