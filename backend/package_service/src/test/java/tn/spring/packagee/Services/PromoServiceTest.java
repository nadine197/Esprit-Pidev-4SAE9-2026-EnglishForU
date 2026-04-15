package tn.spring.packagee.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.packagee.DTOs.ApplyPromoRequest;
import tn.spring.packagee.DTOs.ApplyPromoResponse;
import tn.spring.packagee.DTOs.CreatePromoCodeRequest;
import tn.spring.packagee.Entities.PromoCode;
import tn.spring.packagee.Enum.DiscountType;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.ConflictException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PromoCodeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PromoService}.
 *
 * Each test method follows the Arrange-Act-Assert (AAA) pattern.
 * The repository layer is mocked with Mockito so no database is required.
 */
@ExtendWith(MockitoExtension.class)
class PromoServiceTest {

    @Mock
    private PromoCodeRepository promoRepo;

    @InjectMocks
    private PromoService promoService;

    // ---------------------------------------------------------------
    // Helper: build a valid PromoCode entity
    // ---------------------------------------------------------------
    private PromoCode buildPromoCode(String code, DiscountType type, double value) {
        PromoCode p = new PromoCode();
        p.setId(1L);
        p.setCode(code.toUpperCase());
        p.setDiscountType(type);
        p.setDiscountValue(BigDecimal.valueOf(value));
        p.setUsageLimit(10);
        p.setCurrentUses(0);
        p.setActive(true);
        return p;
    }

    // Helper: build a valid CreatePromoCodeRequest
    private CreatePromoCodeRequest buildCreateRequest(String code, DiscountType type, double value) {
        CreatePromoCodeRequest req = new CreatePromoCodeRequest();
        req.setCode(code);
        req.setDiscountType(type);
        req.setDiscountValue(BigDecimal.valueOf(value));
        req.setUsageLimit(10);
        req.setActive(true);
        return req;
    }

    // ===========================
    // createPromo
    // ===========================

    @Test
    void createPromo_shouldSavePromo_whenCodeIsNew() {
        // Arrange
        CreatePromoCodeRequest req = buildCreateRequest("SAVE10", DiscountType.PERCENTAGE, 10);
        when(promoRepo.existsByCode("SAVE10")).thenReturn(false);

        // Act
        promoService.createPromo(req);

        // Assert: repository save must be called exactly once
        verify(promoRepo, times(1)).save(any(PromoCode.class));
    }

    @Test
    void createPromo_shouldNormalizeCodeToUpperCase() {
        // Arrange
        CreatePromoCodeRequest req = buildCreateRequest("save10", DiscountType.PERCENTAGE, 10);
        when(promoRepo.existsByCode("SAVE10")).thenReturn(false);

        // Act
        promoService.createPromo(req);

        // Assert: the saved entity must carry the uppercase code
        verify(promoRepo).save(argThat(p -> "SAVE10".equals(p.getCode())));
    }

    @Test
    void createPromo_shouldThrowConflictException_whenCodeAlreadyExists() {
        // Arrange
        CreatePromoCodeRequest req = buildCreateRequest("SAVE10", DiscountType.PERCENTAGE, 10);
        when(promoRepo.existsByCode("SAVE10")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> promoService.createPromo(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SAVE10");
    }

    @Test
    void createPromo_shouldThrowBadRequestException_whenDiscountValueIsZero() {
        // Arrange: value = 0 is invalid
        CreatePromoCodeRequest req = buildCreateRequest("ZERO", DiscountType.FIXED, 0);
        when(promoRepo.existsByCode("ZERO")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> promoService.createPromo(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("discountValue must be > 0");
    }

    @Test
    void createPromo_shouldThrowBadRequestException_whenPercentageExceeds100() {
        // Arrange
        CreatePromoCodeRequest req = buildCreateRequest("BIGSALE", DiscountType.PERCENTAGE, 110);
        when(promoRepo.existsByCode("BIGSALE")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> promoService.createPromo(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Percentage discount cannot exceed 100");
    }

    @Test
    void createPromo_shouldThrowBadRequestException_whenEndDateIsBeforeStartDate() {
        // Arrange
        CreatePromoCodeRequest req = buildCreateRequest("DATES", DiscountType.FIXED, 5);
        req.setStartDate(LocalDate.now().plusDays(5));
        req.setEndDate(LocalDate.now());           // end before start → invalid
        when(promoRepo.existsByCode("DATES")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> promoService.createPromo(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("endDate cannot be before startDate");
    }

    @Test
    void createPromo_shouldThrowBadRequestException_whenNullCode() {
        // Arrange
        CreatePromoCodeRequest req = buildCreateRequest("dummy", DiscountType.FIXED, 5);
        req.setCode(null);

        // Act & Assert
        assertThatThrownBy(() -> promoService.createPromo(req))
                .isInstanceOf(BadRequestException.class);
    }

    // ===========================
    // getAll
    // ===========================

    @Test
    void getAll_shouldReturnAllPromoCodes() {
        // Arrange
        List<PromoCode> mockList = List.of(
                buildPromoCode("PROMO1", DiscountType.FIXED, 5),
                buildPromoCode("PROMO2", DiscountType.PERCENTAGE, 20)
        );
        when(promoRepo.findAll()).thenReturn(mockList);

        // Act
        List<PromoCode> result = promoService.getAll();

        // Assert
        assertThat(result).hasSize(2);
        verify(promoRepo, times(1)).findAll();
    }

    // ===========================
    // update
    // ===========================

    @Test
    void update_shouldUpdatePromoFields_whenPromoExists() {
        // Arrange
        PromoCode existing = buildPromoCode("OLD", DiscountType.FIXED, 5);
        PromoCode changes = new PromoCode();
        changes.setDiscountValue(BigDecimal.valueOf(15));
        changes.setDiscountType(DiscountType.PERCENTAGE);

        when(promoRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(promoRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PromoCode result = promoService.update(1L, changes);

        // Assert
        assertThat(result.getDiscountValue()).isEqualByComparingTo("15");
        assertThat(result.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
    }

    @Test
    void update_shouldThrowNotFoundException_whenPromoDoesNotExist() {
        // Arrange
        when(promoRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> promoService.update(99L, new PromoCode()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldThrowConflictException_whenNewCodeAlreadyTakenByAnotherPromo() {
        // Arrange
        PromoCode existing = buildPromoCode("ORIGINAL", DiscountType.FIXED, 5);
        PromoCode changes = new PromoCode();
        changes.setCode("TAKEN");

        when(promoRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(promoRepo.existsByCode("TAKEN")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> promoService.update(1L, changes))
                .isInstanceOf(ConflictException.class);
    }

    // ===========================
    // delete
    // ===========================

    @Test
    void delete_shouldDeletePromo_whenPromoHasNeverBeenUsed() {
        // Arrange
        PromoCode promo = buildPromoCode("UNUSED", DiscountType.FIXED, 5);
        promo.setCurrentUses(0);
        when(promoRepo.findById(1L)).thenReturn(Optional.of(promo));

        // Act
        promoService.delete(1L);

        // Assert
        verify(promoRepo, times(1)).delete(promo);
    }

    @Test
    void delete_shouldThrowBadRequestException_whenPromoHasBeenUsed() {
        // Arrange
        PromoCode promo = buildPromoCode("USED", DiscountType.FIXED, 5);
        promo.setCurrentUses(3);
        when(promoRepo.findById(1L)).thenReturn(Optional.of(promo));

        // Act & Assert
        assertThatThrownBy(() -> promoService.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot delete promo code that has been used");
    }

    @Test
    void delete_shouldThrowNotFoundException_whenPromoDoesNotExist() {
        // Arrange
        when(promoRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> promoService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // enable / disable
    // ===========================

    @Test
    void enable_shouldSetActiveTrue_whenPromoIsCurrentlyDisabled() {
        // Arrange
        PromoCode promo = buildPromoCode("PROMO", DiscountType.FIXED, 5);
        promo.setActive(false);
        when(promoRepo.findById(1L)).thenReturn(Optional.of(promo));

        // Act
        promoService.enable(1L);

        // Assert
        assertThat(promo.getActive()).isTrue();
    }

    @Test
    void enable_shouldThrowBadRequestException_whenPromoIsAlreadyActive() {
        // Arrange
        PromoCode promo = buildPromoCode("PROMO", DiscountType.FIXED, 5);
        promo.setActive(true);
        when(promoRepo.findById(1L)).thenReturn(Optional.of(promo));

        // Act & Assert
        assertThatThrownBy(() -> promoService.enable(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already enabled");
    }

    @Test
    void disable_shouldSetActiveFalse_whenPromoIsCurrentlyActive() {
        // Arrange
        PromoCode promo = buildPromoCode("PROMO", DiscountType.FIXED, 5);
        promo.setActive(true);
        when(promoRepo.findById(1L)).thenReturn(Optional.of(promo));

        // Act
        promoService.disable(1L);

        // Assert
        assertThat(promo.getActive()).isFalse();
    }

    @Test
    void disable_shouldThrowBadRequestException_whenPromoIsAlreadyDisabled() {
        // Arrange
        PromoCode promo = buildPromoCode("PROMO", DiscountType.FIXED, 5);
        promo.setActive(false);
        when(promoRepo.findById(1L)).thenReturn(Optional.of(promo));

        // Act & Assert
        assertThatThrownBy(() -> promoService.disable(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already disabled");
    }

    // ===========================
    // validateAndCompute
    // ===========================

    @Test
    void validateAndCompute_shouldReturnCorrectDiscount_forFixedDiscount() {
        // Arrange
        PromoCode promo = buildPromoCode("FIXED5", DiscountType.FIXED, 5);
        when(promoRepo.findByCode("FIXED5")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("FIXED5");
        req.setAmountOriginal(BigDecimal.valueOf(100));

        // Act
        ApplyPromoResponse response = promoService.validateAndCompute(req);

        // Assert
        assertThat(response.valid()).isTrue();
        assertThat(response.discountAmount()).isEqualByComparingTo("5");
        assertThat(response.amountFinal()).isEqualByComparingTo("95");
    }

    @Test
    void validateAndCompute_shouldReturnCorrectDiscount_forPercentageDiscount() {
        // Arrange
        PromoCode promo = buildPromoCode("PCT20", DiscountType.PERCENTAGE, 20);
        when(promoRepo.findByCode("PCT20")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("PCT20");
        req.setAmountOriginal(BigDecimal.valueOf(200));

        // Act
        ApplyPromoResponse response = promoService.validateAndCompute(req);

        // Assert: 20% of 200 = 40 discount, final = 160
        assertThat(response.discountAmount()).isEqualByComparingTo("40");
        assertThat(response.amountFinal()).isEqualByComparingTo("160");
    }

    @Test
    void validateAndCompute_shouldThrowBadRequestException_whenPromoIsInactive() {
        // Arrange
        PromoCode promo = buildPromoCode("INACTIVE", DiscountType.FIXED, 5);
        promo.setActive(false);
        when(promoRepo.findByCode("INACTIVE")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("INACTIVE");
        req.setAmountOriginal(BigDecimal.valueOf(100));

        // Act & Assert
        assertThatThrownBy(() -> promoService.validateAndCompute(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void validateAndCompute_shouldThrowBadRequestException_whenPromoIsExpired() {
        // Arrange
        PromoCode promo = buildPromoCode("EXPIRED", DiscountType.FIXED, 5);
        promo.setEndDate(LocalDate.now().minusDays(1));  // expired yesterday
        when(promoRepo.findByCode("EXPIRED")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("EXPIRED");
        req.setAmountOriginal(BigDecimal.valueOf(100));

        // Act & Assert
        assertThatThrownBy(() -> promoService.validateAndCompute(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateAndCompute_shouldThrowBadRequestException_whenPromoHasNotStartedYet() {
        // Arrange
        PromoCode promo = buildPromoCode("FUTURE", DiscountType.FIXED, 5);
        promo.setStartDate(LocalDate.now().plusDays(5)); // starts in the future
        when(promoRepo.findByCode("FUTURE")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("FUTURE");
        req.setAmountOriginal(BigDecimal.valueOf(100));

        // Act & Assert
        assertThatThrownBy(() -> promoService.validateAndCompute(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not started yet");
    }

    @Test
    void validateAndCompute_shouldThrowBadRequestException_whenUsageLimitReached() {
        // Arrange
        PromoCode promo = buildPromoCode("MAXED", DiscountType.FIXED, 5);
        promo.setUsageLimit(5);
        promo.setCurrentUses(5);   // already at limit
        when(promoRepo.findByCode("MAXED")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("MAXED");
        req.setAmountOriginal(BigDecimal.valueOf(100));

        // Act & Assert
        assertThatThrownBy(() -> promoService.validateAndCompute(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("usage limit reached");
    }

    @Test
    void validateAndCompute_shouldIncrementCurrentUses_afterSuccessfulValidation() {
        // Arrange
        PromoCode promo = buildPromoCode("INC", DiscountType.FIXED, 5);
        promo.setCurrentUses(2);
        when(promoRepo.findByCode("INC")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("INC");
        req.setAmountOriginal(BigDecimal.valueOf(100));

        // Act
        promoService.validateAndCompute(req);

        // Assert: currentUses must now be 3
        assertThat(promo.getCurrentUses()).isEqualTo(3);
        verify(promoRepo).save(promo);
    }

    @Test
    void validateAndCompute_shouldNotReturnNegativeFinalAmount_whenDiscountExceedsOriginal() {
        // Arrange: fixed discount of 200 on an original amount of 50
        PromoCode promo = buildPromoCode("BIG", DiscountType.FIXED, 200);
        when(promoRepo.findByCode("BIG")).thenReturn(Optional.of(promo));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("BIG");
        req.setAmountOriginal(BigDecimal.valueOf(50));

        // Act
        ApplyPromoResponse response = promoService.validateAndCompute(req);

        // Assert: final amount must be clamped at 0, never negative
        assertThat(response.amountFinal()).isEqualByComparingTo("0");
    }

    @Test
    void validateAndCompute_shouldThrowNotFoundException_whenCodeDoesNotExist() {
        // Arrange
        when(promoRepo.findByCode("GHOST")).thenReturn(Optional.empty());

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCode("GHOST");
        req.setAmountOriginal(BigDecimal.valueOf(100));

        // Act & Assert
        assertThatThrownBy(() -> promoService.validateAndCompute(req))
                .isInstanceOf(NotFoundException.class);
    }
}
