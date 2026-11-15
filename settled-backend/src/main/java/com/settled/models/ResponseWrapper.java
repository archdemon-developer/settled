package com.settled.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.settled.constants.DateTimeFormats;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {
    private String status;
    private int code;

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR = "ERROR";

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DateTimeFormats.UTC_TIMESTAMP_PATTERN,
            timezone = DateTimeFormats.UTC)
    private Instant timestamp;

    private String requestId;
    private T data;
    private ErrorDetails error;
    private PaginationInfo paginationInfo;

    public static <T> ResponseWrapper<T> success(int code, T data) {
        return ResponseWrapper.<T>builder()
                .status(SUCCESS_STATUS)
                .code(code)
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .data(data)
                .build();
    }

    public static ResponseWrapper<Void> error(int code, ErrorDetails error) {
        return ResponseWrapper.<Void>builder()
                .status(ERROR)
                .code(code)
                .timestamp(Instant.now())
                .requestId(UUID.randomUUID().toString())
                .error(error)
                .build();
    }
}
