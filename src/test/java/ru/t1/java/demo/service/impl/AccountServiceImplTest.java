package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setClientId(1L);
        testAccount.setBalance(1000.0);
        testAccount.setStatus(Account.AccountStatus.OPEN);
        testAccount.setAccountType(Account.AccountType.DEBIT);
        testAccount.setFrozenAmount(0.0);
    }

    @Test
    void getAccountByAccountId_WhenAccountExists_ShouldReturnAccount() {
        when(accountRepository.findById(1L)).thenReturn(java.util.Optional.of(testAccount));

        Account result = accountService.getAccountByAccountId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccountByAccountId_WhenAccountDoesNotExist_ShouldReturnNull() {
        when(accountRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        Account result = accountService.getAccountByAccountId(1L);

        assertNull(result);
        verify(accountRepository).findById(1L);
    }

    @Test
    void changeBalance_WhenPositiveAmount_ShouldIncreaseBalance() {
        BigDecimal amount = new BigDecimal("100.00");
        double initialBalance = testAccount.getBalance();

        accountService.changeBalance(testAccount, amount);

        assertEquals(initialBalance + amount.doubleValue(), testAccount.getBalance());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void changeBalance_WhenNegativeAmount_ShouldDecreaseBalance() {
        BigDecimal amount = new BigDecimal("-100.00");
        double initialBalance = testAccount.getBalance();

        accountService.changeBalance(testAccount, amount);

        assertEquals(initialBalance + amount.doubleValue(), testAccount.getBalance());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void changeBalance_WhenAccountIsNull_ShouldThrowException() {
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(IllegalArgumentException.class, () ->
                accountService.changeBalance(null, amount)
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void changeBalance_WhenAmountIsNull_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                accountService.changeBalance(testAccount, null)
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void changeBalance_WhenAccountIsNotOpen_ShouldThrowException() {
        testAccount.setStatus(Account.AccountStatus.BLOCKED);
        BigDecimal amount = new BigDecimal("100.00");

        assertThrows(IllegalStateException.class, () ->
                accountService.changeBalance(testAccount, amount)
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void changeBalance_WhenInsufficientFunds_ShouldThrowException() {
        BigDecimal amount = new BigDecimal("-2000.00");

        assertThrows(IllegalStateException.class, () ->
                accountService.changeBalance(testAccount, amount)
        );

        verify(accountRepository, never()).save(any());
    }
}

