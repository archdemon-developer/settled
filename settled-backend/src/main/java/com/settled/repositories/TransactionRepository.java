package com.settled.repositories;

import com.settled.enums.TransactionStatus;
import com.settled.models.entities.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByReference(String reference);

    List<Transaction> findByStatus(TransactionStatus status);
}
