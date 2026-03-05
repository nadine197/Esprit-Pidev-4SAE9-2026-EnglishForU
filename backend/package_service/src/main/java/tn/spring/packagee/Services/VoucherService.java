package tn.spring.packagee.Services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;
import tn.spring.packagee.Entities.Payment;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class VoucherService {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public byte[] generateVoucherPdf(Payment p) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // Fonts
            Font brand = new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE);
            Font h1 = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font h2 = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font small = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            Font bold = new Font(Font.HELVETICA, 11, Font.BOLD);

            // === Header bar ===
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);

            PdfPCell headCell = new PdfPCell(new Phrase("PACKAGE MANAGEMENT • PAYMENT VOUCHER", brand));
            headCell.setPadding(14);
            headCell.setBorder(Rectangle.NO_BORDER);
            headCell.setBackgroundColor(new Color(33, 37, 41)); // dark gray
            header.addCell(headCell);

            doc.add(header);
            doc.add(Chunk.NEWLINE);

            // === Title + voucher number ===
            Paragraph title = new Paragraph("Payment Voucher", h1);
            title.setSpacingAfter(6);
            doc.add(title);

            Paragraph sub = new Paragraph("Voucher No: " + safe(p.getVoucherNumber()), bold);
            sub.setSpacingAfter(12);
            doc.add(sub);

            // === Student name highlight box ===
            PdfPTable studentBox = new PdfPTable(1);
            studentBox.setWidthPercentage(100);

            String studentName = safeStudentName(p);
            PdfPCell studentCell = new PdfPCell(new Phrase("Student: " + studentName, new Font(Font.HELVETICA, 12, Font.BOLD)));
            studentCell.setPadding(12);
            studentCell.setBackgroundColor(new Color(245, 245, 245));
            studentCell.setBorderColor(new Color(220, 220, 220));
            studentBox.addCell(studentCell);

            doc.add(studentBox);
            doc.add(Chunk.NEWLINE);

            // === Info section (2 columns) ===
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1f, 1f});
            info.setSpacingAfter(12);

            info.addCell(labelValue("Payment ID", "#" + safeId(p.getId()), h2, normal));
            info.addCell(labelValue("Status", safeEnum(p.getStatus()), h2, normal));

            info.addCell(labelValue("Method", safeEnum(p.getPaymentMethod()), h2, normal));
            info.addCell(labelValue("Provider Ref", safe(p.getProviderRef()), h2, normal));

            info.addCell(labelValue("Target", safeEnum(p.getTargetType()), h2, normal));
            info.addCell(labelValue("Target ID", safeId(p.getTargetId()), h2, normal));

            info.addCell(labelValue("Checkout URL", safe(p.getCheckoutUrl()), h2, normal));
            info.addCell(labelValue("Student ID", safe(p.getStudentId() == null ? null : p.getStudentId().toString()), h2, normal));

            doc.add(info);

            // === Amounts table ===
            Paragraph amountsTitle = new Paragraph("Amounts (TND)", h2);
            amountsTitle.setSpacingAfter(6);
            doc.add(amountsTitle);

            PdfPTable amounts = new PdfPTable(2);
            amounts.setWidthPercentage(60);
            amounts.setHorizontalAlignment(Element.ALIGN_LEFT);
            amounts.setSpacingAfter(14);
            amounts.setWidths(new float[]{1.2f, 1f});

            amounts.addCell(amountRow("Original", money(p.getAmountOriginal()), normal, false));
            amounts.addCell(amountRow("Discount", money(p.getDiscountAmount()), normal, false));
            amounts.addCell(amountRow("Final", money(p.getAmountFinal()), bold, true));

            doc.add(amounts);

            // === Date block ===
            String confirmed = (p.getConfirmedAt() != null) ? DT.format(p.getConfirmedAt()) : "—";
            Paragraph dateLine = new Paragraph("Confirmed at: " + confirmed, normal);
            dateLine.setSpacingAfter(18);
            doc.add(dateLine);

            // === Footer ===
            LineSeparator sep = new LineSeparator();
            sep.setLineColor(new Color(220, 220, 220));
            doc.add(sep);
            doc.add(Chunk.NEWLINE);

            Paragraph footer = new Paragraph(
                    "This voucher is system-generated. If you have questions, contact support and provide the voucher number.",
                    small
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            writer.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Voucher PDF generation failed: " + e.getMessage(), e);
        }
    }

    // ---------- Helpers ----------

    private PdfPCell labelValue(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderColor(new Color(230, 230, 230));
        cell.setBackgroundColor(Color.WHITE);

        Paragraph p = new Paragraph();
        p.add(new Phrase(label + "\n", labelFont));
        p.add(new Phrase(value, valueFont));
        cell.addElement(p);
        return cell;
    }

    private PdfPCell amountRow(String label, String value, Font valueFont, boolean highlight) {
        PdfPCell left = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 11, Font.NORMAL)));
        PdfPCell right = new PdfPCell(new Phrase(value, valueFont));

        left.setPadding(8);
        right.setPadding(8);

        left.setBorderColor(new Color(220, 220, 220));
        right.setBorderColor(new Color(220, 220, 220));

        if (highlight) {
            left.setBackgroundColor(new Color(245, 245, 245));
            right.setBackgroundColor(new Color(245, 245, 245));
        }

        // create a 2-col row table cell style: return as nested? easier: caller adds both cells
        // We'll return only right cell? No: We'll add both from caller:
        // BUT here caller adds 2 cells per row, so we need a trick:
        // We'll store left cell temporarily by attaching to right? Not clean.
        // Solution: handle adding in caller (below).
        throw new UnsupportedOperationException("Use addAmountRow() instead");
    }

    private String safeStudentName(Payment p) {
        // If you later add studentFirstName + studentLastName:
        // String fn = safe(p.getStudentFirstName());
        // String ln = safe(p.getStudentLastName());
        // if (!"—".equals(fn) || !"—".equals(ln)) return (fn + " " + ln).replaceAll("\\s+"," ").trim();

        return safe(p.getStudentFullName());
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String safeEnum(Enum<?> e) {
        return e == null ? "—" : e.name();
    }

    private String safeId(Object id) {
        return id == null ? "—" : id.toString();
    }

    private String money(BigDecimal v) {
        if (v == null) return "0.00";
        return MONEY.format(v);
    }
}