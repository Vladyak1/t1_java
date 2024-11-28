package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.model.UnblockResponse;
import ru.t1.java.demo.service.UnblockService;

@RestController
@RequestMapping("/unblock")
@RequiredArgsConstructor
@Slf4j
public class UnblockController {

    private final UnblockService unblockService;

    @PostMapping("/client/{id}")
    public ResponseEntity<UnblockResponse> unblockClient(@PathVariable Long id) {
        log.info("Received unblock request for client with id: {}", id);
        UnblockResponse response = unblockService.unblockClient(id);
        log.debug("Unblock response for client {}: {}", id, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/account/{id}")
    public ResponseEntity<UnblockResponse> unblockAccount(@PathVariable Long id) {
        log.info("Received unblock request for account with id: {}", id);
        UnblockResponse response = unblockService.unblockAccount(id);
        log.debug("Unblock response for account {}: {}", id, response);
        return ResponseEntity.ok(response);
    }
}
