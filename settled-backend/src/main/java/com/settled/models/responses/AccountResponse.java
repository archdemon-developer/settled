package com.settled.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.settled.enums.AccountStatus;
import com.settled.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(
            description = "Unique account identifier",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Unique account code", example = "CHK001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(
            description = "Human-readable account name",
            example = "Main Checking",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(
            description = "Account type",
            enumAsRef = true,
            example = "CHECKING",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountType type;

    @Schema(
            description = "Account status. New accounts are ACTIVE; archiving sets ARCHIVED.",
            enumAsRef = true,
            example = "ACTIVE",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountStatus status;

    @Schema(
            description = "When the account was created (UTC)",
            example = "2026-10-09T14:35:00.000Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(
            description = "When the account was last modified (UTC)",
            example = "2026-10-09T14:35:00.000Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
}
