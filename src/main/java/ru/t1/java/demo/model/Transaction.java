package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "transaction")
public class Transaction extends AbstractPersistable<Long> {

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId; //Добавлен уникальный transactionId

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING) //Важно для Enum
    private TransactionStatus status; //Добавлен статус

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp; //Добавлен timestamp


    public Transaction() {
        this.transactionId = java.util.UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.status = TransactionStatus.REQUESTED; // Default status
    }


    public enum TransactionStatus {
        ACCEPTED, REJECTED, BLOCKED, CANCELLED, REQUESTED
    }
}