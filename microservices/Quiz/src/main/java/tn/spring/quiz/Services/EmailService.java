package tn.spring.quiz.Services;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);
        System.out.println("Mail Send...");
    }



    public void sendCertificateEmail(
            String toEmail,
            String studentName,
            String courseTitle,
            String downloadUrl,
            byte[] pdfBytes,
            byte[] qrCodeBytes,
            String attachmentName
    )
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your Course Certificate");
        helper.setText(buildCertificateEmailBody(studentName, courseTitle, downloadUrl), true);

        helper.addInline("certificateQrCode", new ByteArrayResource(qrCodeBytes), "image/png");
        helper.addAttachment(attachmentName,
                () -> new java.io.ByteArrayInputStream(pdfBytes));

        mailSender.send(message);
    }

    private String buildCertificateEmailBody(String studentName, String courseTitle, String downloadUrl) {
        String safeStudentName = studentName != null && !studentName.isBlank() ? studentName.trim() : "Learner";
        String safeCourseTitle = courseTitle != null && !courseTitle.isBlank() ? courseTitle.trim() : "your course";

        return """
                <div style="font-family: Arial, sans-serif; color: #0f172a; line-height: 1.6;">
                  <h2 style="margin-bottom: 8px; color: #2563eb;">Congratulations %s</h2>
                  <p>You successfully completed the assessment for <strong>%s</strong>.</p>
                  <p>Your course certificate is attached to this email.</p>
                  <p>Scan the QR code below any time to open and download your certificate PDF.</p>
                  <div style="margin: 24px 0;">
                    <img src="cid:certificateQrCode" alt="Certificate QR code" style="width: 220px; height: 220px; border-radius: 18px; border: 1px solid #cbd5e1; padding: 12px; background: #ffffff;" />
                  </div>
                  <p style="margin-top: 8px;">
                    <a href="%s" style="display: inline-block; padding: 10px 18px; background: #2563eb; color: #ffffff; text-decoration: none; border-radius: 12px; font-weight: 700;">
                      Open Certificate PDF
                    </a>
                  </p>
                  <p style="margin-top: 24px; color: #475569;">Keep learning and building. Your progress is worth celebrating.</p>
                </div>
                """.formatted(safeStudentName, safeCourseTitle, downloadUrl);
    }
}
