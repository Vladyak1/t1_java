package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;

@Slf4j
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @LogException
    @Track
    @HandlingResult
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        log.info("Вызов PostMapping /transactions объекта: {}", transaction);
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

