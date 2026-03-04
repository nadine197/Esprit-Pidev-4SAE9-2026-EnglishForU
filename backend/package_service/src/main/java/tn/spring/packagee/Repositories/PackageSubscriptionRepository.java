package tn.spring.packagee.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.packagee.Entities.PackageSubscription;
import tn.spring.packagee.Enum.SubscriptionStatus;

import java.util.List;

public interface PackageSubscriptionRepository extends JpaRepository<PackageSubscription, Long> {
    List<PackageSubscription> findByStudentId(Long studentId);
    List<PackageSubscription> findByStudentIdAndStatus(Long studentId, SubscriptionStatus status);
}