package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaAccountConsumer {

    private final AccountRepository accountRepository;

    @KafkaListener(topics = "t1_demo_accounts")
    public void listener(Account account) {
        try {
            log.info("KafkaListener получил Account: {}", account);
            accountRepository.save(account);
            log.info("Account сохранен в БД");
        } catch (Exception e) {
            log.error("Ошибка сохранения Account: ", e);
        }
    }
}
