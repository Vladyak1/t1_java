package ru.t1.java.demo.service;

import ru.t1.java.demo.model.UnblockResponse;

public interface UnblockService {
    UnblockResponse unblockClient(Long id);

    UnblockResponse unblockAccount(Long id);
}
