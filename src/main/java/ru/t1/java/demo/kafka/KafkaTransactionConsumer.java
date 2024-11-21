package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.Transaction.TransactionStatus;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;


    @KafkaListener(id = "${t1.kafka.consumer.group-id}", topics = {"t1_demo_transactions", "t1_demo_transaction_result"})
    public void listener(Object message) {
        try {
            if (message instanceof Transaction transaction) {
                log.info("KafkaListener получил Transaction: {}", transaction);
                transactionRepository.save(transaction);
                log.info("Transaction сохранен в БД");
            } else if (message instanceof Map) {
                Map<String, Object> resultMessage = (Map<String, Object>) message;
                String status = (String) resultMessage.get("status");
                Long clientId = (Long) resultMessage.get("clientId");
                Long accountId = (Long) resultMessage.get("accountId");

                processResultMessage(status, clientId, accountId);

            }
        } catch (Exception e) {
            log.error("Ошибка обработки сообщения: ", e);
        }
    }

    private void processResultMessage(String status, Long clientId, Long accountId) {
        try {
            TransactionStatus transactionStatus = TransactionStatus.valueOf(status);
            switch (transactionStatus) {
                case ACCEPTED:
                    updateTransactionStatus(accountId, transactionStatus);
                    break;
                case BLOCKED:
                    updateTransactionStatus(accountId, transactionStatus);
                    blockAccount(clientId, accountId);
                    break;
                case REJECTED:
                    updateTransactionStatus(accountId, transactionStatus);
                    rejectTransaction(clientId, accountId);
                    break;
                default:
                    log.warn("Для этого статуса не предусмотрено действий: {}", status);
            }
        } catch (IllegalArgumentException e) {
            log.error("Неизвестный статус: {}", status, e);
        }
    }

    private void updateTransactionStatus(Long accountId, TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        for(Transaction transaction : transactions){
            transaction.setStatus(status);
            transactionRepository.save(transaction);
        }
    }


    private void blockAccount(Long clientId, Long accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account != null) {
            BigDecimal blockedAmount = calculateBlockedAmount(accountId);
            account.setBalance(account.getBalance() - blockedAmount.doubleValue());
            account.setFrozenAmount(account.getFrozenAmount() + blockedAmount.doubleValue());
            account.setStatus(Account.AccountStatus.BLOCKED);
            accountRepository.save(account);
            log.info("Статус Аккаунта {} изменен на BLOCKED. Атрибут frozenAmount: {}", accountId, blockedAmount);
        }
    }

    private BigDecimal calculateBlockedAmount(Long accountId){
        List<Transaction> transactions = transactionRepository.findByAccountIdAndStatus(accountId, TransactionStatus.BLOCKED);
        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalAmount;
    }

    private void rejectTransaction(Long clientId, Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        for(Transaction transaction : transactions){
            if(transaction.getStatus() == TransactionStatus.REQUESTED) {
                accountService.changeBalance(accountRepository.findById(accountId).get(), transaction.getAmount().negate());
                transaction.setStatus(TransactionStatus.REJECTED);
                transactionRepository.save(transaction);
            }
        }
    }
}
