package tn.spring.packagee.DTOs;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PackageItemType;

@Setter
@Getter
public class AddPackageItemRequest {
    @NotNull
    private PackageItemType itemType;

    @NotNull
    private Long itemId;

}