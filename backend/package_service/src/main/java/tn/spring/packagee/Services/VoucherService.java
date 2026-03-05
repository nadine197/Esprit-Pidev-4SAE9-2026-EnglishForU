package tn.spring.packagee.Services;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import tn.spring.packagee.Entities.Payment;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class VoucherService {

    public byte[] generateVoucherPdf(Payment p) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out); // ✅ FIXED

            doc.open();

            Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 12, Font.NORMAL);

            doc.add(new Paragraph("Payment Voucher", title));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Voucher Number: " + safe(p.getVoucherNumber()), bold));
            doc.add(new Paragraph("Payment ID: #" + p.getId(), normal));
            doc.add(new Paragraph("Status: " + safeEnum(p.getStatus()), normal));
            doc.add(new Paragraph("Method: " + safeEnum(p.getPaymentMethod()), normal));
            doc.add(new Paragraph("Provider Ref: " + safe(p.getProviderRef()), normal));
            doc.add(new Paragraph("Student: " + safe(p.getStudentFullName()), normal));
            doc.add(new Paragraph("Target: " + safeEnum(p.getTargetType()) + " (ID " + p.getTargetId() + ")", normal));

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Amounts", bold));
            doc.add(new Paragraph("Original: " + safeMoney(p.getAmountOriginal()) + " TND", normal));
            doc.add(new Paragraph("Discount: " + safeMoney(p.getDiscountAmount()) + " TND", normal));
            doc.add(new Paragraph("Final: " + safeMoney(p.getAmountFinal()) + " TND", bold));

            doc.add(new Paragraph(" "));
            String date = (p.getConfirmedAt() != null)
                    ? p.getConfirmedAt().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    : "—";
            doc.add(new Paragraph("Confirmed at: " + date, normal));

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Thank you.", normal));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Voucher PDF generation failed: " + e.getMessage(), e);
        }
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