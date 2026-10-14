package com.settled.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.settled.enums.PostingDirection;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(
            description = "Unique posting identifier",
            example = "3b241101-e2bb-4255-8caf-4136c566a962",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(
            description = "The UUID of the account this posting applies to",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID accountId;

    @Schema(
            description = "Posting amount, always greater than zero. Stored with up to 4 decimal places.",
            example = "1000.50",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(
            description = "Whether this posting debits or credits the account",
            enumAsRef = true,
            example = "DEBIT",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private PostingDirection direction;

    @Schema(
            description = "When the posting was created (UTC)",
            example = "2026-10-09T14:37:05.000Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
}
