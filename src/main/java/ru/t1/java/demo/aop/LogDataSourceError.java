package ru.t1.java.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
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

    @Pointcut("within(ru.t1.java.demo.*)")
    public void loggingMethods() {

    }

    @After("@annotation(LogException)")
    public void logError(Exception ex) {
        log.info("LoggingDataSourceError отработал после перехвата ошибки: {}", ex.getMessage());
        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                .stackTrace(ex.getStackTrace().toString())
                .message(ex.getMessage())
                .methodSignature(ex.getStackTrace()[0].toString())
                .build();
        dataSourceErrorLogRepository.save(errorLog);
    }
}
