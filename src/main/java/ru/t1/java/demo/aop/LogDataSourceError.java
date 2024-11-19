package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

@Slf4j
@Aspect
@Component
@Order(0)
public class LogDataSourceError {

    @Autowired
    private DataSourceErrorLogRepository dataSourceErrorLogRepository;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private final String topic = "t1_demo_metrics";
    private final String errorType = "DATA_SOURCE";

    @Pointcut("within(ru.t1.java.demo.*)")
    public void loggingMethods() {}

    @After("@annotation(ru.t1.java.demo.aop.LogException)")
    public void logError(JoinPoint joinPoint, Exception ex) {
        log.info("Была перехвачена ошибка в методе: {}", joinPoint.getSignature().toShortString());
        String message = createKafkaMessage(ex);
        try {
            kafkaTemplate.send(topic, message);
        } catch (Exception e) {
            log.error("Ошибка в методе logError при отправлении сообщения в Kafka: {}", e.getMessage());
            try {
                saveToDatabase(ex);
            } catch (Exception dbEx) {
                log.error("Ошибка в методе logError при отправлении сообщения в БД: {}", dbEx.getMessage());
            }
        }
    }

    private String createKafkaMessage(Exception ex) {
        return String.format("{\"type\":\"%s\",\"message\":\"%s\",\"stackTrace\":\"%s\",\"methodSignature\":\"%s\"}",
                errorType, ex.getMessage(), ex.getStackTrace().toString(), ex.getStackTrace()[0].toString());
    }

    private void saveToDatabase(Exception ex) {
        log.info("LoggingDataSourceError отработал и сохранил в БД после перехвата ошибки: {}", ex.getMessage());
        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .stackTrace(ex.getStackTrace().toString())
                .message(ex.getMessage())
                .methodSignature(ex.getStackTrace()[0].toString())
                .build();
        dataSourceErrorLogRepository.save(errorLog);
    }
}
