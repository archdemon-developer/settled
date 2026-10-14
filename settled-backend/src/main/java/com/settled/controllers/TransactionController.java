package com.settled.controllers;

import com.settled.docs.ApiExamples;
import com.settled.enums.TransactionStatus;
import com.settled.models.ResponseWrapper;
import com.settled.models.requests.CreatePostingRequest;
import com.settled.models.requests.CreateTransactionRequest;
import com.settled.models.responses.TransactionDetailResponse;
import com.settled.models.responses.TransactionResponse;
import com.settled.services.LedgerService;
import com.settled.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(
            summary = "Create transaction",
            description =
                    "Creates a new transaction in DRAFT state. The transaction is created with a unique reference and waits for postings to be added before being posted.")
    @ApiResponse(
            responseCode = "201",
            description = "Transaction created successfully",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Transaction created",
                                            value = ApiExamples.SUCCESS_201_TRANSACTION_CREATED)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or the request body is malformed",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = {
                                @ExampleObject(
                                        name = "Validation failed",
                                        value = ApiExamples.VAL_001_CREATE_TRANSACTION_VALIDATION_ERROR),
                                @ExampleObject(
                                        name = "Malformed request body",
                                        value = ApiExamples.VAL_002_MALFORMED_REQUEST_BODY)
                            }))
    @ApiResponse(
            responseCode = "409",
            description = "Conflict - duplicate transaction reference",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Duplicate reference",
                                            value = ApiExamples.TXN_003_DUPLICATE_REFERENCE)))
    public ResponseEntity<ResponseWrapper<TransactionResponse>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
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
    @Operation(
            summary = "Get transaction by ID",
            description =
                    "Retrieves a transaction by its UUID. Returns the transaction details along with any postings that have been added.")
    @ApiResponse(
            responseCode = "200",
            description = "Transaction found and returned",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Transaction found",
                                            value = ApiExamples.SUCCESS_200_TRANSACTION_FOUND)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad request - the transaction ID is not a valid UUID",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Invalid path parameter",
                                            value = ApiExamples.VAL_003_INVALID_PATH_PARAMETER)))
    @ApiResponse(
            responseCode = "404",
            description = "Transaction not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Transaction not found",
                                            value = ApiExamples.TXN_001_TRANSACTION_NOT_FOUND)))
    public ResponseEntity<ResponseWrapper<TransactionResponse>> getTransaction(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id) {
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
    @Operation(
            summary = "List transactions",
            description =
                    "Lists all transactions, optionally filtered by status (DRAFT, POSTED or REVERSED). If no status is provided, all transactions are returned.")
    @ApiResponse(
            responseCode = "200",
            description = "List of transactions",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Transaction list",
                                            value = ApiExamples.SUCCESS_200_TRANSACTION_LIST)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad request - the status filter is not a valid value",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Invalid status parameter",
                                            value = ApiExamples.VAL_003_INVALID_STATUS_PARAMETER)))
    public ResponseEntity<ResponseWrapper<List<TransactionResponse>>> listTransactions(
            @Parameter(
                            description = "Filter by transaction status (DRAFT, POSTED, REVERSED). Optional.",
                            schema = @Schema(enumAsRef = true))
                    @RequestParam(required = false)
                    TransactionStatus status) {
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
    @Operation(
            summary = "Mark transaction as posted",
            description =
                    "Marks a DRAFT transaction as POSTED. The transaction must have at least 2 postings (1 debit + 1 credit) that balance (total debits = total credits). Once posted, the transaction is immutable.")
    @ApiResponse(
            responseCode = "200",
            description = "Transaction marked as posted",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Transaction posted",
                                            value = ApiExamples.SUCCESS_200_TRANSACTION_POSTED)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad request - the transaction ID is not a valid UUID",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Invalid path parameter",
                                            value = ApiExamples.VAL_003_INVALID_PATH_PARAMETER)))
    @ApiResponse(
            responseCode = "404",
            description = "Transaction not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Transaction not found",
                                            value = ApiExamples.TXN_001_TRANSACTION_NOT_FOUND)))
    @ApiResponse(
            responseCode = "409",
            description = "Conflict - transaction is not in DRAFT state",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Transaction not in DRAFT",
                                            value = ApiExamples.TXN_002_NOT_DRAFT_ON_POST)))
    @ApiResponse(
            responseCode = "422",
            description = "Unprocessable - transaction has no postings or its postings do not balance",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = {
                                @ExampleObject(name = "No postings", value = ApiExamples.TXN_004_NO_POSTINGS),
                                @ExampleObject(
                                        name = "Postings do not balance",
                                        value = ApiExamples.POST_001_POSTINGS_DO_NOT_BALANCE)
                            }))
    public ResponseEntity<ResponseWrapper<TransactionResponse>> markAsPosted(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id) {
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

    @PostMapping(
            value = "/{id}/postings",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Add posting to transaction",
            description =
                    "Adds a posting (debit or credit) to a DRAFT transaction. A posting represents a single ledger entry to an account. Multiple postings can be added to a transaction before it is posted.")
    @ApiResponse(
            responseCode = "201",
            description = "Posting added successfully. Returns the full transaction with all postings.",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Posting added",
                                            value = ApiExamples.SUCCESS_201_POSTING_ADDED)))
    @ApiResponse(
            responseCode = "400",
            description =
                    "Bad request - validation failed, the request body is malformed, or the transaction ID is not a valid UUID",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = {
                                @ExampleObject(
                                        name = "Validation failed",
                                        value = ApiExamples.VAL_001_ADD_POSTING_VALIDATION_ERROR),
                                @ExampleObject(
                                        name = "Malformed request body",
                                        value = ApiExamples.VAL_002_MALFORMED_REQUEST_BODY),
                                @ExampleObject(
                                        name = "Invalid path parameter",
                                        value = ApiExamples.VAL_003_INVALID_PATH_PARAMETER)
                            }))
    @ApiResponse(
            responseCode = "404",
            description = "Transaction or account not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = {
                                @ExampleObject(
                                        name = "Transaction not found",
                                        value = ApiExamples.TXN_001_TRANSACTION_NOT_FOUND),
                                @ExampleObject(
                                        name = "Account not found",
                                        value = ApiExamples.ACC_001_ACCOUNT_WITH_ID_NOT_FOUND)
                            }))
    @ApiResponse(
            responseCode = "409",
            description = "Conflict - transaction is not in DRAFT state",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Transaction not in DRAFT",
                                            value = ApiExamples.TXN_002_NOT_DRAFT_ON_ADD_POSTING)))
    public ResponseEntity<ResponseWrapper<TransactionDetailResponse>> addPosting(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id,
            @Valid @RequestBody CreatePostingRequest request) {
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
