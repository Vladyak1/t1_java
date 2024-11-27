package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.model.dto.ClientStatusRequest;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;

@Slf4j
@RestController
@RequestMapping("/transaction-accept")
@RequiredArgsConstructor
public class TransactionAcceptController {

    private final AccountRepository accountRepository;

    @PostMapping("/checkClientStatus")
    public ResponseEntity<String> checkClientStatus(@RequestBody ClientStatusRequest request) {
        log.info("Проверка статуса клиента с ID: {}", request.getClientId());
        Account account = accountRepository.findByClientId(request.getClientId());

        if (account == null) {
            log.info("Не найден аккаунт клиента с ID: {}", request.getClientId());
            return ResponseEntity.status(404).body("CLIENT_NOT_FOUND");
        }

        switch (account.getStatus()) {
            case BLOCKED:
            case ARRESTED:
            case CLOSED:
                log.info("Аккаунт клиента с ID: {} заблокирован/неактивен", request.getClientId());
                return ResponseEntity.ok("BLOCKED");
            case OPEN:
                log.info("Аккаунт клиента с ID: {} активен", request.getClientId());
                return ResponseEntity.ok("ACTIVE");
            default:
                log.info("Неизвестный статус аккаунта клиента с ID: {}", request.getClientId());
                return ResponseEntity.status(500).body("UNKNOWN_STATUS");
        }
    }
}
