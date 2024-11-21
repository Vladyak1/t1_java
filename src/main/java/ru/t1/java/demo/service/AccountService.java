package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Account;

import java.math.BigDecimal;

public interface AccountService {

    Account getAccountByAccountId(Long accountId);

    void changeBalance(Account account, BigDecimal amount);
}
