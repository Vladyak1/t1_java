package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;

@Slf4j
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @LogException
    @Track
    @HandlingResult
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        log.info("Вызов PostMapping /accounts объекта: {}", account);
        Account savedAccount = accountRepository.save(account);
        log.info("PostMapping /account успешно выполнен: {}", savedAccount);
        return ResponseEntity.ok(savedAccount);
    }

    @LogException
    @Track
    @HandlingResult
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        log.info("Вызов GetMapping /accounts/{id} с Id: {}", id);
        return accountRepository.findById(id)
                .map(account -> {
                    log.info("Счет найден: {}", account);
                    return ResponseEntity.ok(account);
                })
                .orElseGet(() -> {
                    log.warn("Счет с ID {} не найден", id);
                    return ResponseEntity.notFound().build();
                });
    }
}

