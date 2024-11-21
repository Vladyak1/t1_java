package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@ToString
@AllArgsConstructor
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "balance", precision = 19, scale = 2, nullable = false)
    private Double balance;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "frozen_amount", precision = 19, scale = 2, nullable = false)
    private Double frozenAmount = 0.0;

    public enum AccountStatus {
        ARRESTED, BLOCKED, CLOSED, OPEN
    }

    public enum AccountType {
        DEBIT,
        CREDIT
    }

    public Account() {
        this.status = AccountStatus.OPEN;
    }
}
