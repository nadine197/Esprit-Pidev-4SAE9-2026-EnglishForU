package tn.spring.packagee.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.packagee.DTOs.ConfirmPaymentRequest;
import tn.spring.packagee.DTOs.CreatePaymentRequest;
import tn.spring.packagee.DTOs.PaymentResponse;
import tn.spring.packagee.DTOs.UserPublicDTO;
import tn.spring.packagee.Entities.PackageOffer;
import tn.spring.packagee.Entities.Payment;
import tn.spring.packagee.Enum.PackageType;
import tn.spring.packagee.Enum.PaymentMethod;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Exceptions.BadRequestException;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PackageOfferRepository;
import tn.spring.packagee.Repositories.PaymentRepository;
import tn.spring.packagee.Repositories.PromoCodeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentService}.
 *
 * External collaborators (repositories, UserClient, FlouciService) are mocked.
 * PaymentFlouciService is mocked to avoid real HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository         paymentRepo;
    @Mock private PackageOfferRepository    packageOfferRepo;
    @Mock private UserClient                userClient;
    @Mock private PaymentFlouciService      flouciService;
    @Mock private PromoCodeRepository       promoCodeRepo;

    @InjectMocks
    private PaymentService paymentService;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private PackageOffer buildOffer(Long id, String name) {
        PackageOffer o = new PackageOffer();
        o.setId(id);
        o.setName(name);
        o.setType(PackageType.COURSE_ONLY);
        return o;
    }

    private UserPublicDTO buildUser(String name, String lastName) {
        UserPublicDTO u = new UserPublicDTO();
        u.setId(UUID.randomUUID());
        u.setName(name);
        u.setLastName(lastName);
        return u;
    }

    private Payment buildPayment(Long id, PaymentStatus status) {
        Payment p = new Payment();
        p.setId(id);
        p.setStatus(status);
        p.setAmountOriginal(BigDecimal.valueOf(100));
        p.setDiscountAmount(BigDecimal.ZERO);
        p.setAmountFinal(BigDecimal.valueOf(100));
        p.setPaymentMethod(PaymentMethod.CASH);
        p.setStudentId(UUID.randomUUID());
        p.setTargetId(1L);
        p.setTargetName("Basic Plan type : MONTHLY");
        p.setStudentFullName("John Doe");
        return p;
    }

    private CreatePaymentRequest buildCreateRequest(PaymentMethod method) {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setTargetId(1L);
        req.setStudentEmail("john@example.com");
        req.setAmountOriginal(BigDecimal.valueOf(100));
        req.setDiscountAmount(BigDecimal.ZERO);
        req.setPaymentMethod(method);
        req.setIdPromoCode(null);
        return req;
    }

    // ===========================
    // create
    // ===========================

    @Test
    void create_shouldSavePaymentWithStatusPending_forCashPayment() {
        // Arrange
        when(packageOfferRepo.findById(1L)).thenReturn(Optional.of(buildOffer(1L, "Basic Plan")));
        when(userClient.fetchStudentByEmail("john@example.com")).thenReturn(buildUser("John", "Doe"));
        when(promoCodeRepo.findByCode(null)).thenReturn(Optional.empty());
        when(paymentRepo.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        CreatePaymentRequest req = buildCreateRequest(PaymentMethod.CASH);

        // Act
        PaymentResponse response = paymentService.create(req);

        // Assert
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.getStudentFullName()).isEqualTo("John Doe");
        verify(paymentRepo, atLeastOnce()).save(any());
    }

    @Test
    void create_shouldComputeCorrectFinalAmount_whenDiscountIsApplied() {
        // Arrange
        when(packageOfferRepo.findById(1L)).thenReturn(Optional.of(buildOffer(1L, "Basic Plan")));
        when(userClient.fetchStudentByEmail("john@example.com")).thenReturn(buildUser("John", "Doe"));
        when(promoCodeRepo.findByCode(null)).thenReturn(Optional.empty());
        when(paymentRepo.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        CreatePaymentRequest req = buildCreateRequest(PaymentMethod.CASH);
        req.setAmountOriginal(BigDecimal.valueOf(200));
        req.setDiscountAmount(BigDecimal.valueOf(30));

        // Act
        PaymentResponse response = paymentService.create(req);

        // Assert: 200 - 30 = 170
        assertThat(response.getAmountFinal()).isEqualByComparingTo("170");
    }

    @Test
    void create_shouldClampFinalAmountToZero_whenDiscountExceedsOriginal() {
        // Arrange
        when(packageOfferRepo.findById(1L)).thenReturn(Optional.of(buildOffer(1L, "Basic Plan")));
        when(userClient.fetchStudentByEmail("john@example.com")).thenReturn(buildUser("John", "Doe"));
        when(promoCodeRepo.findByCode(null)).thenReturn(Optional.empty());
        when(paymentRepo.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        CreatePaymentRequest req = buildCreateRequest(PaymentMethod.CASH);
        req.setAmountOriginal(BigDecimal.valueOf(50));
        req.setDiscountAmount(BigDecimal.valueOf(200)); // discount > original

        // Act
        PaymentResponse response = paymentService.create(req);

        // Assert: final must not go below 0
        assertThat(response.getAmountFinal()).isEqualByComparingTo("0");
    }

    @Test
    void create_shouldThrowNotFoundException_whenPackageOfferDoesNotExist() {
        // Arrange
        when(packageOfferRepo.findById(99L)).thenReturn(Optional.empty());

        CreatePaymentRequest req = buildCreateRequest(PaymentMethod.CASH);
        req.setTargetId(99L);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Package offer not found");
    }

    // ===========================
    // confirm
    // ===========================

    @Test
    void confirm_shouldSetStatusToSuccess_andRecordVoucherNumber() {
        // Arrange
        Payment pending = buildPayment(5L, PaymentStatus.PENDING);
        when(paymentRepo.findById(5L)).thenReturn(Optional.of(pending));
        when(paymentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConfirmPaymentRequest req = new ConfirmPaymentRequest();
        req.setProvider(PaymentMethod.STRIPE);
        req.setProviderRef("stripe_ref_001");

        // Act
        PaymentResponse response = paymentService.confirm(5L, req);

        // Assert
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(pending.getVoucherNumber()).isNotNull().startsWith("VCH-");
        assertThat(pending.getConfirmedAt()).isNotNull();
    }

    @Test
    void confirm_shouldReturnImmediately_whenPaymentIsAlreadySuccessful() {
        // Arrange: payment already in SUCCESS state
        Payment success = buildPayment(5L, PaymentStatus.SUCCESS);
        success.setVoucherNumber("VCH-2025-0000005");
        when(paymentRepo.findById(5L)).thenReturn(Optional.of(success));

        ConfirmPaymentRequest req = new ConfirmPaymentRequest();
        req.setProvider(PaymentMethod.STRIPE);
        req.setProviderRef("stripe_ref_001");

        // Act
        PaymentResponse response = paymentService.confirm(5L, req);

        // Assert: no additional save calls, status remains SUCCESS
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentRepo, never()).save(any());
    }

    @Test
    void confirm_shouldThrowNotFoundException_whenPaymentDoesNotExist() {
        // Arrange
        when(paymentRepo.findById(99L)).thenReturn(Optional.empty());

        ConfirmPaymentRequest req = new ConfirmPaymentRequest();
        req.setProvider(PaymentMethod.CASH);
        req.setProviderRef("ref");

        // Act & Assert
        assertThatThrownBy(() -> paymentService.confirm(99L, req))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // fail
    // ===========================

    @Test
    void fail_shouldSetStatusToFailed() {
        // Arrange
        Payment pending = buildPayment(3L, PaymentStatus.PENDING);
        when(paymentRepo.findById(3L)).thenReturn(Optional.of(pending));

        // Act
        PaymentResponse response = paymentService.fail(3L);

        // Assert
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void fail_shouldThrowNotFoundException_whenPaymentDoesNotExist() {
        // Arrange
        when(paymentRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.fail(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // getById
    // ===========================

    @Test
    void getById_shouldReturnPaymentResponse_whenPaymentExists() {
        // Arrange
        Payment p = buildPayment(7L, PaymentStatus.SUCCESS);
        when(paymentRepo.findById(7L)).thenReturn(Optional.of(p));

        // Act
        PaymentResponse response = paymentService.getById(7L);

        // Assert
        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenPaymentDoesNotExist() {
        // Arrange
        when(paymentRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // listByStudent
    // ===========================

    @Test
    void listByStudent_shouldReturnAllPaymentsForGivenStudent() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        List<Payment> payments = List.of(
                buildPayment(1L, PaymentStatus.SUCCESS),
                buildPayment(2L, PaymentStatus.PENDING)
        );
        payments.forEach(p -> p.setStudentId(studentId));
        when(paymentRepo.findByStudentId(studentId)).thenReturn(payments);

        // Act
        List<PaymentResponse> result = paymentService.listByStudent(studentId);

        // Assert
        assertThat(result).hasSize(2);
        verify(paymentRepo, times(1)).findByStudentId(studentId);
    }

    // ===========================
    // list (all)
    // ===========================

    @Test
    void list_shouldReturnAllPayments() {
        // Arrange
        when(paymentRepo.findAll()).thenReturn(List.of(
                buildPayment(1L, PaymentStatus.SUCCESS),
                buildPayment(2L, PaymentStatus.PENDING),
                buildPayment(3L, PaymentStatus.FAILED)
        ));

        // Act
        List<PaymentResponse> result = paymentService.list();

        // Assert
        assertThat(result).hasSize(3);
    }

    // ===========================
    // updateStatus
    // ===========================

    @Test
    void updateStatus_shouldChangePaymentStatus_whenValidStatusString() {
        // Arrange
        Payment p = buildPayment(4L, PaymentStatus.PENDING);
        when(paymentRepo.findById(4L)).thenReturn(Optional.of(p));
        when(paymentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentResponse response = paymentService.updateStatus(4L, "SUCCESS");

        // Assert
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void updateStatus_shouldThrowIllegalArgumentException_whenStatusStringIsInvalid() {
        // Arrange
        Payment p = buildPayment(4L, PaymentStatus.PENDING);
        when(paymentRepo.findById(4L)).thenReturn(Optional.of(p));

        // Act & Assert: "INVALID" is not a valid PaymentStatus enum constant
        assertThatThrownBy(() -> paymentService.updateStatus(4L, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateStatus_shouldThrowNotFoundException_whenPaymentDoesNotExist() {
        // Arrange
        when(paymentRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.updateStatus(99L, "SUCCESS"))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // deleteIfPending
    // ===========================

    @Test
    void deleteIfPending_shouldDeletePayment_whenStatusIsPending() {
        // Arrange
        Payment p = buildPayment(6L, PaymentStatus.PENDING);
        when(paymentRepo.findById(6L)).thenReturn(Optional.of(p));

        // Act
        paymentService.deleteIfPending(6L);

        // Assert
        verify(paymentRepo, times(1)).delete(p);
    }

    @Test
    void deleteIfPending_shouldThrowBadRequestException_whenStatusIsNotPending() {
        // Arrange
        Payment p = buildPayment(6L, PaymentStatus.SUCCESS);
        when(paymentRepo.findById(6L)).thenReturn(Optional.of(p));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.deleteIfPending(6L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only PENDING payments can be deleted");
    }

    @Test
    void deleteIfPending_shouldThrowNotFoundException_whenPaymentDoesNotExist() {
        // Arrange
        when(paymentRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.deleteIfPending(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // toResponse (mapping check)
    // ===========================

    @Test
    void toResponse_shouldMapAllFieldsCorrectly() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        Payment p = buildPayment(1L, PaymentStatus.SUCCESS);
        p.setStudentId(studentId);
        p.setVoucherNumber("VCH-2025-0000001");
        p.setProviderRef("stripe_ref_xyz");
        p.setCheckoutUrl("https://stripe.com/pay/xyz");

        // Act
        PaymentResponse r = paymentService.toResponse(p);

        // Assert: every field is mapped
        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getStudentId()).isEqualTo(studentId);
        assertThat(r.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(r.getVoucherNumber()).isEqualTo("VCH-2025-0000001");
        assertThat(r.getProviderRef()).isEqualTo("stripe_ref_xyz");
        assertThat(r.getCheckoutUrl()).isEqualTo("https://stripe.com/pay/xyz");
    }
}
