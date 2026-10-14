package com.settled.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    ACCOUNT_NOT_FOUND("ACC_001", "Account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_ARCHIVED("ACC_002", "Account is archived", HttpStatus.CONFLICT),
    DUPLICATE_ACCOUNT_CODE("ACC_003", "Account code already exists", HttpStatus.CONFLICT),

    TRANSACTION_NOT_FOUND("TXN_001", "Transaction not found", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_DRAFT("TXN_002", "Transaction is not in DRAFT state", HttpStatus.CONFLICT),
    DUPLICATE_TRANSACTION_REFERENCE("TXN_003", "Transaction reference already exists", HttpStatus.CONFLICT),
    TRANSACTION_HAS_NO_POSTINGS("TXN_004", "Transaction has no postings", HttpStatus.UNPROCESSABLE_CONTENT),
    INVALID_POSTING("POST_001", "Invalid posting: debits must equal credits", HttpStatus.UNPROCESSABLE_CONTENT),

    IDEMPOTENCY_KEY_MISMATCH("IDEM_001", "Idempotency key already used for different transaction", HttpStatus.CONFLICT),

    VALIDATION_ERROR("VAL_001", "Validation failed", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST_BODY("VAL_002", "Request body is malformed or contains an invalid value", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER("VAL_003", "Request parameter has an invalid value", HttpStatus.BAD_REQUEST);
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
