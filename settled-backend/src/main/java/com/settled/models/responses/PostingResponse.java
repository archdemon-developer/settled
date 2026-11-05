package com.settled.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.settled.constants.DateTimeFormats;
import com.settled.enums.PostingDirection;
import java.math.BigDecimal;
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
public class PostingResponse {
    private UUID id;
    private UUID accountId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;

    private PostingDirection direction;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = DateTimeFormats.UTC_TIMESTAMP_PATTERN,
            timezone = DateTimeFormats.UTC)
    private Instant createdAt;
}
