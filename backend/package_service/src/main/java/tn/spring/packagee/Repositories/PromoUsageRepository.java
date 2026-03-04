package tn.spring.packagee.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.packagee.Entities.PromoUsage;

public interface PromoUsageRepository extends JpaRepository<PromoUsage, Long> {
    long countByPromoCodeId(Long promoCodeId);
    long countByPromoCodeIdAndStudentId(Long promoCodeId, Long studentId);

    boolean existsByPromoCodeIdAndPaymentId(Long promoCodeId, Long paymentId);
}