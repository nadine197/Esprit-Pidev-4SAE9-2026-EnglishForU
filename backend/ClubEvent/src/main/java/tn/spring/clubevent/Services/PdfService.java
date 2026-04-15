package tn.spring.clubevent.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfService {

    private static final String QR_SECRET_KEY = "LinguaAcademy-SecurePass-Key-2026";
    private static final Font LOGO_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(0, 102, 255));
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(30, 41, 59));
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
    private static final Font LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(100, 116, 139));
    private static final Font VALUE_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, new BaseColor(15, 23, 42));
    private static final Font CODE_FONT = new Font(Font.FontFamily.COURIER, 18, Font.BOLD, new BaseColor(0, 102, 255));
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, new BaseColor(148, 163, 184));

    private static final BaseColor PRIMARY_COLOR = new BaseColor(0, 102, 255);
    private static final BaseColor SUCCESS_COLOR = new BaseColor(34, 197, 94);
    private static final BaseColor BACKGROUND_COLOR = new BaseColor(248, 250, 252);

    public byte[] generateEventPass(
            String userName,
            String eventTitle,
            LocalDateTime eventDate,
            String location,
            String clubName,
            String passCode
    ) throws DocumentException, WriterException, java.io.IOException {
        return generateEventPass(userName, eventTitle, eventDate, location, clubName, passCode, null, null);
    }

    public byte[] generateEventPass(
            String userName,
            String eventTitle,
            LocalDateTime eventDate,
            String location,
            String clubName,
            String passCode,
            Long eventId,
            String userId
    ) throws DocumentException, WriterException, java.io.IOException {

        Document document = new Document(PageSize.A5, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Paragraph brand = new Paragraph("🎓 LinguaAcademy", LOGO_FONT);
        brand.setAlignment(Element.ALIGN_CENTER);
        brand.setSpacingAfter(5);
        document.add(brand);

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(15);
        PdfPCell headerCell = new PdfPCell(new Phrase("🎟️ EVENT PASS", HEADER_FONT));
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setPadding(14);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);
        document.add(headerTable);

        Paragraph title = new Paragraph(eventTitle, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(95);
        infoTable.setWidths(new float[]{1.2f, 2f});
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);

        addInfoRow(infoTable, "👤 Participant", userName);
        addInfoRow(infoTable, "🏢 Club", clubName);
        addInfoRow(infoTable, "📅 Date & Time", eventDate != null
                ? eventDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy • HH:mm")) : "To Be Announced");
        addInfoRow(infoTable, "📍 Location", location != null ? location : "To Be Announced");

        document.add(infoTable);

        PdfPTable codeTable = new PdfPTable(1);
        codeTable.setWidthPercentage(85);
        codeTable.setSpacingBefore(15);
        codeTable.setSpacingAfter(15);

        PdfPCell codeLabel = new PdfPCell(new Phrase("PASS CODE", LABEL_FONT));
        codeLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
        codeLabel.setBorder(Rectangle.NO_BORDER);
        codeLabel.setBackgroundColor(BACKGROUND_COLOR);
        codeLabel.setPadding(8);
        codeTable.addCell(codeLabel);

        PdfPCell codeCell = new PdfPCell(new Phrase(passCode, CODE_FONT));
        codeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        codeCell.setPadding(15);
        codeCell.setBackgroundColor(new BaseColor(239, 246, 255));
        codeCell.setBorderColor(PRIMARY_COLOR);
        codeCell.setBorderWidth(2f);
        codeTable.addCell(codeCell);
        document.add(codeTable);

        if (eventId != null && userId != null) {
            String qrData = generateSecureQRData(eventId, userId, passCode);
            byte[] qrCodeImage = generateQRCodeImage(qrData, 200, 200);

            Image qrImage = Image.getInstance(qrCodeImage);
            qrImage.scaleToFit(150, 150);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(10);
            qrImage.setSpacingAfter(10);
            document.add(qrImage);

            Paragraph qrLabel = new Paragraph("Scan to verify ticket authenticity",
                    new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, new BaseColor(100, 116, 139)));
            qrLabel.setAlignment(Element.ALIGN_CENTER);
            qrLabel.setSpacingAfter(10);
            document.add(qrLabel);
        }

        Paragraph instructions = new Paragraph("⚡ Present this pass at the event entrance for entry",
                new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, new BaseColor(71, 85, 105)));
        instructions.setAlignment(Element.ALIGN_CENTER);
        instructions.setSpacingAfter(8);
        document.add(instructions);

        Paragraph footer = new Paragraph("This is your official event pass. Keep it safe.", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        Paragraph generated = new Paragraph("Generated on " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")), SMALL_FONT);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingBefore(3);
        document.add(generated);

        document.close();
        return out.toByteArray();
    }

    private String generateSecureQRData(Long eventId, String userId, String passCode) {
        try {
            String data = eventId + "|" + userId + "|" + passCode;
            String signature = generateHMAC(data, QR_SECRET_KEY);
            return data + "|" + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secure QR data", e);
        }
    }

    private String generateHMAC(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    private byte[] generateQRCodeImage(String data, int width, int height) throws WriterException, java.io.IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setBackgroundColor(BACKGROUND_COLOR);
        labelCell.setPadding(10);
        labelCell.setPaddingLeft(12);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setBackgroundColor(BACKGROUND_COLOR);
        valueCell.setPadding(10);
        table.addCell(valueCell);
    }

    public String generateQRCodeBase64(Long eventId, String userId, String passCode) {
        try {
            String qrData = generateSecureQRData(eventId, userId, passCode);
            byte[] qrCodeImage = generateQRCodeImage(qrData, 250, 250);
            return Base64.getEncoder().encodeToString(qrCodeImage);
        } catch (Exception e) {
            System.err.println("Failed to generate QR code base64: " + e.getMessage());
            return null;
        }
    }

    public byte[] generateReceipt(
            String userName,
            String eventTitle,
            LocalDateTime eventDate,
            Double amount,
            LocalDateTime paidAt,
            String transactionId,
            String passCode
    ) throws DocumentException {

        Document document = new Document(PageSize.A5, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Paragraph brand = new Paragraph("🎓 LinguaAcademy", LOGO_FONT);
        brand.setAlignment(Element.ALIGN_CENTER);
        brand.setSpacingAfter(5);
        document.add(brand);

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(15);
        PdfPCell headerCell = new PdfPCell(new Phrase("📄 PAYMENT RECEIPT", HEADER_FONT));
        headerCell.setBackgroundColor(SUCCESS_COLOR);
        headerCell.setPadding(14);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);
        document.add(headerTable);

        Paragraph title = new Paragraph(eventTitle, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable statusTable = new PdfPTable(1);
        statusTable.setWidthPercentage(70);
        statusTable.setSpacingBefore(5);
        statusTable.setSpacingAfter(20);
        PdfPCell statusCell = new PdfPCell(new Phrase("✓ PAYMENT SUCCESSFUL",
                new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, SUCCESS_COLOR)));
        statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        statusCell.setPadding(12);
        statusCell.setBackgroundColor(new BaseColor(240, 253, 244));
        statusCell.setBorderColor(SUCCESS_COLOR);
        statusCell.setBorderWidth(2f);
        statusTable.addCell(statusCell);
        document.add(statusTable);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(95);
        infoTable.setWidths(new float[]{1.3f, 2f});
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(15);

        addReceiptRow(infoTable, "👤 Customer", userName);
        addReceiptRow(infoTable, "📅 Event Date", eventDate != null
                ? eventDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy • HH:mm")) : "To Be Announced");
        addReceiptRow(infoTable, "💰 Amount Paid", String.format("%.2f TND", amount));
        addReceiptRow(infoTable, "📆 Payment Date", paidAt != null
                ? paidAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm")) : "N/A");
        addReceiptRow(infoTable, "🔖 Transaction ID", transactionId != null ? transactionId.substring(0, Math.min(25, transactionId.length())) : "N/A");
        if (passCode != null) {
            addReceiptRow(infoTable, "🎟️ Pass Code", passCode);
        }

        document.add(infoTable);

        Paragraph footer = new Paragraph("Thank you for your purchase! Your event pass has been sent to your email.", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(15);
        document.add(footer);

        Paragraph contact = new Paragraph("For support, please contact support@linguaacademy.com", SMALL_FONT);
        contact.setAlignment(Element.ALIGN_CENTER);
        contact.setSpacingBefore(3);
        document.add(contact);

        Paragraph generated = new Paragraph("Receipt generated on " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")), SMALL_FONT);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingBefore(5);
        document.add(generated);

        document.close();
        return out.toByteArray();
    }

    private void addReceiptRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(new BaseColor(226, 232, 240));
        labelCell.setBorderWidth(1f);
        labelCell.setPadding(10);
        labelCell.setPaddingLeft(12);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(new BaseColor(226, 232, 240));
        valueCell.setBorderWidth(1f);
        valueCell.setPadding(10);
        table.addCell(valueCell);
    }
}

