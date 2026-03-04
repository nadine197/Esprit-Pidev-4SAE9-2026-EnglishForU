package tn.spring.packagee.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.*;
import tn.spring.packagee.Entities.PromoCode;
import tn.spring.packagee.Enum.DiscountType;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.ConflictException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PromoCodeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromoService {

    private final PromoCodeRepository promoRepo;

    public PromoService(PromoCodeRepository promoRepo) {
        this.promoRepo = promoRepo;
    }

    // -------------------------
    // CREATE (ADMIN)
    // -------------------------
    public void createPromo(CreatePromoCodeRequest req) {
        String normalized = normalizeCode(req.getCode());
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
        p.setCurrentUses(0);
        p.setCreatedAt(new Date());
        p.setForUser(req.getForUser());
        p.setActive(req.getActive() != null ? req.getActive() : true);

        validatePromoFields(p);
        promoRepo.save(p);
    }

    // -------------------------
    // READ (ADMIN)
    // -------------------------
    @Transactional(readOnly = true)
    public List<PromoCode> getAll() {
        return promoRepo.findAll();
    }



    // -------------------------
    // UPDATE (ADMIN)
    // -------------------------
    public PromoCode update(Long id, PromoCode req) {
        PromoCode p = promoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Promo code not found: " + id));

        if (req.getCode() != null) {
            String normalized = normalizeCode(req.getCode());
            if (!normalized.equals(p.getCode()) && promoRepo.existsByCode(normalized)) {
                throw new ConflictException("Promo code already exists: " + normalized);
            }
            p.setCode(normalized);
        }

        if (req.getDiscountType() != null) p.setDiscountType(req.getDiscountType());
        if (req.getDiscountValue() != null) p.setDiscountValue(req.getDiscountValue());

        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) p.setEndDate(req.getEndDate());

        if (req.getUsageLimit() != null) p.setUsageLimit(req.getUsageLimit());
        if (req.getActive() != null) p.setActive(req.getActive());
        if (req.getForUser() != null) p.setForUser(req.getForUser());

        validatePromoFields(p);
        return promoRepo.save(p);
    }

    // -------------------------
    // DELETE (ADMIN)
    // -------------------------
    public void delete(Long id) {
        PromoCode p = promoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Promo code not found: " + id));

        // Optional rule: don't allow delete if already used
        if ( p.getCurrentUses() > 0) {
            throw new BadRequestException("Cannot delete promo code that has been used");
        }

        promoRepo.delete(p);
    }
    // -------------------------
// ENABLE PROMO
// -------------------------
    public void enable(Long id) {
        PromoCode promo = promoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Promo code not found: " + id));

        if (Boolean.TRUE.equals(promo.getActive())) {
            throw new BadRequestException("Promo code already enabled");
        }

        promo.setActive(true);
    }

    // -------------------------
// DISABLE PROMO
// -------------------------
    public void disable(Long id) {
        PromoCode promo = promoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Promo code not found: " + id));

        if (Boolean.FALSE.equals(promo.getActive())) {
            throw new BadRequestException("Promo code already disabled");
        }

        promo.setActive(false);
    }
    // -------------------------
    // VALIDATE (STUDENT/ADMIN)
    // -------------------------
    @Transactional(readOnly = true)
    public ApplyPromoResponse validateAndCompute(ApplyPromoRequest req) {
        String normalized = normalizeCode(req.getCode());

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


            if (promo.getCurrentUses() >= promo.getUsageLimit()) {
                throw new BadRequestException("Promo usage limit reached");
            }


        BigDecimal discount = computeDiscount(promo, req.getAmountOriginal());
        BigDecimal finalAmount = req.getAmountOriginal().subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;

        return new ApplyPromoResponse(true, promo.getCode(), discount, finalAmount);
    }

    // -------------------------
    // helpers
    // -------------------------
    private String normalizeCode(String code) {
        if (code == null) throw new BadRequestException("Promo code is required");
        String normalized = code.trim().toUpperCase();
        if (normalized.isBlank()) throw new BadRequestException("Promo code is required");
        return normalized;
    }

    private void validatePromoFields(PromoCode p) {
        if (p.getDiscountType() == null) throw new BadRequestException("discountType is required");
        if (p.getDiscountValue() == null) throw new BadRequestException("discountValue is required");
        if (p.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("discountValue must be > 0");
        }

        if (p.getDiscountType() == DiscountType.PERCENTAGE && p.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100");
        }

        if (p.getUsageLimit() != null && p.getUsageLimit() <= 0) {
            throw new BadRequestException("usageLimit must be >= 1");
        }

        if (p.getStartDate() != null && p.getEndDate() != null && p.getEndDate().isBefore(p.getStartDate())) {
            throw new BadRequestException("endDate cannot be before startDate");
        }
    }

    private BigDecimal computeDiscount(PromoCode promo, BigDecimal original) {
        if (original == null || original.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        if (promo.getDiscountType() == DiscountType.FIXED) {
            return promo.getDiscountValue().min(original);
        }

        BigDecimal pct = promo.getDiscountValue();
        BigDecimal d = original.multiply(pct).divide(new BigDecimal("100"));
        return d.min(original);
    }

 }