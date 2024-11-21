package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTransactionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <K, V> void sendMessage(String topic, V data) {
        kafkaTemplate.send(topic, data);
    }
}
