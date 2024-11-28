package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.Transaction.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.TransactionAcceptService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionAcceptServiceImpl implements TransactionAcceptService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final KafkaTransactionProducer kafkaProducer;

    @Value("${t1.transactionThresholdCount}")
    private int transactionThresholdCount;

    @Value("${t1.transactionThresholdTimeSeconds}")
    private long transactionThresholdTimeSeconds;

    private final ConcurrentHashMap<String, TransactionCounter> transactionCounters = new ConcurrentHashMap<>();


    @KafkaListener(topics = "t1_demo_transaction_accept")
    public void listener(Map<String, Object> message) {
        try {
            Long clientId = (Long) message.get("clientId");
            Long accountId = (Long) message.get("accountId");
            String transactionId = (String) message.get("transactionId");
            Instant timestamp = (Instant) message.get("timestamp");
            BigDecimal amount = (BigDecimal) message.get("amount");
            Double balance = (Double) message.get("balance");

            String key = clientId + ":" + accountId;
            TransactionCounter counter = transactionCounters.computeIfAbsent(key,
                    k -> new TransactionCounter(transactionThresholdCount, transactionThresholdTimeSeconds));

            if (counter.increment(timestamp)) {
                log.info("Превышено допустимое количество транзакций для клиента: {} с аккаунтом: {}",
                        clientId, accountId);
                setBlockedStatus(clientId, accountId,transactionThresholdCount);
                sendMessageToResultTopic(TransactionStatus.BLOCKED, clientId, accountId);
            } else {
                Account account = accountService.getAccountByAccountId(accountId);

                if (account == null || account.getBalance() < amount.doubleValue()) {
                    sendMessageToResultTopic(TransactionStatus.REJECTED, clientId, accountId);
                } else {
                    Transaction transaction = new Transaction();
                    transaction.setAccountId(accountId);
                    transaction.setClientId(clientId);
                    transaction.setAmount(amount);
                    transaction.setStatus(TransactionStatus.ACCEPTED);
                    transaction.setTransactionId(transactionId);
                    transaction.setTimestamp(timestamp);
                    transactionRepository.save(transaction);
                    sendMessageToResultTopic(TransactionStatus.ACCEPTED, clientId, accountId);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка выполнения транзакции: ", e);
        }
    }

    private void setBlockedStatus(Long clientId, Long accountId, int thresholdCount) {
        Instant thresholdTime = Instant.now().minusSeconds(transactionThresholdTimeSeconds);
        List<Transaction> transactions = transactionRepository.findByClientIdAndAccountIdAndTimestampAfter(clientId,
                accountId, thresholdTime);

        int count = 0;
        for (Transaction transaction : transactions) {
            if (count < thresholdCount && transaction.getStatus() != TransactionStatus.BLOCKED) {
                transaction.setStatus(TransactionStatus.BLOCKED);
                transactionRepository.save(transaction);
                count++;
            }
        }
    }

    private void sendMessageToResultTopic(TransactionStatus status, Long clientId, Long accountId) {
        Map<String, Object> message = new HashMap<>();
        message.put("status", status);
        message.put("clientId", clientId);
        message.put("accountId", accountId);
        kafkaProducer.sendTo("t1_demo_transaction_result", message);
    }


    private static class TransactionCounter {
        private final int thresholdCount;
        private final Duration thresholdTime;
        private int count;
        private Instant lastTransactionTime;


        public TransactionCounter(int thresholdCount, long thresholdTimeSeconds) {
            this.thresholdCount = thresholdCount;
            this.thresholdTime = Duration.ofSeconds(thresholdTimeSeconds);
        }

        public synchronized boolean increment(Instant timestamp) {
            if (lastTransactionTime == null || timestamp.isAfter(lastTransactionTime.plus(thresholdTime))) {
                count = 1;
                lastTransactionTime = timestamp;
            } else {
                count++;
            }
            return count >= thresholdCount;
        }
    }
}
