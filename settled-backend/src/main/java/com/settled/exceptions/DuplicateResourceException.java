package com.settled.exceptions;

import com.settled.enums.ErrorCode;

public class DuplicateResourceException extends SettledException {
    public DuplicateResourceException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public DuplicateResourceException(ErrorCode errorCode, String details, String field) {
        super(errorCode, details, field);
    }
}
