package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionAcceptServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private KafkaTransactionProducer kafkaProducer;

    @InjectMocks
    private TransactionAcceptServiceImpl transactionAcceptService;

    private Map<String, Object> testMessage;
    private Account testAccount;
    private static final Long CLIENT_ID = 1L;
    private static final Long ACCOUNT_ID = 1L;
    private static final String TRANSACTION_ID = "test-transaction-id";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100.00);
    private static final Double BALANCE = 1000.00;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionAcceptService, "transactionThresholdCount", 3);
        ReflectionTestUtils.setField(transactionAcceptService, "transactionThresholdTimeSeconds", 60);

        testMessage = new HashMap<>();
        testMessage.put("clientId", CLIENT_ID);
        testMessage.put("accountId", ACCOUNT_ID);
        testMessage.put("transactionId", TRANSACTION_ID);
        testMessage.put("timestamp", Instant.now());
        testMessage.put("amount", AMOUNT);
        testMessage.put("balance", BALANCE);

        testAccount = new Account();
        testAccount.setAccountId(ACCOUNT_ID);
        testAccount.setClientId(CLIENT_ID);
        testAccount.setBalance(BALANCE);
    }

    @Test
    void listener_WhenValidTransaction_ShouldAccept() {
        when(accountService.getAccountByAccountId(ACCOUNT_ID)).thenReturn(testAccount);

        transactionAcceptService.listener(testMessage);

        verify(transactionRepository).save(argThat(transaction ->
                transaction.getStatus() == Transaction.TransactionStatus.ACCEPTED &&
                        transaction.getAmount().equals(AMOUNT)
        ));
    }

    @Test
    void listener_WhenInsufficientBalance_ShouldReject() {
        testMessage.put("amount", BigDecimal.valueOf(2000.00));
        when(accountService.getAccountByAccountId(ACCOUNT_ID)).thenReturn(testAccount);

        transactionAcceptService.listener(testMessage);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void listener_WhenAccountNotFound_ShouldReject() {
        when(accountService.getAccountByAccountId(ACCOUNT_ID)).thenReturn(null);

        transactionAcceptService.listener(testMessage);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void listener_WhenThresholdExceeded_ShouldBlock() {
        when(accountService.getAccountByAccountId(ACCOUNT_ID)).thenReturn(testAccount);

        for (int i = 0; i < 4; i++) {
            transactionAcceptService.listener(testMessage);
        }

        verify(transactionRepository, atMost(3)).save(any());
    }

    @Test
    void listener_WhenExceptionOccurs_ShouldHandleGracefully() {
        when(accountService.getAccountByAccountId(ACCOUNT_ID))
                .thenThrow(new RuntimeException("Test exception"));

        transactionAcceptService.listener(testMessage);

        verify(transactionRepository, never()).save(any());
    }

}


