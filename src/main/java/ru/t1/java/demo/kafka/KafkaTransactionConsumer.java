package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {

    private final TransactionRepository transactionRepository;

    @KafkaListener(id = "${t1.kafka.consumer.group-id}", topics = "t1_demo_transactions")
    public void listener(Transaction transaction) {
        try {
            log.info("KafkaListener получил Transaction: {}", transaction);
            transactionRepository.save(transaction);
            log.info("Transaction сохранен в БД");
        } catch (Exception e) {
            log.error("Ошибка сохранения Transaction: ", e);
        }
    }
}
