package ru.t1.java.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.aop.MetricAspect;

@SpringBootApplication
public class T1AspectsApplication {

    @Bean
    public MetricAspect metricAspect() {
        return new MetricAspect();
    }

    @Bean
    public LogDataSourceError logDataSourceError() {
        return new LogDataSourceError();
    }

    public static void main(String[] args) {
        SpringApplication.run(T1AspectsApplication.class, args);
    }
}

