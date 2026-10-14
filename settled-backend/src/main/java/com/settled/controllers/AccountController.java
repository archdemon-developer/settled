package com.settled.controllers;

import com.settled.docs.ApiExamples;
import com.settled.enums.AccountStatus;
import com.settled.models.ResponseWrapper;
import com.settled.models.requests.CreateAccountRequest;
import com.settled.models.responses.AccountResponse;
import com.settled.services.AccountService;
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
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Account created",
                                            value = ApiExamples.SUCCESS_201_ACCOUNT_CREATED)))
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
                                        value = ApiExamples.VAL_001_VALIDATION_ERROR),
                                @ExampleObject(
                                        name = "Malformed request body",
                                        value = ApiExamples.VAL_002_MALFORMED_REQUEST_BODY)
                            }))
    @ApiResponse(
            responseCode = "409",
            description = "Duplicate account code",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Duplicate code",
                                            value = ApiExamples.ACC_003_ACCOUNT_ALREADY_EXISTS)))
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

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get account by ID",
            description =
                    "Retrieves a single account by its UUID. To look an account up by its human-readable code instead, use the by-code endpoint.")
    @ApiResponse(
            responseCode = "200",
            description = "Account found",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Account found",
                                            value = ApiExamples.SUCCESS_200_ACCOUNT_FOUND)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad request - the account ID is not a valid UUID",
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
            description = "Account not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Account not found",
                                            value = ApiExamples.ACC_001_ACCOUNT_WITH_ID_NOT_FOUND)))
    public ResponseEntity<ResponseWrapper<AccountResponse>> getAccountById(
            @Parameter(description = "Account UUID") @PathVariable UUID id) {
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
    @Operation(
            summary = "Get account by code",
            description = "Retrieves a single account by its unique code, for example CHK001.")
    @ApiResponse(
            responseCode = "200",
            description = "Account found",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Account found",
                                            value = ApiExamples.SUCCESS_200_ACCOUNT_FOUND)))
    @ApiResponse(
            responseCode = "404",
            description = "Account not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Account not found",
                                            value = ApiExamples.ACC_001_ACCOUNT_WITH_CODE_NOT_FOUND)))
    public ResponseEntity<ResponseWrapper<AccountResponse>> getAccountByCode(
            @Parameter(description = "Account code") @PathVariable String code) {
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
    @Operation(
            summary = "List accounts",
            description =
                    "Lists all accounts, optionally filtered by status (ACTIVE or ARCHIVED). If no status is provided, accounts of every status are returned.")
    @ApiResponse(
            responseCode = "200",
            description = "List of accounts",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Account list",
                                            value = ApiExamples.SUCCESS_200_ACCOUNT_LIST)))
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
    public ResponseEntity<ResponseWrapper<List<AccountResponse>>> listAccountsByStatus(
            @Parameter(
                            description =
                                    "Filter by account status. Optional - omit to return accounts of every status.",
                            schema = @Schema(enumAsRef = true))
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
    @Operation(
            summary = "Archive account",
            description =
                    "Archives an account by setting its status to ARCHIVED. The account is not deleted. Archiving an account that is already archived succeeds with no further effect.")
    @ApiResponse(
            responseCode = "200",
            description = "Account archived",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "Account archived",
                                            value = ApiExamples.SUCCESS_200_ACCOUNT_ARCHIVED)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad request - the account ID is not a valid UUID",
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
            description = "Account not found",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples =
                                    @ExampleObject(
                                            name = "Account not found",
                                            value = ApiExamples.ACC_001_ACCOUNT_WITH_ID_NOT_FOUND)))
    public ResponseEntity<ResponseWrapper<AccountResponse>> archiveAccount(
            @Parameter(description = "Account UUID") @PathVariable UUID id) {
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
