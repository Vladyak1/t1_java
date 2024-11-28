package ru.t1.java.demo.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Account getAccountByAccountId(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    @Override
    public void changeBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance() + amount.doubleValue());
        accountRepository.save(account);
    }
}
