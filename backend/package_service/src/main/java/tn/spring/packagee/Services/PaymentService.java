package tn.spring.packagee.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.CreatePaymentRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.DTOs.UserPublicDTO;
import tn.spring.packagee.Entities.PackageOffer;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Entities.PromoCode;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TransactionStatus;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PackageOfferRepository;
import tn.spring.packagee.Repositories.PaymentRepository;
import tn.spring.packagee.Repositories.PromoCodeRepository;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService  {

    private final PaymentRepository paymentRepo;
    private final PackageOfferRepository packageOfferRepo;
    private final UserClient userClient;
    private final PaymentFlouciService flouciService;
    private final PromoCodeRepository promoCodeRepo;

    public PaymentService(PaymentRepository paymentRepo, PackageOfferRepository packageOfferRepo, UserClient userClient, PaymentFlouciService flouciService, PromoCodeRepository promoCodeRepo) {
        this.paymentRepo = paymentRepo;
        this.packageOfferRepo = packageOfferRepo;
        this.userClient = userClient;
        this.flouciService = flouciService;
        this.promoCodeRepo = promoCodeRepo;
    }

    public PaymentResponse create(CreatePaymentRequest req) {

        PackageOffer pkg  = packageOfferRepo.findById(req.getTargetId())
                .orElseThrow(() -> new NotFoundException("Package offer not found"));

        Payment p = new Payment();

        p.setTargetId(req.getTargetId());
        p.setTargetName(pkg.getName() + " type : "+ pkg.getType());
        // ✅ fetch and store student full name ONCE at creation
        UserPublicDTO user = userClient.fetchStudentByEmail(req.getStudentEmail());
        p.setStudentFullName(user.getName()  + " " + user.getLastName());
        p.setStudentId(user.getId());
        p.setAmountOriginal(req.getAmountOriginal());
        p.setDiscountAmount(req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO);
        PromoCode prm = promoCodeRepo.findByCode(req.getIdPromoCode()).orElse(null);

        if(p.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0 && prm!=null) {
             prm.setCurrentUses(prm.getCurrentUses() + 1);
             promoCodeRepo.save(prm);
        }
        BigDecimal finalAmount = p.getAmountOriginal().subtract(p.getDiscountAmount());
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        p.setAmountFinal(finalAmount);
        p.setPaymentMethod(req.getPaymentMethod());
        p.setStatus(PaymentStatus.PENDING);

        p = paymentRepo.save(p);
        if (req.getPaymentMethod().name().equals("FLOUCI")) {
            try {
                    BigInteger amountMillimes = BigInteger.valueOf(finalAmount.multiply(BigDecimal.valueOf(1000)).intValue()); // TND -> millimes
                String trackingId = "PAY_" + p.getId(); // put your internal payment id here

                var flouciRes = flouciService.generatePayment(amountMillimes);
                p.setCheckoutUrl(flouciRes.getLink());

                p.setProviderRef(flouciRes.getPayment_id());
                p = paymentRepo.save(p);

            } catch (Exception e) {
                p.setStatus(PaymentStatus.FAILED);
                paymentRepo.save(p);
                throw new RuntimeException("Flouci init failed: " + e.getMessage());
            }
        }
        return toResponse(p);
    }

    public PaymentResponse confirm(Long paymentId, ConfirmPaymentRequest req) {
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        if (p.getStatus() == PaymentStatus.SUCCESS) {
            return toResponse(p);
        }
        System.out.println("✅ Payment update 2: " + paymentId);

        // ✅ provider is method here
        // better: p.setProvider(req.getProvider()); but you use paymentMethod; keep your design:
        p.setPaymentMethod(req.getProvider()); // if provider is enum string
        p.setProviderRef(req.getProviderRef());
        p.setStatus(PaymentStatus.SUCCESS);
        p.setConfirmedAt(Instant.now()); ;
        // ✅ voucher meta (only once)
        p.setConfirmedAt(java.time.Instant.now());
        if (p.getVoucherNumber() == null) {
            p.setVoucherNumber("VCH-" + java.time.Year.now().getValue() + "-" + String.format("%07d", p.getId()));
        }
        System.out.println("✅ Payment biche save: " + paymentId);

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
    public List<PaymentResponse> listByStudent(UUID studentId) {
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
        r.setTargetName(p.getTargetName());
        r.setTargetId(p.getTargetId());
        r.setAmountOriginal(p.getAmountOriginal());
        r.setDiscountAmount(p.getDiscountAmount());
        r.setAmountFinal(p.getAmountFinal());
        r.setProviderRef(p.getProviderRef());
        r.setCheckoutUrl(p.getCheckoutUrl());
        r.setVoucherNumber(p.getVoucherNumber());
        r.setConfirmedAt(p.getConfirmedAt());
        r.setStudentFullName(p.getStudentFullName());
        r.setStatus(p.getStatus());
        r.setPaymentMethod(p.getPaymentMethod());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}