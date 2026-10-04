package com.settled.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private BigDecimal amount;
    private PostingDirection direction;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
}
