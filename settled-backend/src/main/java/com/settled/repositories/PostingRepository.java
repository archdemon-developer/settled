package com.settled.repositories;

import com.settled.models.entities.Posting;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRepository extends JpaRepository<Posting, UUID> {
    List<Posting> findByTransactionId(UUID transactionId);

    List<Posting> findByAccountId(UUID accountId);
}
