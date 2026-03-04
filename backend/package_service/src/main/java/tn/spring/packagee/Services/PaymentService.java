package tn.spring.packagee.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.CreatePaymentRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Entities.PromoCode;
import tn.spring.packagee.Entities.Transaction;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TransactionStatus;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PaymentRepository;
import tn.spring.packagee.Repositories.PromoCodeRepository;
import tn.spring.packagee.Repositories.TransactionRepository;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService  {

    private final PaymentRepository paymentRepo;
    private final TransactionRepository txRepo;
    private final PromoCodeRepository promoRepo;

    public PaymentService(PaymentRepository paymentRepo,
                          TransactionRepository txRepo,
                          PromoCodeRepository promoRepo) {
        this.paymentRepo = paymentRepo;
        this.txRepo = txRepo;
        this.promoRepo = promoRepo;
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
        p.setPaymentMethod(req.getPaymentMethod());
        p.setStatus(PaymentStatus.PENDING);

        p = paymentRepo.save(p);
        return toResponse(p);
    }

    public PaymentResponse confirm(Long paymentId, ConfirmPaymentRequest req) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        // avoid confirming twice
        if (p.getStatus() == PaymentStatus.SUCCESS) {
            return toResponse(p);
        }

        p.setStatus(PaymentStatus.SUCCESS);
        paymentRepo.save(p);

        // ✅ increment promo usage only after success
        if (p.getPromoCodeId() != null) {
            PromoCode promo = promoRepo.findById(p.getPromoCodeId())
                    .orElseThrow(() -> new NotFoundException("Promo code not found: " + p.getPromoCodeId()));

            if (!Boolean.TRUE.equals(promo.getActive())) {
                throw new BadRequestException("Promo code is disabled");
            }

            if (promo.getUsageLimit() != null && promo.getCurrentUses() >= promo.getUsageLimit()) {
                throw new BadRequestException("Promo usage limit reached");
            }

            promo.setCurrentUses(promo.getCurrentUses() + 1);
            promoRepo.save(promo);
        }

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
        r.setStatus(p.getStatus());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}