package tn.spring.clubevent.Controllers;

import com.itextpdf.text.DocumentException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.clubevent.Enums.RequestStatus;
import tn.spring.clubevent.Models.Event;
import tn.spring.clubevent.Models.EventParticipation;
import tn.spring.clubevent.Repositories.EventParticipationRepository;
import tn.spring.clubevent.Repositories.EventRepository;
import tn.spring.clubevent.Services.EmailService;
import tn.spring.clubevent.Services.PdfService;
import tn.spring.clubevent.Services.StripeService;
import tn.spring.clubevent.Services.SupabaseStorageService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class PaymentController {

    private final StripeService stripeService;
    private final PdfService pdfService;
    private final EmailService emailService;
    private final SupabaseStorageService supabaseStorageService;
    private final EventRepository eventRepository;
    private final EventParticipationRepository participationRepository;

    @PostMapping("/{id}/payment/intent")
    public ResponseEntity<?> createPaymentIntent(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        try {
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            if (!event.isPaid()) {
                return ResponseEntity.badRequest().body(Map.of("error", "This event is free, no payment required"));
            }

            String userEmail = body.getOrDefault("userEmail", "");
            double price = event.getPrice() != null ? event.getPrice() : 0.0;

            double priceInUsd = price * 0.32;

            Map<String, String> result = stripeService.createPaymentIntent(priceInUsd, "usd", event.getTitle(), userEmail);
            return ResponseEntity.ok(result);

        } catch (StripeException e) {
            return ResponseEntity.status(502).body(Map.of("error", "Stripe error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/payment/confirm")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long id,
            @RequestParam String userId,
            @RequestBody Map<String, Object> body
    ) {
        try {
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            String paymentIntentId = (String) body.get("paymentIntentId");
            String userName = (String) body.getOrDefault("userName", "Participant");
            String userEmail = (String) body.getOrDefault("userEmail", "");

            Double latitude = body.get("latitude") != null
                    ? ((Number) body.get("latitude")).doubleValue() : null;
            Double longitude = body.get("longitude") != null
                    ? ((Number) body.get("longitude")).doubleValue() : null;

            if (paymentIntentId == null || paymentIntentId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing paymentIntentId"));
            }

            PaymentIntent intent = stripeService.retrievePaymentIntent(paymentIntentId);
            if (!"succeeded".equals(intent.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment not succeeded, status: " + intent.getStatus()));
            }

            if (participationRepository.findByEventIdAndUserId(id, userId).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Already registered for this event"));
            }

            String passCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            EventParticipation participation = EventParticipation.builder()
                    .eventId(id)
                    .userId(userId)
                    .userName(userName)
                    .status(RequestStatus.ACCEPTED)
                    .stripePaymentIntentId(paymentIntentId)
                    .transactionLatitude(latitude)
                    .transactionLongitude(longitude)
                    .paidAt(LocalDateTime.now())
                    .amountPaid(event.getPrice())
                    .passCode(passCode)
                    .build();

            participationRepository.save(participation);

            if (!userEmail.isBlank()) {
                try {
                    byte[] pdf = pdfService.generateEventPass(
                            userName,
                            event.getTitle(),
                            event.getEventDate(),
                            event.getLocationName() != null ? event.getLocationName() : event.getLocation(),
                            event.getClubName(),
                            passCode,
                            id,
                            userId
                    );
                    emailService.sendPassEmail(userEmail, userName, event.getTitle(),
                            event.getEventDate(),
                            event.getLocationName() != null ? event.getLocationName() : event.getLocation(),
                            passCode, pdf);
                } catch (Exception ignored) {
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Payment confirmed and registration successful",
                    "passCode", passCode
            ));

        } catch (StripeException e) {
            return ResponseEntity.status(502).body(Map.of("error", "Stripe error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<?> getMyPurchases(@RequestParam String userId) {
        try {
            List<EventParticipation> purchases = participationRepository
                    .findByUserIdAndAmountPaidIsNotNullOrderByPaidAtDesc(userId);

            List<Map<String, Object>> result = purchases.stream().map(p -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", p.getId());
                item.put("eventId", p.getEventId());
                item.put("passCode", p.getPassCode());
                item.put("amountPaid", p.getAmountPaid());
                item.put("paidAt", p.getPaidAt());
                item.put("status", p.getStatus().name());
                item.put("stripePaymentIntentId", p.getStripePaymentIntentId());

                eventRepository.findById(p.getEventId()).ifPresent(event -> {
                    item.put("eventTitle", event.getTitle());
                    item.put("eventDate", event.getEventDate());
                    item.put("eventLocation", event.getLocationName() != null ? event.getLocationName() : event.getLocation());
                    item.put("eventImageUrl", event.getImageUrl());
                    item.put("clubName", event.getClubName());
                });

                if (p.getPassCode() != null) {
                    String qrBase64 = pdfService.generateQRCodeBase64(p.getEventId(), userId, p.getPassCode());
                    if (qrBase64 != null) {
                        item.put("qrCodeBase64", qrBase64);
                    }
                }

                return item;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{eventId}/pass")
    public ResponseEntity<?> downloadPass(
            @PathVariable Long eventId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "false") boolean view
    ) {
        try {
            EventParticipation participation = participationRepository
                    .findByEventIdAndUserId(eventId, userId)
                    .orElseThrow(() -> new RuntimeException("No participation found for this event"));

            if (participation.getPassCode() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No pass code available - this may be a free event"));
            }

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            byte[] pdf = pdfService.generateEventPass(
                    participation.getUserName(),
                    event.getTitle(),
                    event.getEventDate(),
                    event.getLocationName() != null ? event.getLocationName() : event.getLocation(),
                    event.getClubName(),
                    participation.getPassCode(),
                    eventId,
                    userId
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            if (view) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"EventPass-" + participation.getPassCode() + ".pdf\"");
            } else {
                headers.setContentDispositionFormData("attachment", "EventPass-" + participation.getPassCode() + ".pdf");
            }
            headers.setContentLength(pdf.length);

            return ResponseEntity.ok().headers(headers).body(pdf);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate PDF: " + e.getMessage()));
        }
    }

    @GetMapping("/{eventId}/receipt")
    public ResponseEntity<?> downloadReceipt(
            @PathVariable Long eventId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "false") boolean view
    ) {
        try {
            EventParticipation participation = participationRepository
                    .findByEventIdAndUserId(eventId, userId)
                    .orElseThrow(() -> new RuntimeException("No participation found for this event"));

            if (participation.getAmountPaid() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No payment record found - this may be a free event"));
            }

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            byte[] pdf = pdfService.generateReceipt(
                    participation.getUserName(),
                    event.getTitle(),
                    event.getEventDate(),
                    participation.getAmountPaid(),
                    participation.getPaidAt(),
                    participation.getStripePaymentIntentId(),
                    participation.getPassCode()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String txId = participation.getStripePaymentIntentId() != null
                    ? participation.getStripePaymentIntentId().substring(0, Math.min(20, participation.getStripePaymentIntentId().length()))
                    : "receipt";
            if (view) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Receipt-" + txId + ".pdf\"");
            } else {
                headers.setContentDispositionFormData("attachment", "Receipt-" + txId + ".pdf");
            }
            headers.setContentLength(pdf.length);

            return ResponseEntity.ok().headers(headers).body(pdf);

        } catch (DocumentException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate PDF: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{eventId}/pass-url")
    public ResponseEntity<?> getPassUrl(
            @PathVariable Long eventId,
            @RequestParam String userId
    ) {
        try {
            EventParticipation participation = participationRepository
                    .findByEventIdAndUserId(eventId, userId)
                    .orElseThrow(() -> new RuntimeException("No participation found for this event"));

            if (participation.getPassCode() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No pass code available"));
            }

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            byte[] pdf = pdfService.generateEventPass(
                    participation.getUserName(),
                    event.getTitle(),
                    event.getEventDate(),
                    event.getLocationName() != null ? event.getLocationName() : event.getLocation(),
                    event.getClubName(),
                    participation.getPassCode(),
                    eventId,
                    userId
            );

            String fileName = "passes/" + eventId + "_" + participation.getPassCode() + ".pdf";
            String url = supabaseStorageService.uploadPdf(pdf, fileName);

            return ResponseEntity.ok(Map.of("url", url));

        } catch (DocumentException e) {
            return ResponseEntity.status(500).body(Map.of("error", "PDF generation failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{eventId}/receipt-url")
    public ResponseEntity<?> getReceiptUrl(
            @PathVariable Long eventId,
            @RequestParam String userId
    ) {
        try {
            EventParticipation participation = participationRepository
                    .findByEventIdAndUserId(eventId, userId)
                    .orElseThrow(() -> new RuntimeException("No participation found for this event"));

            if (participation.getAmountPaid() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No payment record found"));
            }

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            byte[] pdf = pdfService.generateReceipt(
                    participation.getUserName(),
                    event.getTitle(),
                    event.getEventDate(),
                    participation.getAmountPaid(),
                    participation.getPaidAt(),
                    participation.getStripePaymentIntentId(),
                    participation.getPassCode()
            );

            String txId = participation.getStripePaymentIntentId() != null
                    ? participation.getStripePaymentIntentId().substring(0, Math.min(20, participation.getStripePaymentIntentId().length()))
                    : "receipt";
            String fileName = "receipts/" + eventId + "_" + txId + ".pdf";
            String url = supabaseStorageService.uploadPdf(pdf, fileName);

            return ResponseEntity.ok(Map.of("url", url));

        } catch (DocumentException e) {
            return ResponseEntity.status(500).body(Map.of("error", "PDF generation failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

