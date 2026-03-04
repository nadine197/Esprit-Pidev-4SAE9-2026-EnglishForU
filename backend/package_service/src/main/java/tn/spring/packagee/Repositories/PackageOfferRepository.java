package tn.spring.packagee.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.packagee.Entities.PackageOffer;

import java.util.List;

public interface PackageOfferRepository extends JpaRepository<PackageOffer, Long> {
    List<PackageOffer> findByIsActiveTrue();
    List<PackageOffer> findByNameContainingIgnoreCase(String q);
}