package tn.spring.packagee.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.packagee.Entities.PromoCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCode(String code);
    boolean existsByCode(String code);

    // promo active and not expired
    List<PromoCode> findByActiveTrueAndEndDateAfter(LocalDate date);
}