package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import tn.spring.packagee.Enum.TransactionStatus;

import java.time.Instant;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderRef() { return providerRef; }
    public void setProviderRef(String providerRef) { this.providerRef = providerRef; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}