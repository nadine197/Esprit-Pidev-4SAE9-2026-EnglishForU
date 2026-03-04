package tn.spring.quiz.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.quiz.Models.Certificate;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CertificatePdfService {

    public byte[] generateCertificatePdf(Certificate cert, String userName) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);

            // ✅ FORMAT PAYSAGE
            Document document = new Document(pdf, PageSize.A4.rotate());

            // ================= BORDER =================
            PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
            canvas.setLineWidth(4f);
            canvas.setStrokeColor(ColorConstants.BLUE);
            canvas.rectangle(20, 20,
                    PageSize.A4.rotate().getWidth() - 40,
                    PageSize.A4.rotate().getHeight() - 40);
            canvas.stroke();

            // ================= TITLE =================
            Paragraph title = new Paragraph("CERTIFICATE OF COMPLETION")
                    .setBold()
                    .setFontSize(36)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE);

            // ================= NAME =================
            Paragraph name = new Paragraph(userName)
                    .setBold()
                    .setFontSize(30)
                    .setTextAlignment(TextAlignment.CENTER);

            // ================= BODY =================
            Paragraph body = new Paragraph(
                    "has successfully completed the course"
            ).setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18);

            Paragraph course = new Paragraph(cert.getCourse().getTitle())
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.CENTER);



            // ================= DATE =================
            Paragraph date = new Paragraph(
                    "Issued on: " +
                            cert.getIssueDate().format(DateTimeFormatter.ISO_DATE)
            ).setTextAlignment(TextAlignment.CENTER);

            // ================= QR CODE =================
            Image qrImage = new Image(
                    ImageDataFactory.create(generateQr(cert.getId()))
            ).scaleToFit(120, 120);

            qrImage.setHorizontalAlignment(
                    com.itextpdf.layout.properties.HorizontalAlignment.CENTER
            );

            // ================= SIGNATURE =================
            Paragraph signature = new Paragraph("Delivered by JobBoard")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(14);

            // ================= ADD ELEMENTS =================
            document.add(title);
            document.add(new Paragraph("\n"));
            document.add(name);
            document.add(new Paragraph("\n"));
            document.add(body);
            document.add(course);
            document.add(new Paragraph("\n"));
            document.add(date);
            document.add(new Paragraph("\n"));
            document.add(qrImage);
            document.add(new Paragraph("\n\n"));
            document.add(signature);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating certificate PDF", e);
        }
    }

    // ================= QR GENERATOR =================
    private byte[] generateQr(Long certId) throws Exception {

        String verificationUrl =
                "http://localhost:8080/api/certificates/verify/" + certId;

        QRCodeWriter writer = new QRCodeWriter();
        var bitMatrix = writer.encode(
                verificationUrl,
                BarcodeFormat.QR_CODE,
                200,
                200
        );

        ByteArrayOutputStream pngOutput = new ByteArrayOutputStream();
        com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(
                bitMatrix,
                "PNG",
                pngOutput
        );

        return pngOutput.toByteArray();
    }
}