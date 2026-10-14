package com.settled.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.settled.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(
            description = "Unique transaction identifier",
            example = "7c9e6679-7425-40de-944b-e07fc1f90ae7",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(
            description = "Unique transaction reference",
            example = "TXN-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String reference;

    @Schema(
            description = "Transaction description. Null when none was provided.",
            example = "Payment for services rendered",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(
            description = "Transaction date (UTC), as supplied when the transaction was created",
            example = "2026-10-09T14:35:00.000Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant postedAt;

    @Schema(
            description = "Transaction status. New transactions are DRAFT; posting a balanced transaction sets POSTED.",
            enumAsRef = true,
            example = "DRAFT",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private TransactionStatus status;

    @Schema(
            description = "When the transaction record was created (UTC)",
            example = "2026-10-09T14:36:12.000Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(
            description = "When the transaction record was last modified (UTC)",
            example = "2026-10-09T14:36:12.000Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
}
