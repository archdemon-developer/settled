package com.settled.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.settled.enums.AccountStatus;
import com.settled.enums.AccountType;
import com.settled.exceptions.DuplicateResourceException;
import com.settled.exceptions.ResourceNotFoundException;
import com.settled.models.entities.Account;
import com.settled.models.requests.CreateAccountRequest;
import com.settled.models.responses.AccountResponse;
import com.settled.repositories.AccountRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    public void testCreateAccount_duplicateCode() {
        Account account = Account.builder()
                .code("ACC123")
                .name("Test Account")
                .type(AccountType.SAVINGS)
                .build();

        CreateAccountRequest request = CreateAccountRequest.builder()
                .code("ACC123")
                .name("Test Account")
                .type(AccountType.SAVINGS)
                .build();

        when(accountRepository.findByCode("ACC123")).thenReturn(Optional.of(account));
        assertThrows(DuplicateResourceException.class, () -> accountService.create(request));
        verify(accountRepository, times(1)).findByCode("ACC123");
    }

    @Test
    public void testCreateAccount_accountCreated() {
        UUID accountId = UUID.randomUUID();

        CreateAccountRequest request = CreateAccountRequest.builder()
                .code("ACC123")
                .name("Test Account")
                .type(AccountType.SAVINGS)
                .build();

        when(accountRepository.findByCode("ACC123")).thenReturn(Optional.empty());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        Account savedAccount = Account.builder()
                .id(accountId)
                .code("ACC123")
                .name("Test Account")
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        when(accountRepository.save(captor.capture())).thenReturn(savedAccount);

        AccountResponse accountResponse = accountService.create(request);

        Account captured = captor.getValue();
        assertEquals("ACC123", captured.getCode());
        assertEquals("Test Account", captured.getName());
        assertEquals(AccountType.SAVINGS, captured.getType());
        assertEquals(AccountStatus.ACTIVE, captured.getStatus());

        assertEquals(request.getCode(), accountResponse.getCode());
        assertEquals(request.getName(), accountResponse.getName());
        assertEquals(request.getType(), accountResponse.getType());
        verify(accountRepository, times(1)).findByCode("ACC123");
        verify(accountRepository, times(1)).save(captor.getValue());
    }

    @Test
    public void testGetById_NoAccounts() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getById(id));
    }

    @Test
    public void testGetById_accountExists() {
        UUID id = UUID.randomUUID();

        Account account = Account.builder()
                .id(id)
                .code("CODE123")
                .name("ACC123")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS)
                .build();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        AccountResponse accountResponse = accountService.getById(id);

        assertNotNull(accountResponse);

        assertEquals(account.getCode(), accountResponse.getCode());
        assertEquals(account.getId(), accountResponse.getId());
        assertEquals(account.getStatus(), accountResponse.getStatus());
        assertEquals(account.getName(), accountResponse.getName());
        assertEquals(account.getType(), accountResponse.getType());
        verify(accountRepository, times(1)).findById(id);
    }

    @Test
    public void testGetByCode_NoAccounts() {
        when(accountRepository.findByCode("CODE123")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getByCode("CODE123"));
        verify(accountRepository, times(1)).findByCode("CODE123");
    }

    @Test
    public void testGetByCode_accountExists() {

        Account account = Account.builder()
                .id(UUID.randomUUID())
                .code("CODE123")
                .name("ACC123")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS)
                .build();

        when(accountRepository.findByCode("CODE123")).thenReturn(Optional.of(account));

        AccountResponse accountResponse = accountService.getByCode("CODE123");

        assertNotNull(accountResponse);

        assertEquals(account.getCode(), accountResponse.getCode());
        assertEquals(account.getId(), accountResponse.getId());
        assertEquals(account.getStatus(), accountResponse.getStatus());
        assertEquals(account.getName(), accountResponse.getName());
        assertEquals(account.getType(), accountResponse.getType());
        verify(accountRepository, times(1)).findByCode("CODE123");
    }

    @Test
    public void testGetAccountsList_emptyStatus() {
        List<Account> accounts = List.of(
                Account.builder()
                        .id(UUID.randomUUID())
                        .code("CODE123")
                        .name("ACC123")
                        .status(AccountStatus.ACTIVE)
                        .type(AccountType.SAVINGS)
                        .build(),
                Account.builder()
                        .id(UUID.randomUUID())
                        .code("CODE456")
                        .name("ACC456")
                        .status(AccountStatus.ARCHIVED)
                        .type(AccountType.CHECKING)
                        .build());

        when(accountRepository.findAll()).thenReturn(accounts);
        List<AccountResponse> accountResponses = accountService.listByStatus(null);

        assertNotNull(accountResponses);
        assertEquals(accounts.size(), accountResponses.size());
        assertEquals(accounts.get(0).getCode(), accountResponses.get(0).getCode());
        assertEquals(accounts.get(1).getCode(), accountResponses.get(1).getCode());
        assertEquals(accounts.get(0).getStatus(), accountResponses.get(0).getStatus());
        assertEquals(accounts.get(1).getStatus(), accountResponses.get(1).getStatus());
        assertEquals(accounts.get(0).getName(), accountResponses.get(0).getName());
        assertEquals(accounts.get(1).getName(), accountResponses.get(1).getName());
        assertEquals(accounts.get(0).getType(), accountResponses.get(0).getType());
        assertEquals(accounts.get(1).getType(), accountResponses.get(1).getType());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    public void testGetAccountsList_withStatus() {
        List<Account> accounts = List.of(
                Account.builder()
                        .id(UUID.randomUUID())
                        .code("CODE123")
                        .name("ACC123")
                        .status(AccountStatus.ACTIVE)
                        .type(AccountType.SAVINGS)
                        .build(),
                Account.builder()
                        .id(UUID.randomUUID())
                        .code("CODE456")
                        .name("ACC456")
                        .status(AccountStatus.ACTIVE)
                        .type(AccountType.CHECKING)
                        .build());

        when(accountRepository.findByStatus(AccountStatus.ACTIVE)).thenReturn(accounts);
        List<AccountResponse> accountResponses = accountService.listByStatus(AccountStatus.ACTIVE);

        assertNotNull(accountResponses);
        assertEquals(accounts.size(), accountResponses.size());
        assertEquals(accounts.get(0).getCode(), accountResponses.get(0).getCode());
        assertEquals(accounts.get(0).getStatus(), accountResponses.get(0).getStatus());
        assertEquals(accounts.get(0).getName(), accountResponses.get(0).getName());
        assertEquals(accounts.get(0).getType(), accountResponses.get(0).getType());
        assertEquals(accounts.get(1).getCode(), accountResponses.get(1).getCode());
        assertEquals(accounts.get(1).getStatus(), accountResponses.get(1).getStatus());
        assertEquals(accounts.get(1).getName(), accountResponses.get(1).getName());
        assertEquals(accounts.get(1).getType(), accountResponses.get(1).getType());
        verify(accountRepository, times(1)).findByStatus(AccountStatus.ACTIVE);
    }

    @Test
    public void testArchiveAccount_accountExists() {
        UUID id = UUID.randomUUID();

        Account account = Account.builder()
                .id(id)
                .code("CODE123")
                .name("ACC123")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS)
                .createdAt(Instant.now())
                .build();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        Account archivedAccount = Account.builder()
                .id(id)
                .code("CODE123")
                .name("ACC123")
                .status(AccountStatus.ARCHIVED)
                .type(AccountType.SAVINGS)
                .createdAt(account.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(accountRepository.save(captor.capture())).thenReturn(archivedAccount);

        AccountResponse accountResponse = accountService.archive(id);

        Account captured = captor.getValue();
        assertEquals(AccountStatus.ARCHIVED, captured.getStatus());

        assertNotNull(accountResponse);
        assertEquals(AccountStatus.ARCHIVED, accountResponse.getStatus());

        verify(accountRepository, times(1)).findById(id);
        verify(accountRepository, times(1)).save(captor.getValue());
    }

    @Test
    public void testArchiveAccount_accountNotFound() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.archive(id));
        verify(accountRepository, times(1)).findById(id);
    }
}
