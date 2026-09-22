package com.settled.repositories;

import com.settled.enums.AccountStatus;
import com.settled.models.entities.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByCode(String code);

    List<Account> findByStatus(AccountStatus status);

    List<Account> findByType(String type);
}
