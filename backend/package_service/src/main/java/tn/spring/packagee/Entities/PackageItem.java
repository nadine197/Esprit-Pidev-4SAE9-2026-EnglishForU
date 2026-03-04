package tn.spring.packagee.Entities;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PackageItemType;

@Setter
@Getter
@Entity
@Table(
        name = "package_item",
        indexes = {
                @Index(name = "idx_package_item_package", columnList = "package_offer_id")
        }
)
public class PackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link to PackageOffer
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "package_offer_id", nullable = false)
    private PackageOffer packageOffer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PackageItemType itemType;

    // ID from another microservice (course-service, group-service, event-service)
    @Column(nullable = false)
    private Long itemId;


}