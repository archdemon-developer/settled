package com.settled.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.settled.enums.AccountStatus;
import com.settled.enums.AccountType;
import com.settled.models.entities.Account;
import com.settled.models.responses.AccountResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class MapperUtilsTests {
    @Test
    void shouldMapAccountListToAccountResponseList() {
        List<Account> accounts = List.of(
                Account.builder()
                        .id(UUID.randomUUID())
                        .code("ACC001")
                        .name("Cash")
                        .type(AccountType.SAVINGS)
                        .status(AccountStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .build(),
                Account.builder()
                        .id(UUID.randomUUID())
                        .code("ACC002")
                        .name("Bank")
                        .type(AccountType.CHECKING)
                        .status(AccountStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .build());

        List<AccountResponse> responses = MapperUtil.toAccountResponseList(accounts);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("ACC001", responses.get(0).getCode());
        assertEquals("ACC002", responses.get(1).getCode());
        assertTrue(responses.stream().allMatch(r -> r.getStatus() == AccountStatus.ACTIVE));
    }

    @Test
    void shouldMapAccountToAccountResponse() {
        Instant now = Instant.now();
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .code("ACC001")
                .name("Cash")
                .type(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(null)
                .build();

        AccountResponse response = MapperUtil.toAccountResponse(account);

        assertNotNull(response);
        assertEquals(account.getId(), response.getId());
        assertEquals(account.getCode(), response.getCode());
        assertEquals(account.getName(), response.getName());
        assertEquals(account.getType(), response.getType());
        assertEquals(account.getStatus(), response.getStatus());
        assertEquals(account.getCreatedAt(), response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    void shouldMapNullAccountToNullAccountResponse() {
        AccountResponse response = MapperUtil.toAccountResponse(null);
        assertNull(response);
    }
}
