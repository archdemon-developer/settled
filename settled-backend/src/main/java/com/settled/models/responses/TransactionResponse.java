package com.settled.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.settled.constants.DateTimeFormats;
import com.settled.enums.TransactionStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private String reference;
    private String description;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DateTimeFormats.UTC_TIMESTAMP_PATTERN,
            timezone = DateTimeFormats.UTC)
    private Instant postedAt;

    private TransactionStatus status;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DateTimeFormats.UTC_TIMESTAMP_PATTERN,
            timezone = DateTimeFormats.UTC)
    private Instant createdAt;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DateTimeFormats.UTC_TIMESTAMP_PATTERN,
            timezone = DateTimeFormats.UTC)
    private Instant updatedAt;
}
