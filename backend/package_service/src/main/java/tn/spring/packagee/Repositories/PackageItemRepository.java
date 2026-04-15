package tn.spring.packagee.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.packagee.Entities.PackageItem;

import java.util.List;

public interface PackageItemRepository extends JpaRepository<PackageItem, Long> {
    List<PackageItem> findByPackageOfferId(Long packageOfferId);
}