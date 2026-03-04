package tn.spring.packagee.Entities;



import jakarta.persistence.*;
import tn.spring.packagee.Enum.PackageItemType;

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

    // getters/setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PackageOffer getPackageOffer() { return packageOffer; }
    public void setPackageOffer(PackageOffer packageOffer) { this.packageOffer = packageOffer; }

    public PackageItemType getItemType() { return itemType; }
    public void setItemType(PackageItemType itemType) { this.itemType = itemType; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}