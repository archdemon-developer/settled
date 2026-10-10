package com.settled.controllers;

import com.settled.enums.AccountStatus;
import com.settled.models.ResponseWrapper;
import com.settled.models.requests.CreateAccountRequest;
import com.settled.models.responses.AccountResponse;
import com.settled.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create account",
            description = "Creates a new account with a unique code. Accounts are the foundation of the ledger.")
    @ApiResponse(
            responseCode = "201",
            description = "Account created successfully",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseWrapper.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Duplicate account code")
    public ResponseEntity<ResponseWrapper<AccountResponse>> createAccount(
            @Valid
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Account creation details",
                            required = true,
                            content =
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateAccountRequest.class)))
                    @RequestBody
                    CreateAccountRequest request) {
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

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get account by ID")
    @ApiResponse(
            responseCode = "200",
            description = "Account found",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<ResponseWrapper<AccountResponse>> getAccountById(
            @Parameter(description = "Account UUID", required = true) @PathVariable UUID id) {
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

    @GetMapping(value = "/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get account by code")
    @ApiResponse(
            responseCode = "200",
            description = "Account found",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<ResponseWrapper<AccountResponse>> getAccountByCode(
            @Parameter(description = "Account code", required = true) @PathVariable String code) {
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List accounts")
    @ApiResponse(
            responseCode = "200",
            description = "List of accounts",
            content = @Content(mediaType = "application/json"))
    public ResponseEntity<ResponseWrapper<List<AccountResponse>>> listAccountsByStatus(
            @Parameter(description = "Filter by account status", schema = @Schema(enumAsRef = true))
                    @RequestParam(required = false)
                    AccountStatus status) {
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

    @PutMapping(value = "/{id}/archive", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Archive account")
    @ApiResponse(
            responseCode = "200",
            description = "Account archived",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<ResponseWrapper<AccountResponse>> archiveAccount(
            @Parameter(description = "Account UUID", required = true) @PathVariable UUID id) {
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
