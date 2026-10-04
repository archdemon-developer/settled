package com.settled.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.settled.enums.AccountStatus;
import com.settled.models.ResponseWrapper;
import com.settled.models.requests.CreateAccountRequest;
import com.settled.models.responses.AccountResponse;
import com.settled.services.AccountService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "APIs for managing ledger accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create account",
            description = "Creates a new account with a unique code. Accounts are the foundation of the ledger, representing entities like assets, liabilities, equity, revenue, or expenses.")
    @ApiResponse(responseCode = "201", description = "Account created successfully",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request - missing or invalid fields")
    @ApiResponse(responseCode = "409", description = "Conflict - duplicate account code")
    public ResponseEntity<ResponseWrapper<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse data = accountService.create(request);

        ResponseWrapper<AccountResponse> response = ResponseWrapper.<AccountResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.CREATED.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID",
            description = "Retrieves an account by its UUID. Returns the account details including type, status, and creation timestamp.")
    @ApiResponse(responseCode = "200", description = "Account found and returned",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<ResponseWrapper<AccountResponse>> getAccountById(
            @Parameter(description = "Account UUID", required = true)
            @PathVariable UUID id) {
        AccountResponse data = accountService.getById(id);

        ResponseWrapper<AccountResponse> response = ResponseWrapper.<AccountResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get account by code",
            description = "Retrieves an account by its unique code. The code is a human-readable identifier for the account.")
    @ApiResponse(responseCode = "200", description = "Account found and returned",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<ResponseWrapper<AccountResponse>> getAccountByCode(
            @Parameter(description = "Account code (e.g., ACC001, CASH, SALES)", required = true)
            @PathVariable String code) {
        AccountResponse data = accountService.getByCode(code);

        ResponseWrapper<AccountResponse> response = ResponseWrapper.<AccountResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "List accounts",
            description = "Lists all accounts, optionally filtered by status (ACTIVE or ARCHIVED). If no status is provided, all accounts are returned.")
    @ApiResponse(responseCode = "200", description = "List of accounts",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    public ResponseEntity<ResponseWrapper<List<AccountResponse>>> listAccountsByStatus(
            @Parameter(description = "Filter by account status (ACTIVE, ARCHIVED). Optional.", required = false)
            @RequestParam(required = false) AccountStatus status) {
        List<AccountResponse> data = accountService.listByStatus(status);

        ResponseWrapper<List<AccountResponse>> response = ResponseWrapper.<List<AccountResponse>>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}/archive")
    @Operation(summary = "Archive account",
            description = "Marks an account as ARCHIVED. Once archived, the account cannot be used in new transactions but historical data is preserved.")
    @ApiResponse(responseCode = "200", description = "Account archived successfully",
            content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<ResponseWrapper<AccountResponse>> archiveAccount(
            @Parameter(description = "Account UUID", required = true)
            @PathVariable UUID id) {
        AccountResponse data = accountService.archive(id);

        ResponseWrapper<AccountResponse> response = ResponseWrapper.<AccountResponse>builder()
                .status("SUCCESS")
                .code(HttpStatus.OK.value())
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}