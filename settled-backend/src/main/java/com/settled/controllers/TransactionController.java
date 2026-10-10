package com.settled.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.settled.enums.TransactionStatus;
import com.settled.models.ResponseWrapper;
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
import org.springframework.http.MediaType;
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
@Tag(name = "Transactions", description = "APIs for managing double-entry ledger transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final LedgerService ledgerService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create transaction",
            description = "Creates a new transaction in DRAFT state. The transaction is created with a unique reference and waits for postings to be added before being posted.")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request - missing or invalid fields")
    @ApiResponse(responseCode = "409", description = "Conflict - duplicate transaction reference")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transaction details with reference and posted date",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateTransactionRequest.class)))
            @Valid @RequestBody
            CreateTransactionRequest request) {
        TransactionResponse data = transactionService.create(request);

        ResponseWrapper<TransactionResponse> response = ResponseWrapper.<TransactionResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get transaction by ID",
            description = "Retrieves a transaction by its UUID. Returns the transaction details along with any postings that have been added.")
    @ApiResponse(responseCode = "200", description = "Transaction found and returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> getTransaction(
            @Parameter(description = "Transaction UUID", required = true)
            @PathVariable UUID id) {
        TransactionResponse data = transactionService.getById(id);

        ResponseWrapper<TransactionResponse> response = ResponseWrapper.<TransactionResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List transactions",
            description = "Lists all transactions, optionally filtered by status (DRAFT or POSTED). If no status is provided, all transactions are returned.")
    @ApiResponse(responseCode = "200", description = "List of transactions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseWrapper.class)))
    public ResponseEntity<ResponseWrapper<List<TransactionResponse>>> listTransactions(
            @Parameter(description = "Filter by transaction status (DRAFT, POSTED). Optional.", schema = @Schema(enumAsRef = true))
            @RequestParam(required = false) TransactionStatus status) {
        List<TransactionResponse> data = transactionService.listByStatus(status);

        ResponseWrapper<List<TransactionResponse>> response = ResponseWrapper.<List<TransactionResponse>>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/post", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mark transaction as posted",
            description = "Marks a DRAFT transaction as POSTED. The transaction must have at least 2 postings (1 debit + 1 credit) that balance (total debits = total credits). Once posted, the transaction is immutable.")
    @ApiResponse(responseCode = "200", description = "Transaction marked as posted",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @ApiResponse(responseCode = "409", description = "Transaction is not in DRAFT state or postings do not balance")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> markAsPosted(
            @Parameter(description = "Transaction UUID", required = true)
            @PathVariable UUID id) {
        TransactionResponse data = transactionService.markAsPosted(id);

        ResponseWrapper<TransactionResponse> response = ResponseWrapper.<TransactionResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/postings", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add posting to transaction",
            description = "Adds a posting (debit or credit) to a DRAFT transaction. A posting represents a single ledger entry to an account. Multiple postings can be added to a transaction before it is posted.")
    @ApiResponse(responseCode = "201", description = "Posting added successfully. Returns the full transaction with all postings.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "400", description = "Invalid posting request - invalid amount or missing fields")
    @ApiResponse(responseCode = "404", description = "Transaction or account not found")
    @ApiResponse(responseCode = "409", description = "Transaction is not in DRAFT state")
    public ResponseEntity<ResponseWrapper<TransactionDetailResponse>> addPosting(
            @Parameter(description = "Transaction UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Posting details (account, amount, and direction)",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreatePostingRequest.class)))
            @Valid @RequestBody
            CreatePostingRequest request) {
        TransactionDetailResponse data = ledgerService.addPosting(id, request);

        ResponseWrapper<TransactionDetailResponse> response = ResponseWrapper.<TransactionDetailResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}