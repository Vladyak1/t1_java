package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.kafka.KafkaTransactionProducer;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.Transaction.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;
import ru.t1.java.demo.service.AccountService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final KafkaTransactionProducer kafkaProducer;


    @Override
    public void processTransaction(Transaction transaction) {
        try {
            Account account = accountService.getAccountByAccountId(transaction.getAccountId());

            if (account != null && account.getStatus() == Account.AccountStatus.OPEN) {
                transaction.setStatus(TransactionStatus.REQUESTED);
                transactionRepository.save(transaction);

                accountService.changeBalance(account, transaction.getAmount());

                Map<String, Object> message = new HashMap<>();
                message.put("clientId", transaction.getClientId());
                message.put("accountId", account.getAccountId());
                message.put("transactionId", transaction.getTransactionId());
                message.put("timestamp", transaction.getTimestamp());
                message.put("amount", transaction.getAmount());
                message.put("balance", account.getBalance());

                kafkaProducer.sendMessage("t1_demo_transaction_accept", message);

                log.info("Транзакция проведена успешно, сообщение отправлено в Kafka.");
            } else {
                log.warn("Account не найден или не в статусе OPEN. Транзакция не проведена.");
            }
        } catch (Exception e) {
            log.error("Ошибка выполнения транзакции: ", e);
        }
    }
}
