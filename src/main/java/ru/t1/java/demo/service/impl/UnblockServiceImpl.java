package ru.t1.java.demo.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.UnblockResponse;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.service.UnblockService;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnblockServiceImpl implements UnblockService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    @Override
    public UnblockResponse unblockClient(Long id) {
        log.info("Processing unblock request for client with id: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Client not found with id: {}", id);
                    return new EntityNotFoundException("Client not found with id: " + id);
                });

        boolean decision = makeUnblockDecision();

        if (decision) {
            client.setBlockedFor(false);
            clientRepository.save(client);
            log.info("Client {} has been unblocked", id);
        } else {
            log.info("Unblock request denied for client {}", id);
        }

        return new UnblockResponse(id, decision);
    }

    @Override
    public UnblockResponse unblockAccount(Long id) {
        log.info("Processing unblock request for account with id: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", id);
                    return new EntityNotFoundException("Account not found with id: " + id);
                });

        boolean decision = makeUnblockDecision();

        if (decision) {
            account.setStatus(Account.AccountStatus.OPEN);
            accountRepository.save(account);
            log.info("Account {} has been unblocked", id);
        } else {
            log.info("Unblock request denied for account {}", id);
        }

        return new UnblockResponse(id, decision);
    }

    private boolean makeUnblockDecision() {
        return new Random().nextBoolean();
    }
}
