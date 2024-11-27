package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Transaction;

public interface TransactionService {
    void processTransaction(Transaction transaction);

    boolean isClientBlockedOrInactive(Long clientId);

    void blockClientAndTransaction(Transaction transaction);
}
