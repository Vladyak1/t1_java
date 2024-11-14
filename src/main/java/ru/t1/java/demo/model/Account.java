package ru.t1.java.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import ru.t1.java.demo.model.enums.AccountType;

@Getter
@Setter
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "account")
public class Account {

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "account_type")
    private AccountType accountType;

    @Column(name = "balance")
    private Double balance;
}
