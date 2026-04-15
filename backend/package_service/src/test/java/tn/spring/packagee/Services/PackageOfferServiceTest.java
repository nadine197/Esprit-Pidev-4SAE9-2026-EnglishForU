package tn.spring.packagee.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.packagee.DTOs.AddPackageItemRequest;
import tn.spring.packagee.DTOs.CreatePackageOfferRequest;
import tn.spring.packagee.DTOs.PackageOfferResponse;
import tn.spring.packagee.Entities.PackageItem;
import tn.spring.packagee.Entities.PackageOffer;
import tn.spring.packagee.Enum.PackageItemType;
import tn.spring.packagee.Enum.PackageType;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Repositories.PackageItemRepository;
import tn.spring.packagee.Repositories.PackageOfferRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PackageOfferService}.
 *
 * All repository calls are mocked; no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
class PackageOfferServiceTest {

    @Mock
    private PackageOfferRepository offerRepo;

    @Mock
    private PackageItemRepository itemRepo;

    @InjectMocks
    private PackageOfferService packageOfferService;

    // ---------------------------------------------------------------
    // Helper: build a valid PackageOffer entity
    // ---------------------------------------------------------------
    private PackageOffer buildOffer(Long id, String name, boolean active) {
        PackageOffer offer = new PackageOffer();
        offer.setId(id);
        offer.setName(name);
        offer.setDescription("A test package");
        offer.setType(PackageType.COURSE_ONLY);
        offer.setDurationDays(30);
        offer.setPrice(BigDecimal.valueOf(99.99));
        offer.setIsActive(active);
        offer.setFeatures(List.of("Feature A", "Feature B"));
        offer.setItems(new ArrayList<>());
        return offer;
    }

    // Helper: build a valid CreatePackageOfferRequest
    private CreatePackageOfferRequest buildCreateRequest(String name) {
        CreatePackageOfferRequest req = new CreatePackageOfferRequest();
        req.setName(name);
        req.setDescription("A test package");
        req.setType(PackageType.MIXED);
        req.setDurationDays(30);
        req.setPrice(BigDecimal.valueOf(99.99));
        req.setIsActive(true);
        req.setFeatures(List.of("Feature A", "Feature B"));
        return req;
    }

    // ===========================
    // create
    // ===========================

    @Test
    void create_shouldSavePackageOffer_andReturnResponse() {
        // Arrange
        CreatePackageOfferRequest req = buildCreateRequest("Basic Plan");
        PackageOffer saved = buildOffer(1L, "Basic Plan", true);
        when(offerRepo.save(any(PackageOffer.class))).thenReturn(saved);

        // Act
        PackageOfferResponse response = packageOfferService.create(req);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Basic Plan");
        verify(offerRepo, times(1)).save(any(PackageOffer.class));
    }

    @Test
    void create_shouldDefaultToActive_whenIsActiveNotSpecified() {
        // Arrange
        CreatePackageOfferRequest req = buildCreateRequest("Pro Plan");
        req.setIsActive(null);   // null -> should default to true
        PackageOffer saved = buildOffer(2L, "Pro Plan", true);
        when(offerRepo.save(any(PackageOffer.class))).thenReturn(saved);

        // Act
        PackageOfferResponse response = packageOfferService.create(req);

        // Assert: saved entity must have isActive = true
        verify(offerRepo).save(argThat(o -> Boolean.TRUE.equals(o.getIsActive())));
    }

    // ===========================
    // getByID
    // ===========================

    @Test
    void getByID_shouldReturnResponse_whenOfferExists() {
        // Arrange
        PackageOffer offer = buildOffer(1L, "Basic Plan", true);
        when(offerRepo.findById(1L)).thenReturn(Optional.of(offer));

        // Act
        PackageOfferResponse response = packageOfferService.getByID(1L);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Basic Plan");
    }

    @Test
    void getByID_shouldThrowNotFoundException_whenOfferDoesNotExist() {
        // Arrange
        when(offerRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> packageOfferService.getByID(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // listAll
    // ===========================

    @Test
    void listAll_shouldReturnAllOffers_includingInactiveOnes() {
        // Arrange
        List<PackageOffer> all = List.of(
                buildOffer(1L, "Active Plan",   true),
                buildOffer(2L, "Inactive Plan", false)
        );
        when(offerRepo.findAll()).thenReturn(all);

        // Act
        List<PackageOfferResponse> result = packageOfferService.listAll();

        // Assert: both offers are returned
        assertThat(result).hasSize(2);
    }

    // ===========================
    // listActive
    // ===========================

    @Test
    void listActive_shouldReturnOnlyActiveOffers() {
        // Arrange
        List<PackageOffer> activeOffers = List.of(
                buildOffer(1L, "Active Plan", true)
        );
        when(offerRepo.findByIsActiveTrue()).thenReturn(activeOffers);

        // Act
        List<PackageOfferResponse> result = packageOfferService.listActive();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    // ===========================
    // update
    // ===========================

    @Test
    void update_shouldUpdateAllFields_whenOfferExists() {
        // Arrange
        PackageOffer existing = buildOffer(1L, "Old Name", true);
        PackageOffer changes = buildOffer(1L, "New Name", true);
        changes.setPrice(BigDecimal.valueOf(149.99));
        changes.setDurationDays(60);

        when(offerRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(offerRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PackageOfferResponse result = packageOfferService.update(1L, changes);

        // Assert
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualByComparingTo("149.99");
        assertThat(result.getDurationDays()).isEqualTo(60);
    }

    @Test
    void update_shouldThrowNotFoundException_whenOfferDoesNotExist() {
        // Arrange
        when(offerRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> packageOfferService.update(99L, new PackageOffer()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ===========================
    // setActive (enable / disable)
    // ===========================

    @Test
    void setActive_shouldEnableOffer_whenCalledWithTrue() {
        // Arrange
        PackageOffer offer = buildOffer(1L, "Plan", false);
        when(offerRepo.findById(1L)).thenReturn(Optional.of(offer));

        // Act
        packageOfferService.setActive(1L, true);

        // Assert
        assertThat(offer.getIsActive()).isTrue();
        verify(offerRepo).save(offer);
    }

    @Test
    void setActive_shouldDisableOffer_whenCalledWithFalse() {
        // Arrange
        PackageOffer offer = buildOffer(1L, "Plan", true);
        when(offerRepo.findById(1L)).thenReturn(Optional.of(offer));

        // Act
        packageOfferService.setActive(1L, false);

        // Assert
        assertThat(offer.getIsActive()).isFalse();
        verify(offerRepo).save(offer);
    }

    @Test
    void setActive_shouldThrowNotFoundException_whenOfferDoesNotExist() {
        // Arrange
        when(offerRepo.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> packageOfferService.setActive(99L, true))
                .isInstanceOf(NotFoundException.class);
    }

    // ===========================
    // addItem
    // ===========================

    @Test
    void addItem_shouldAddItemToOffer_andReturnUpdatedResponse() {
        // Arrange
        PackageOffer offer = buildOffer(1L, "Plan", true);
        AddPackageItemRequest itemReq = new AddPackageItemRequest();
        itemReq.setItemType(PackageItemType.COURSE);
        itemReq.setItemId(42L);

        when(offerRepo.findById(1L)).thenReturn(Optional.of(offer));
        when(itemRepo.save(any(PackageItem.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PackageOfferResponse response = packageOfferService.addItem(1L, itemReq);

        // Assert: the offer now has one item
        assertThat(offer.getItems()).hasSize(1);
        assertThat(offer.getItems().get(0).getItemId()).isEqualTo(42L);
        verify(itemRepo, times(1)).save(any(PackageItem.class));
    }

    @Test
    void addItem_shouldThrowNotFoundException_whenOfferDoesNotExist() {
        // Arrange
        when(offerRepo.findById(99L)).thenReturn(Optional.empty());

        AddPackageItemRequest itemReq = new AddPackageItemRequest();
        itemReq.setItemType(PackageItemType.COURSE);
        itemReq.setItemId(1L);

        // Act & Assert
        assertThatThrownBy(() -> packageOfferService.addItem(99L, itemReq))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ===========================
    // searchByName
    // ===========================

    @Test
    void searchByName_shouldReturnMatchingOffers() {
        // Arrange
        List<PackageOffer> matches = List.of(
                buildOffer(1L, "English Basic", true),
                buildOffer(2L, "English Advanced", true)
        );
        when(offerRepo.findByNameContainingIgnoreCase("english")).thenReturn(matches);

        // Act
        List<PackageOfferResponse> result = packageOfferService.searchByName("english");

        // Assert
        assertThat(result).hasSize(2);
        verify(offerRepo, times(1)).findByNameContainingIgnoreCase("english");
    }

    @Test
    void searchByName_shouldReturnEmptyList_whenNothingMatches() {
        // Arrange
        when(offerRepo.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());

        // Act
        List<PackageOfferResponse> result = packageOfferService.searchByName("xyz");

        // Assert
        assertThat(result).isEmpty();
    }
}
