package com.settled.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.settled.enums.AccountStatus;
import com.settled.enums.ErrorCode;
import com.settled.exceptions.DuplicateResourceException;
import com.settled.models.requests.CreateAccountRequest;
import com.settled.models.responses.AccountResponse;
import com.settled.services.AccountService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {

        AccountResponse accountResponse = AccountResponse.builder()
                .id(UUID.randomUUID())
                .code("ACC001")
                .name("Cash")
                .type("ASSET")
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();

        when(accountService.create(any(CreateAccountRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "code": "ACC001",
                        "name": "Cash",
                        "type": "ASSET"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.code").value("ACC001"))
                .andExpect(jsonPath("$.data.name").value("Cash"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(accountService).create(any(CreateAccountRequest.class));
    }

    @Test
    void shouldGetAccountById() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountResponse accountResponse = AccountResponse.builder()
                .id(accountId)
                .code("ACC001")
                .name("Cash")
                .type("ASSET")
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();

        when(accountService.getById(accountId)).thenReturn(accountResponse);

        mockMvc.perform(get("/api/v1/accounts/" + accountId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value("ACC001"))
                .andExpect(jsonPath("$.data.name").value("Cash"))
                .andExpect(jsonPath("$.data.type").value("ASSET"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(accountService).getById(accountId);
    }

    @Test
    void shouldGetAccountByCode() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountResponse accountResponse = AccountResponse.builder()
                .id(accountId)
                .code("ACC001")
                .name("Cash")
                .type("ASSET")
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();

        when(accountService.getByCode("ACC001")).thenReturn(accountResponse);

        mockMvc.perform(get("/api/v1/accounts/code/ACC001").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value("ACC001"))
                .andExpect(jsonPath("$.data.name").value("Cash"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(accountService).getByCode("ACC001");
    }

    @Test
    void shouldListAllAccounts() throws Exception {
        List<AccountResponse> accounts = List.of(
                AccountResponse.builder()
                        .id(UUID.randomUUID())
                        .code("ACC001")
                        .name("Cash")
                        .type("ASSET")
                        .status(AccountStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .build(),
                AccountResponse.builder()
                        .id(UUID.randomUUID())
                        .code("ACC002")
                        .name("Bank")
                        .type("ASSET")
                        .status(AccountStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .build());

        when(accountService.listByStatus(null)).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("ACC001"))
                .andExpect(jsonPath("$.data[1].code").value("ACC002"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(accountService).listByStatus(null);
    }

    @Test
    void shouldListAccountsByStatus() throws Exception {
        List<AccountResponse> accounts = List.of(AccountResponse.builder()
                .id(UUID.randomUUID())
                .code("ACC001")
                .name("Cash")
                .type("ASSET")
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        when(accountService.listByStatus(AccountStatus.ACTIVE)).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/accounts?status=ACTIVE").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].code").value("ACC001"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(accountService).listByStatus(AccountStatus.ACTIVE);
    }

    @Test
    void shouldArchiveAccount() throws Exception {

        UUID accountId = UUID.randomUUID();
        AccountResponse archivedAccount = AccountResponse.builder()
                .id(accountId)
                .code("ACC001")
                .name("Cash")
                .type("ASSET")
                .status(AccountStatus.ARCHIVED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(accountService.archive(accountId)).thenReturn(archivedAccount);

        mockMvc.perform(put("/api/v1/accounts/" + accountId + "/archive").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value("ACC001"))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andExpect(jsonPath("$.requestId").exists());

        verify(accountService).archive(accountId);
    }

    @Test
    void shouldReturn409WhenCreatingAccountWithDuplicateCode() throws Exception {
        DuplicateResourceException ex = new DuplicateResourceException(
                ErrorCode.DUPLICATE_ACCOUNT_CODE, "Code 'ACC001' already exists", "code");

        when(accountService.create(any(CreateAccountRequest.class))).thenThrow(ex);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "code": "ACC001",
                    "name": "Cash",
                    "type": "ASSET"
                }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.DUPLICATE_ACCOUNT_CODE.getCode()))
                .andExpect(jsonPath("$.error.details").value("Code 'ACC001' already exists"))
                .andExpect(jsonPath("$.error.field").value("code"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(accountService).create(any(CreateAccountRequest.class));
    }
}
