package tn.spring.packagee.Services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.ApplyPromoRequest;
import tn.spring.packagee.DTOs.ApplyPromoResponse;
import tn.spring.packagee.DTOs.CreatePromoCodeRequest;
import tn.spring.packagee.Entities.PromoCode;
import tn.spring.packagee.Entities.PromoUsage;
import tn.spring.packagee.Enum.DiscountType;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.ConflictException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PromoCodeRepository;
import tn.spring.packagee.Repositories.PromoUsageRepository;


import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class PromoService  {

    private final PromoCodeRepository promoRepo;
    private final PromoUsageRepository usageRepo;

    public PromoService(PromoCodeRepository promoRepo, PromoUsageRepository usageRepo) {
        this.promoRepo = promoRepo;
        this.usageRepo = usageRepo;
    }

    public void createPromo(CreatePromoCodeRequest req) {
        String normalized = req.getCode().trim().toUpperCase();
        if (promoRepo.existsByCode(normalized)) {
            throw new ConflictException("Promo code already exists: " + normalized);
        }

        PromoCode p = new PromoCode();
        p.setCode(normalized);
        p.setDiscountType(req.getDiscountType());
        p.setDiscountValue(req.getDiscountValue());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setUsageLimit(req.getUsageLimit());
        p.setUsagePerUserLimit(req.getUsagePerUserLimit());
        p.setMinAmount(req.getMinAmount());
        p.setActive(req.getActive() != null ? req.getActive() : true);

        promoRepo.save(p);
    }

    @Transactional(readOnly = true)
    public ApplyPromoResponse validateAndCompute(ApplyPromoRequest req) {
        String normalized = req.getCode().trim().toUpperCase();

        PromoCode promo = promoRepo.findByCode(normalized)
                .orElseThrow(() -> new NotFoundException("Promo code not found: " + normalized));

        if (!Boolean.TRUE.equals(promo.getActive())) {
            throw new BadRequestException("Promo code is not active");
        }

        LocalDate today = LocalDate.now();
        if (promo.getStartDate() != null && today.isBefore(promo.getStartDate())) {
            throw new BadRequestException("Promo code not started yet");
        }
        if (promo.getEndDate() != null && today.isAfter(promo.getEndDate())) {
            throw new BadRequestException("Promo code expired");
        }

        if (promo.getMinAmount() != null && req.getAmountOriginal().compareTo(promo.getMinAmount()) < 0) {
            throw new BadRequestException("Amount is below promo minimum amount");
        }

        // usage limits
        if (promo.getUsageLimit() != null) {
            long used = usageRepo.countByPromoCodeId(promo.getId());
            if (used >= promo.getUsageLimit()) throw new BadRequestException("Promo usage limit reached");
        }

        if (promo.getUsagePerUserLimit() != null) {
            long usedByUser = usageRepo.countByPromoCodeIdAndStudentId(promo.getId(), req.getStudentId());
            if (usedByUser >= promo.getUsagePerUserLimit()) throw new BadRequestException("Promo usage limit per user reached");
        }

        BigDecimal discount = computeDiscount(promo, req.getAmountOriginal());
        BigDecimal finalAmount = req.getAmountOriginal().subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        return new ApplyPromoResponse(true, promo.getCode(), discount, finalAmount);
    }

    private BigDecimal computeDiscount(PromoCode promo, BigDecimal original) {
        if (promo.getDiscountType() == DiscountType.FIXED) {
            return promo.getDiscountValue().min(original);
        }
        // percentage
        BigDecimal pct = promo.getDiscountValue(); // e.g. 10 for 10%
        if (pct.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal d = original.multiply(pct).divide(new BigDecimal("100"));
        return d.min(original);
    }

    public void recordUsageIfPaid(Long promoCodeId, Long paymentId) {
        // optional to prevent duplicates
        if (paymentId == null) return;
        boolean exists = usageRepo.existsByPromoCodeIdAndPaymentId(promoCodeId, paymentId);
        if (exists) return;

        PromoUsage u = new PromoUsage();
        u.setPromoCodeId(promoCodeId);
        // you can extend this method to take studentId/appliedTo... if you want.
        u.setPaymentId(paymentId);
        // not enough info here; keep it as helper or remove from your project.
        usageRepo.save(u);
    }
}