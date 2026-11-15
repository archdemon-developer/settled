package com.settled.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.settled.constants.DateTimeFormats;
import com.settled.enums.AccountStatus;
import com.settled.enums.AccountType;
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
public class AccountResponse {
    private UUID id;
    private String code;
    private String name;
    private AccountType type;
    private AccountStatus status;

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
