package ru.t1.java.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction")
public class Transaction {

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;
}
