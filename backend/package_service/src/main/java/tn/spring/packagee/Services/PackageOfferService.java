package tn.spring.packagee.Services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.packagee.DTOs.AddPackageItemRequest;
import tn.spring.packagee.DTOs.CreatePackageOfferRequest;
import tn.spring.packagee.DTOs.PackageOfferResponse;
import tn.spring.packagee.Entities.PackageItem;
import tn.spring.packagee.Entities.PackageOffer;
import tn.spring.packagee.Exceptions.NotFoundException;
import tn.spring.packagee.Mapper.PackageMapper;
import tn.spring.packagee.Repositories.PackageItemRepository;
import tn.spring.packagee.Repositories.PackageOfferRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackageOfferService {

    private final PackageOfferRepository offerRepo;
    private final PackageItemRepository itemRepo;


    public PackageOfferService(PackageOfferRepository offerRepo, PackageItemRepository itemRepo) {
        this.offerRepo = offerRepo;
        this.itemRepo = itemRepo;
    }

    public PackageOfferResponse create(CreatePackageOfferRequest req) {
        PackageOffer e = new PackageOffer();
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setType(req.getType());
        e.setDurationDays(req.getDurationDays());
        e.setPrice(req.getPrice());
        e.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

        e = offerRepo.save(e);
        return PackageMapper.toResponse(e);
    }

    public PackageOfferResponse addItem(Long packageOfferId, AddPackageItemRequest req) {
        PackageOffer offer = offerRepo.findById(packageOfferId)
                .orElseThrow(() -> new NotFoundException("PackageOffer not found: " + packageOfferId));

        PackageItem item = new PackageItem();
        item.setPackageOffer(offer);
        item.setItemType(req.getItemType());
        item.setItemId(req.getItemId());

        offer.getItems().add(item);
        itemRepo.save(item);

        return PackageMapper.toResponse(offer);
    }

    @Transactional(readOnly = true)
    public List<PackageOfferResponse> listActive() {
        return offerRepo.findByIsActiveTrue().stream()
                .map(PackageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PackageOfferResponse> searchByName(String q) {
        return offerRepo.findByNameContainingIgnoreCase(q).stream()
                .map(PackageMapper::toResponse)
                .collect(Collectors.toList());
    }
}