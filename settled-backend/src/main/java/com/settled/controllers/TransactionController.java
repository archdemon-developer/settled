package com.settled.controllers;

import com.settled.enums.TransactionStatus;
import com.settled.models.ApiResponse;
import com.settled.models.requests.CreatePostingRequest;
import com.settled.models.requests.CreateTransactionRequest;
import com.settled.models.responses.TransactionDetailResponse;
import com.settled.models.responses.TransactionResponse;
import com.settled.services.LedgerService;
import com.settled.services.TransactionService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse data = transactionService.create(request);

        ApiResponse<TransactionResponse> response = ApiResponse.<TransactionResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable UUID id) {
        TransactionResponse data = transactionService.getById(id);

        ApiResponse<TransactionResponse> response = ApiResponse.<TransactionResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listTransactions(
            @RequestParam(required = false) TransactionStatus status) {
        List<TransactionResponse> data = transactionService.listByStatus(status);

        ApiResponse<List<TransactionResponse>> response = ApiResponse.<List<TransactionResponse>>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<TransactionResponse>> markAsPosted(@PathVariable UUID id) {
        TransactionResponse data = transactionService.markAsPosted(id);

        ApiResponse<TransactionResponse> response = ApiResponse.<TransactionResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/postings")
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> addPosting(
            @PathVariable UUID id, @Valid @RequestBody CreatePostingRequest request) {
        TransactionDetailResponse data = ledgerService.addPosting(id, request);

        ApiResponse<TransactionDetailResponse> response = ApiResponse.<TransactionDetailResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
