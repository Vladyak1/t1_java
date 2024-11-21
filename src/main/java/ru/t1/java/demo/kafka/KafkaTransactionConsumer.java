package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.TransactionService;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionConsumer {

    private final TransactionService transactionService;

    @KafkaListener(id = "${t1.kafka.consumer.group-id}", topics = "t1_demo_transactions")
    public void listener(Transaction transaction) {
        try {
            log.info("KafkaListener получил Transaction: {}", transaction);
            transactionService.processTransaction(transaction);
            log.info("Transaction обработан");
        } catch (Exception e) {
            log.error("Ошибка обработки Transaction: ", e);
        }
    }
}
