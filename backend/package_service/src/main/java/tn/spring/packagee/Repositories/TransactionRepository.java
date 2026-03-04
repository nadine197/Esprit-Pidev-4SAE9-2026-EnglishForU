package tn.spring.packagee.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.packagee.Entities.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByPaymentId(Long paymentId);
    Optional<Transaction> findByProviderRef(String providerRef);
}