package com.settled.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.settled.configuration.PostgresContainerConfiguration;
import com.settled.enums.AccountType;
import com.settled.models.entities.Account;
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
public class AccountRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void testFindByCode_accountExists() {
        accountRepository.saveAndFlush(Account.builder()
                .code("ACC001")
                .name("Main Checking")
                .type(AccountType.CHECKING)
                .build());

        Optional<Account> found = accountRepository.findByCode("ACC001");

        assertTrue(found.isPresent());
        assertNotNull(found.get().getId());
        assertEquals("ACC001", found.get().getCode());
        assertEquals("Main Checking", found.get().getName());
        assertEquals(AccountType.CHECKING, found.get().getType());
        assertNotNull(found.get().getCreatedAt());
    }

    @Test
    public void testSave_duplicateCode() {
        accountRepository.saveAndFlush(Account.builder()
                .code("ACC001")
                .name("First")
                .type(AccountType.CHECKING)
                .build());

        Account duplicate = Account.builder()
                .code("ACC001")
                .name("Second")
                .type(AccountType.SAVINGS)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> accountRepository.saveAndFlush(duplicate));
    }
}
