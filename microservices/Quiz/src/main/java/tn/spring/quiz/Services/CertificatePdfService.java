package tn.spring.quiz.Services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import tn.spring.quiz.Models.Certificate;
import tn.spring.quiz.Models.Course;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CertificatePdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public byte[] buildCourseCertificatePdf(Certificate certificate, String assessmentLabel) {
        Course course = certificate.getCourse();
        String recipientName = normalizeStudentName(certificate.getStudentName(), certificate.getStudentId().toString());
        String courseTitle = course != null && course.getTitle() != null && !course.getTitle().isBlank()
                ? course.getTitle()
                : "Untitled Course";
        String courseDescription = course != null && course.getDescription() != null && !course.getDescription().isBlank()
                ? course.getDescription()
                : "This course certificate was generated from the quiz module.";
        String courseDuration = course != null && course.getDuration() != null
                ? course.getDuration() + " hours"
                : "Not specified";
        String issueDate = certificate.getIssueDate() != null
                ? certificate.getIssueDate().format(DATE_FORMAT)
                : "Not specified";

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);
            document.setMargins(42, 48, 42, 48);

            document.add(
                    new Paragraph("Certificate of Completion")
                            .setFontSize(28)
                            .setBold()
                            .setFontColor(ColorConstants.BLUE)
                            .setTextAlignment(TextAlignment.CENTER)
            );

            document.add(
                    new Paragraph("Awarded for successfully completing the course assessment")
                            .setFontSize(12)
                            .setFontColor(ColorConstants.DARK_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(6)
            );

            document.add(
                    new Paragraph(recipientName)
                            .setFontSize(24)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(18)
            );

            document.add(
                    new Paragraph("This certificate confirms successful achievement in the linked course below.")
                            .setFontSize(13)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.DARK_GRAY)
                            .setMarginTop(8)
                            .setMarginBottom(18)
            );

            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{32, 68}))
                    .useAllAvailableWidth();
            detailsTable.setMarginTop(6);

            addRow(detailsTable, "Course", courseTitle);
            addRow(detailsTable, "Description", courseDescription);
            addRow(detailsTable, "Duration", courseDuration);
            addRow(detailsTable, "Assessment", normalizeAssessmentLabel(assessmentLabel));
            addRow(detailsTable, "Final Score", certificate.getFinalScore() + "%");
            addRow(detailsTable, "Issue Date", issueDate);
            addRow(detailsTable, "Certificate ID", "CERT-" + certificate.getId());

            document.add(detailsTable);

            document.add(
                    new Paragraph("Keep learning, keep building, and keep moving forward.")
                            .setFontSize(13)
                            .setItalic()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(28)
                            .setFontColor(ColorConstants.BLACK)
            );

            document.close();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException("Unable to generate certificate PDF", exception);
        }
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(
                new Cell()
                        .add(new Paragraph(label).setBold().setFontColor(ColorConstants.BLUE))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(10)
        );
        table.addCell(
                new Cell()
                        .add(new Paragraph(value))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(10)
        );
    }

    private String normalizeStudentName(String studentName, String studentId) {
        if (studentName != null && !studentName.isBlank()) {
            return studentName.trim();
        }

        String shortId = studentId.length() >= 8 ? studentId.substring(0, 8).toUpperCase() : studentId;
        return "Student " + shortId;
    }

    private String normalizeAssessmentLabel(String assessmentLabel) {
        if (assessmentLabel != null && !assessmentLabel.isBlank()) {
            return assessmentLabel.trim();
        }

        return "Course Completion Assessment";
    }
}
