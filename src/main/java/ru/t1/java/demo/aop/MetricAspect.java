package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Async
@Slf4j
@Aspect
@Component
@Order(1)
public class MetricAspect {

    private static final AtomicLong START_TIME = new AtomicLong();
    private static final String TOPIC_NAME = "t1_demo_metrics";
    private static final long MAX_EXECUTION_TIME_MILLIS = 1000;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Before("@annotation(ru.t1.java.demo.aop.Track)")
    public void logExecTime(JoinPoint joinPoint) throws Throwable {
        log.info("Старт метода: {}", joinPoint.getSignature().toShortString());
        START_TIME.addAndGet(System.currentTimeMillis());
    }

    @After("@annotation(ru.t1.java.demo.aop.Track)")
    public void calculateTime(JoinPoint joinPoint) {
        long afterTime = System.currentTimeMillis();
        log.info("Время исполнения: {} ms", (afterTime - START_TIME.get()));
        START_TIME.set(0L);
    }

    @Around("@annotation(ru.t1.java.demo.aop.Track)")
    public Object logExecTime(ProceedingJoinPoint pJoinPoint) throws Throwable {
        log.info("Вызов метода: {}", pJoinPoint.getSignature().toShortString());
        long beforeTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = pJoinPoint.proceed();//Important
        } finally {
            long afterTime = System.currentTimeMillis();
            long executionTime = afterTime - beforeTime;
            log.info("Время исполнения: {} ms", executionTime);
            if (executionTime > MAX_EXECUTION_TIME_MILLIS) {
                String methodName = pJoinPoint.getSignature().getName();
                String message = "Время исполнения метода '" + methodName + "' превысило лимит: " + executionTime + "ms";

                kafkaTemplate.send(TOPIC_NAME, "METRICS", message);

                log.info("Время работы метода: {} превысило лимит: {} ms и было отправлено в топик t1_demo_metrics",
                        methodName, MAX_EXECUTION_TIME_MILLIS);
            }
        }
        return result;
    }

}