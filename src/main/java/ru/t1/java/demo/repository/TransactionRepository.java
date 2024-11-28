package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.java.demo.model.Transaction;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByClientIdAndAccountIdAndTimestampAfter(Long clientId, Long accountId, Instant timestamp);

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByAccountIdAndStatus(Long accountId, Transaction.TransactionStatus transactionStatus);

    List<Transaction> findByClientIdAndStatus(Long clientId, Transaction.TransactionStatus transactionStatus);
}