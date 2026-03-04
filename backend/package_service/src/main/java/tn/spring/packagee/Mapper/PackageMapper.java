package tn.spring.packagee.Mapper;


import tn.spring.packagee.DTOs.PackageOfferResponse;
import tn.spring.packagee.Entities.PackageItem;
import tn.spring.packagee.Entities.PackageOffer;

import java.util.stream.Collectors;

public class PackageMapper {

    public static PackageOfferResponse toResponse(PackageOffer e) {
        PackageOfferResponse r = new PackageOfferResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setDescription(e.getDescription());
        r.setType(e.getType());
        r.setDurationDays(e.getDurationDays());
        r.setPrice(e.getPrice());
        r.setIsActive(e.getIsActive());
        r.setCreatedAt(e.getCreatedAt());

        r.setItems(e.getItems().stream().map(PackageMapper::toItemResponse).collect(Collectors.toList()));
        return r;
    }

    private static PackageOfferResponse.PackageItemResponse toItemResponse(PackageItem i) {
        PackageOfferResponse.PackageItemResponse r = new PackageOfferResponse.PackageItemResponse();
        r.setId(i.getId());
        r.setItemType(i.getItemType().name());
        r.setItemId(i.getItemId());
        return r;
    }
}