package tn.spring.packagee.Controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.CreatePaymentRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.Services.PaymentService;


import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
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
    public PaymentResponse fail(@PathVariable Long id, @RequestParam String reason) {
        return service.fail(id, reason);
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/student/{studentId}")
    public List<PaymentResponse> byStudent(@PathVariable Long studentId) {
        return service.listByStudent(studentId);
    }
}