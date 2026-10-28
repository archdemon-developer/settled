package com.settled.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.settled.configuration.PostgresContainerConfiguration;
import com.settled.enums.AccountType;
import com.settled.enums.PostingDirection;
import com.settled.models.entities.Account;
import com.settled.models.entities.Posting;
import com.settled.models.entities.Transaction;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresContainerConfiguration.class)
public class PostingRepositoryTests {

    @Autowired
    private PostingRepository postingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    private Account saveAccount(String code) {
        return accountRepository.saveAndFlush(Account.builder()
                .code(code)
                .name(code)
                .type(AccountType.CHECKING)
                .build());
    }

    private Transaction saveTransaction(String reference) {
        return transactionRepository.saveAndFlush(Transaction.builder()
                .reference(reference)
                .postedAt(Instant.now())
                .build());
    }

    private void savePosting(Transaction transaction, Account account, String amount, PostingDirection direction) {
        postingRepository.saveAndFlush(Posting.builder()
                .transaction(transaction)
                .account(account)
                .amount(new BigDecimal(amount))
                .direction(direction)
                .build());
    }

    @Test
    public void testFindByTransactionId_returnsOnlyThatTransactionsPostings() {
        Account cash = saveAccount("CASH");
        Account revenue = saveAccount("REV");
        Transaction first = saveTransaction("TXN001");
        Transaction second = saveTransaction("TXN002");
        savePosting(first, cash, "100.50", PostingDirection.DEBIT);
        savePosting(first, revenue, "100.50", PostingDirection.CREDIT);
        savePosting(second, cash, "25.00", PostingDirection.DEBIT);
        entityManager.clear();

        List<Posting> postings = postingRepository.findByTransactionId(first.getId());

        assertEquals(2, postings.size());
        assertEquals(0, new BigDecimal("100.50").compareTo(postings.get(0).getAmount()));
        assertEquals(0, new BigDecimal("100.50").compareTo(postings.get(1).getAmount()));
    }

    @Test
    public void testFindByAccountId_returnsOnlyThatAccountsPostings() {
        Account cash = saveAccount("CASH");
        Account revenue = saveAccount("REV");
        Transaction transaction = saveTransaction("TXN001");
        savePosting(transaction, cash, "50.00", PostingDirection.DEBIT);
        savePosting(transaction, revenue, "50.00", PostingDirection.CREDIT);
        entityManager.clear();

        List<Posting> postings = postingRepository.findByAccountId(cash.getId());

        assertEquals(1, postings.size());
        assertEquals(PostingDirection.DEBIT, postings.getFirst().getDirection());
    }
}
