package com.settled.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.settled.enums.ErrorCode;
import com.settled.enums.PostingDirection;
import com.settled.enums.TransactionStatus;
import com.settled.exceptions.InvalidPostingException;
import com.settled.exceptions.ResourceNotFoundException;
import com.settled.models.requests.CreatePostingRequest;
import com.settled.models.requests.CreateTransactionRequest;
import com.settled.models.responses.TransactionDetailResponse;
import com.settled.models.responses.TransactionResponse;
import com.settled.services.LedgerService;
import com.settled.services.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
class TransactionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private LedgerService ledgerService;

    @Test
    void shouldCreateTransaction() throws Exception {
        UUID txnId = UUID.randomUUID();
        Instant now = Instant.now();

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .reference("TXN001")
                .description("Test Transaction")
                .postedAt(now)
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(txnId)
                .reference("TXN001")
                .description("Test Transaction")
                .status(TransactionStatus.DRAFT)
                .postedAt(now)
                .createdAt(now)
                .build();

        when(transactionService.create(any(CreateTransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(txnId.toString()))
                .andExpect(jsonPath("$.data.reference").value("TXN001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(transactionService).create(any(CreateTransactionRequest.class));
    }

    @Test
    void shouldGetTransaction() throws Exception {
        UUID txnId = UUID.randomUUID();
        Instant now = Instant.now();

        TransactionResponse response = TransactionResponse.builder()
                .id(txnId)
                .reference("TXN001")
                .description("Test Transaction")
                .status(TransactionStatus.DRAFT)
                .postedAt(now)
                .createdAt(now)
                .build();

        when(transactionService.getById(txnId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/" + txnId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(txnId.toString()))
                .andExpect(jsonPath("$.data.reference").value("TXN001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(transactionService).getById(txnId);
    }

    @Test
    void shouldListTransactions() throws Exception {
        UUID txnId1 = UUID.randomUUID();
        UUID txnId2 = UUID.randomUUID();
        Instant now = Instant.now();

        List<TransactionResponse> responses = List.of(
                TransactionResponse.builder()
                        .id(txnId1)
                        .reference("TXN001")
                        .status(TransactionStatus.DRAFT)
                        .postedAt(now)
                        .createdAt(now)
                        .build(),
                TransactionResponse.builder()
                        .id(txnId2)
                        .reference("TXN002")
                        .status(TransactionStatus.POSTED)
                        .postedAt(now)
                        .createdAt(now)
                        .build());

        when(transactionService.listByStatus(TransactionStatus.DRAFT)).thenReturn(List.of(responses.get(0)));

        mockMvc.perform(get("/api/v1/transactions?status=DRAFT").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].reference").value("TXN001"))
                .andExpect(jsonPath("$.data[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(transactionService).listByStatus(TransactionStatus.DRAFT);
    }

    @Test
    void shouldMarkAsPosted() throws Exception {
        UUID txnId = UUID.randomUUID();
        Instant now = Instant.now();

        TransactionResponse response = TransactionResponse.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.POSTED)
                .postedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(transactionService.markAsPosted(txnId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions/" + txnId + "/post").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("POSTED"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(transactionService).markAsPosted(txnId);
    }

    @Test
    void shouldAddPosting() throws Exception {
        UUID txnId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();

        CreatePostingRequest request = CreatePostingRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .build();

        TransactionDetailResponse response = TransactionDetailResponse.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.DRAFT)
                .postedAt(now)
                .createdAt(now)
                .postings(List.of())
                .build();

        when(ledgerService.addPosting(txnId, request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions/" + txnId + "/postings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(txnId.toString()))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.requestId").exists());

        verify(ledgerService).addPosting(txnId, request);
    }

    @Test
    void shouldReturn404WhenTransactionNotFound() throws Exception {
        UUID txnId = UUID.randomUUID();

        when(transactionService.getById(txnId))
                .thenThrow(new ResourceNotFoundException(ErrorCode.TRANSACTION_NOT_FOUND, "Transaction not found"));

        mockMvc.perform(get("/api/v1/transactions/" + txnId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.requestId").exists());

        verify(transactionService).getById(txnId);
    }

    @Test
    void shouldReturn409WhenAddingPostingToPostedTransaction() throws Exception {
        UUID txnId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        CreatePostingRequest request = CreatePostingRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .build();

        when(ledgerService.addPosting(txnId, request))
                .thenThrow(new InvalidPostingException(
                        ErrorCode.TRANSACTION_NOT_DRAFT, "Can only add postings to DRAFT transactions"));

        mockMvc.perform(post("/api/v1/transactions/" + txnId + "/postings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.requestId").exists());

        verify(ledgerService).addPosting(txnId, request);
    }

    @Test
    void shouldReturn400WithInvalidRequest() throws Exception {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .reference("") // Blank reference - invalid
                .description("Test")
                .postedAt(Instant.now())
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.requestId").exists());
    }
}
