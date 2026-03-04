package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.TransactionStatus;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(
        name = "payment_transaction",
        indexes = {
                @Index(name = "idx_tx_payment", columnList = "paymentId"),
                @Index(name = "idx_tx_provider_ref", columnList = "providerRef")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false, length = 20)
    private String provider; // STRIPE / PAYPAL / FLOUCI / ...

    @Column(nullable = false, length = 120)
    private String providerRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionStatus status = TransactionStatus.INITIATED;

    @Lob
    private String requestPayload;

    @Lob
    private String responsePayload;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // getters/setters

}