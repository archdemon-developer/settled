package com.settled.exceptions;

import com.settled.enums.ErrorCode;

public class InvalidPostingException extends SettledException {
    public InvalidPostingException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public InvalidPostingException(ErrorCode errorCode, String details, String field) {
        super(errorCode, details, field);
    }
}
