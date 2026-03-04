package tn.spring.packagee.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.CreatePaymentRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Entities.Transaction;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TransactionStatus;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PaymentRepository;
import tn.spring.packagee.Repositories.TransactionRepository;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService  {

    private final PaymentRepository paymentRepo;
    private final TransactionRepository txRepo;

    public PaymentService(PaymentRepository paymentRepo, TransactionRepository txRepo) {
        this.paymentRepo = paymentRepo;
        this.txRepo = txRepo;
    }

    public PaymentResponse create(CreatePaymentRequest req) {
        Payment p = new Payment();
        p.setStudentId(req.getStudentId());
        p.setTargetType(req.getTargetType());
        p.setTargetId(req.getTargetId());
        p.setAmountOriginal(req.getAmountOriginal());
        p.setDiscountAmount(req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO);

        BigDecimal finalAmount = p.getAmountOriginal().subtract(p.getDiscountAmount());
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        p.setAmountFinal(finalAmount);
        p.setCurrency(req.getCurrency() != null ? req.getCurrency() : "TND");
        p.setPaymentMethod(req.getPaymentMethod());
        p.setStatus(PaymentStatus.PENDING);

        p = paymentRepo.save(p);
        return toResponse(p);
    }

    public PaymentResponse confirm(Long paymentId, ConfirmPaymentRequest req) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        p.setStatus(PaymentStatus.SUCCESS);

        Transaction tx = new Transaction();
        tx.setPaymentId(p.getId());
        tx.setProvider(req.getProvider());
        tx.setProviderRef(req.getProviderRef());
        tx.setStatus(TransactionStatus.CONFIRMED);
        tx.setResponsePayload(req.getResponsePayload());

        txRepo.save(tx);
        return toResponse(p);
    }

    public PaymentResponse fail(Long paymentId, String reason) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        p.setStatus(PaymentStatus.FAILED);

        Transaction tx = new Transaction();
        tx.setPaymentId(p.getId());
        tx.setProvider("UNKNOWN");
        tx.setProviderRef("FAIL-" + p.getId());
        tx.setStatus(TransactionStatus.REJECTED);
        tx.setResponsePayload(reason);

        txRepo.save(tx);
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Long paymentId) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByStudent(Long studentId) {
        return paymentRepo.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse toResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setStudentId(p.getStudentId());
        r.setTargetType(p.getTargetType());
        r.setTargetId(p.getTargetId());
        r.setAmountOriginal(p.getAmountOriginal());
        r.setDiscountAmount(p.getDiscountAmount());
        r.setAmountFinal(p.getAmountFinal());
        r.setCurrency(p.getCurrency());
        r.setStatus(p.getStatus());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}