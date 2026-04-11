package tn.spring.packagee.Controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.CreatePaymentRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PaymentRepository;
import tn.spring.packagee.Services.PaymentService;
import tn.spring.packagee.Services.VoucherService;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;
    private final PaymentRepository repository;
    private final VoucherService voucherService;
    public PaymentController(PaymentService service, PaymentRepository repository, VoucherService voucherService) {
        this.service = service;
        this.repository = repository;
        this.voucherService = voucherService;
    }

    @PostMapping
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest req) {
        return service.create(req);
    }

    @PostMapping("/{id}/confirm")
    public PaymentResponse confirm(@PathVariable Long id, @Valid @RequestBody ConfirmPaymentRequest req) {
        return service.confirm(id, req);
    }

    @PostMapping("/{id}/fail")
    public PaymentResponse fail(@PathVariable Long id) {
        return service.fail(id);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return service.getById(id);
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public List<PaymentResponse> list(

    ) {
        return service.list();
    }
    @GetMapping("/student/mypayment")
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<PaymentResponse> myPayments(
            @AuthenticationPrincipal String userId
    ) {
        return service.listByStudent(UUID.fromString(userId));
    }
    @GetMapping("/student/{studentId}")

    public List<PaymentResponse> byStudent(@PathVariable UUID studentId) {
        return service.listByStudent(studentId);
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/status")
    public PaymentResponse updateStatus(@PathVariable Long id, @RequestParam String status) {
        return service.updateStatus(id, status);
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteIfPending(id);
    }
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT')")
    @GetMapping(value = "/{id}/voucher", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadVoucher(@PathVariable Long id) {
        Payment p = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + id));

        if (p.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Voucher available only for SUCCESS payments.");
        }

        byte[] pdf = voucherService.generateVoucherPdf(p);
        String filename = (p.getVoucherNumber() != null ? p.getVoucherNumber() : ("voucher-" + id)) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}