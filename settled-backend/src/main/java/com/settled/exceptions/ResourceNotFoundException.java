package com.settled.exceptions;

import com.settled.enums.ErrorCode;

public class ResourceNotFoundException extends SettledException {
    public ResourceNotFoundException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String details, String field) {
        super(errorCode, details, field);
    }
}
