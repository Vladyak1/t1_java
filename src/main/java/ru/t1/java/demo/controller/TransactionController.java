package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.dto.ClientStatusRequest;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;

@Slf4j
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TransactionService transactionService;

    private final String transactionAcceptUrl = "http://localhost:8080/transaction-accept/checkClientStatus";

    @LogException
    @Track
    @HandlingResult
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        log.info("Вызов PostMapping /transactions объекта: {}", transaction);

        if (transactionService.isClientBlockedOrInactive(transaction.getClientId())) {
            log.info("Клиент с ID: {} заблокирован/неактивен", transaction.getClientId());
            ResponseEntity<String> response = restTemplate.postForEntity(transactionAcceptUrl,
                    new ClientStatusRequest(transaction.getClientId(), transaction.getAccountId()), String.class);

            if (response.getBody().equals("BLOCKED") || response.getBody().equals("CLIENT_NOT_FOUND")) {
                log.info("PostMapping /transactions отказал в транзакции: {} " +
                        "и заблокировал остальные открытые транзакции", transaction);
                transactionService.blockClientAndTransaction(transaction);
                return ResponseEntity.ok(transaction);
            }
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("PostMapping /transactions успешно выполнен: {}", savedTransaction);
        return ResponseEntity.ok(savedTransaction);
    }

    @LogException
    @Track
    @HandlingResult
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        log.info("Запрос информации о транзакции с ID: {}", id);
        return transactionRepository.findById(id)
                .map(transaction -> {
                    log.info("Транзакция найдена: {}", transaction);
                    return ResponseEntity.ok(transaction);
                })
                .orElseGet(() -> {
                    log.warn("Транзакция с ID {} не найдена", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
