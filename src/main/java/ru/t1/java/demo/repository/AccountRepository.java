package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.Account;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByClientIdAndAccountId(Long clientId, Long accountId);

    Account findByClientId(Long clientId);

    List<Account> findByStatus(Account.AccountStatus accountStatus);
}
