package com.settled.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.settled.configuration.PostgresContainerConfiguration;
import com.settled.enums.TransactionStatus;
import com.settled.models.entities.Transaction;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresContainerConfiguration.class)
public class TransactionRepositoryTests {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testFindByReference_roundTripsFromDatabase() {
        Instant postedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        transactionRepository.saveAndFlush(Transaction.builder()
                .reference("TXN001")
                .description("Salary")
                .postedAt(postedAt)
                .build());
        entityManager.clear();

        Optional<Transaction> found = transactionRepository.findByReference("TXN001");

        assertTrue(found.isPresent());
        assertEquals("Salary", found.get().getDescription());
        assertEquals(postedAt, found.get().getPostedAt());
        assertEquals(TransactionStatus.DRAFT, found.get().getStatus());
    }

    @Test
    public void testFindByStatus_returnsOnlyMatching() {
        transactionRepository.saveAndFlush(Transaction.builder()
                .reference("TXN001")
                .postedAt(Instant.now())
                .build());
        transactionRepository.saveAndFlush(Transaction.builder()
                .reference("TXN002")
                .postedAt(Instant.now())
                .build());
        transactionRepository.saveAndFlush(Transaction.builder()
                .reference("TXN003")
                .postedAt(Instant.now())
                .status(TransactionStatus.POSTED)
                .build());

        List<Transaction> drafts = transactionRepository.findByStatus(TransactionStatus.DRAFT);

        assertEquals(2, drafts.size());
    }

    @Test
    public void testSave_duplicateReference() {
        transactionRepository.saveAndFlush(Transaction.builder()
                .reference("TXN001")
                .postedAt(Instant.now())
                .build());

        Transaction duplicate = Transaction.builder()
                .reference("TXN001")
                .postedAt(Instant.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> transactionRepository.saveAndFlush(duplicate));
    }
}
