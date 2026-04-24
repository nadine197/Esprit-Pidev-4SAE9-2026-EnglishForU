package tn.spring.packagee.Services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;
import tn.spring.packagee.Entities.Payment;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class VoucherService {

    public byte[] generateVoucherPdf(Payment p) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);

            doc.open();

            // =========================
            // FONTS (modern hierarchy)
            // =========================
            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD);
            Font subtitle = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GRAY);
            Font sectionTitle = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font bold = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font small = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);

            // =========================
            // HEADER (BRAND STYLE)
            // =========================
            Paragraph header = new Paragraph("ENGLISH FOR U", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            doc.add(header);

            Paragraph subtitleText = new Paragraph("PAYMENT VOUCHER", subtitle);
            subtitleText.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitleText);

            doc.add(new Paragraph(" "));

            // divider
            LineSeparator ls = new LineSeparator();
            doc.add(new Chunk(ls));

            doc.add(new Paragraph(" "));

            // =========================
            // VOUCHER INFO BLOCK
            // =========================
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);

            addRow(infoTable, "Voucher #", safe(p.getVoucherNumber()), bold, normal);
            addRow(infoTable, "Payment ID", "#" + p.getId(), bold, normal);
            addRow(infoTable, "Student", safe(p.getStudentFullName()), bold, normal);
            addRow(infoTable, "Target", safe(p.getTargetName()), bold, normal);
            addRow(infoTable, "Method", safeEnum(p.getPaymentMethod()), bold, normal);
            addRow(infoTable, "Provider Ref", safe(p.getProviderRef()), bold, normal);
            addRow(infoTable, "Status", safeEnum(p.getStatus()), bold, normal);

            doc.add(infoTable);

            doc.add(new Paragraph(" "));
            doc.add(new Chunk(ls));
            doc.add(new Paragraph(" "));

            // =========================
            // AMOUNT SECTION (CARD STYLE)
            // =========================
            Paragraph amountTitle = new Paragraph("Payment Summary", sectionTitle);
            doc.add(amountTitle);

            PdfPTable amountTable = new PdfPTable(2);
            amountTable.setWidthPercentage(100);
            amountTable.setSpacingBefore(10);

            addMoneyRow(amountTable, "Original Amount", p.getAmountOriginal(), normal);
            addMoneyRow(amountTable, "Discount", p.getDiscountAmount(), normal);

            PdfPCell finalLabel = new PdfPCell(new Phrase("Final Amount", bold));
            PdfPCell finalValue = new PdfPCell(new Phrase(
                    safeMoney(p.getAmountFinal()) + " TND", bold));

            finalLabel.setBorder(Rectangle.NO_BORDER);
            finalValue.setBorder(Rectangle.NO_BORDER);

            amountTable.addCell(finalLabel);
            amountTable.addCell(finalValue);

            doc.add(amountTable);

            doc.add(new Paragraph(" "));
            doc.add(new Chunk(ls));
            doc.add(new Paragraph(" "));

            // =========================
            // DATE SECTION
            // =========================
            String date = (p.getConfirmedAt() != null)
                    ? p.getConfirmedAt().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm"))
                    : "—";

            Paragraph dateP = new Paragraph("Confirmed at: " + date, normal);
            doc.add(dateP);

            doc.add(new Paragraph(" "));

            // =========================
            // FOOTER (PRO UX TOUCH)
            // =========================
            Paragraph thanks = new Paragraph("Thank you for your payment.", bold);
            thanks.setAlignment(Element.ALIGN_CENTER);
            doc.add(thanks);

            Paragraph footer = new Paragraph(
                    "This is an automatically generated document. Keep it for your records.",
                    small
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Voucher PDF generation failed: " + e.getMessage(), e);
        }


    }
    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));

        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);

        table.addCell(c1);
        table.addCell(c2);
    }

    private void addMoneyRow(PdfPTable table, String label, BigDecimal value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        PdfPCell c2 = new PdfPCell(new Phrase(safeMoney(value) + " TND", font));

        c1.setBorder(Rectangle.NO_BORDER);
        c2.setBorder(Rectangle.NO_BORDER);

        table.addCell(c1);
        table.addCell(c2);
    }
    private String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String safeEnum(Enum<?> e) {
        return e == null ? "—" : e.name();
    }

    private String safeMoney(Object v) {
        return v == null ? "0.00" : v.toString();
    }
}