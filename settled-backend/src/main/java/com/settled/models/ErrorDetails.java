package com.settled.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    @Schema(
            description =
                    "Stable machine-readable error code. Prefixes group them: ACC_ accounts, TXN_ transactions, POST_ postings, VAL_ validation.",
            example = "ACC_001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String errorCode;

    @Schema(
            description = "Short human-readable description of the error type",
            example = "Account not found",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(
            description = "Context about this specific occurrence, when available",
            example = "Account with ID '550e8400-e29b-41d4-a716-446655440000' not found",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String details;

    @Schema(
            description = "Name of the request field the error relates to, when applicable",
            example = "code",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String field;

    @Schema(
            description = "Per-field validation failures. Present only for validation errors (VAL_001).",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<FieldViolation> violations;
}
