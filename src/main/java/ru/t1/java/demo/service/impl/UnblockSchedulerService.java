package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.UnblockResponse;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnblockSchedulerService {

    @Value("${unblock.scheduler.clients.count}")
    private Integer clientsCount;

    @Value("${unblock.scheduler.accounts.count}")
    private Integer accountsCount;

    @Value("${unblock.scheduler.period}")
    private Long period;

    @Value("${service3.url}")
    private String service3Url;

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final RestTemplate restTemplate;

    @Scheduled(fixedDelayString = "${unblock.scheduler.period}")
    public void processBlockedClients() {
        log.info("Starting scheduled processing of blocked clients. Batch size: {}", clientsCount);
        List<Client> blockedClients = clientRepository.findByBlockedForTrue()
                .stream()
                .limit(clientsCount)
                .toList();

        log.debug("Found {} blocked clients to process", blockedClients.size());

        blockedClients.forEach(client -> {
            try {
                ResponseEntity<UnblockResponse> response = restTemplate.postForEntity(
                        service3Url + "/unblock/client/" + client.getId(),
                        null,
                        UnblockResponse.class
                );
                log.info("Unblock request processed for client: {}. Status: {}",
                        client.getId(), response.getBody().isUnblocked());
            } catch (Exception e) {
                log.error("Error processing unblock request for client: {}", client.getId(), e);
            }
        });
    }

    @Scheduled(fixedDelayString = "${unblock.scheduler.period}")
    public void processArrestedAccounts() {
        log.info("Starting scheduled processing of arrested accounts. Batch size: {}", accountsCount);
        List<Account> arrestedAccounts = accountRepository.findByStatus(Account.AccountStatus.ARRESTED)
                .stream()
                .limit(accountsCount)
                .toList();

        log.debug("Found {} arrested accounts to process", arrestedAccounts.size());

        arrestedAccounts.forEach(account -> {
            try {
                ResponseEntity<UnblockResponse> response = restTemplate.postForEntity(
                        service3Url + "/api/v1/unblock/account/" + account.getAccountId(),
                        null,
                        UnblockResponse.class
                );
                log.info("Unblock request processed for account: {}. Status: {}",
                        account.getAccountId(), response.getBody().isUnblocked());
            } catch (Exception e) {
                log.error("Error processing unblock request for account: {}", account.getAccountId(), e);
            }
        });
    }
}
