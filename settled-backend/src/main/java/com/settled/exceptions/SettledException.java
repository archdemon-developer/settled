package com.settled.exceptions;

import com.settled.enums.ErrorCode;
import lombok.Getter;

@Getter
public class SettledException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;
    private final String field;

    public SettledException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
        this.field = null;
    }

    public SettledException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
        this.field = null;
    }

    public SettledException(ErrorCode errorCode, String details, String field) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
        this.field = field;
    }

    public SettledException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details;
        this.field = null;
    }
}
