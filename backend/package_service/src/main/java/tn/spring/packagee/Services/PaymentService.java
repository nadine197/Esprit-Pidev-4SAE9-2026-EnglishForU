package tn.spring.packagee.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.CreatePaymentRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.DTOs.UserPublicDTO;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Entities.PromoCode;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TransactionStatus;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PaymentRepository;
import tn.spring.packagee.Repositories.PromoCodeRepository;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService  {

    private final PaymentRepository paymentRepo;

    private final UserClient userClient;

    public PaymentService(PaymentRepository paymentRepo, UserClient userClient) {
        this.paymentRepo = paymentRepo;
        this.userClient = userClient;
    }

    public PaymentResponse create(CreatePaymentRequest req) {
        Payment p = new Payment();

        p.setTargetType(req.getTargetType());
        p.setTargetId(req.getTargetId());

        // ✅ fetch and store student full name ONCE at creation
        UserPublicDTO user = userClient.fetchStudentByEmail(req.getStudentEmail());
        p.setStudentFullName(user.getName()  + " " + user.getLastName());
        p.setStudentId(user.getId());
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
        p.setPaymentMethod(req.getProvider());
        p.setProviderRef(req.getProviderRef());
        p.setStatus(PaymentStatus.SUCCESS);
        paymentRepo.save(p);

        return toResponse(p);
    }
    public PaymentResponse fail(Long paymentId) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        p.setStatus(PaymentStatus.FAILED);


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
    public List<PaymentResponse> list() {
        // simplest version: fetch all then filter in memory
        // (later: create repository query)
        return paymentRepo.findAll().stream()
                   .map(this::toResponse)
                .toList();
    }

    public PaymentResponse updateStatus(Long id, String status) {
        Payment p = paymentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + id));

        PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());
        p.setStatus(newStatus);
        paymentRepo.save(p);

        return toResponse(p);
    }

    public void deleteIfPending(Long id) {
        Payment p = paymentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + id));

        if (p.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Only PENDING payments can be deleted.");
        }

        paymentRepo.delete(p);
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
        r.setProviderRef(p.getProviderRef());
        r.setStudentFullName(p.getStudentFullName());
        r.setStatus(p.getStatus());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}