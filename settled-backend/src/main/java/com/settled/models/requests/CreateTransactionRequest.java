package com.settled.models.requests;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotBlank(message = "Transaction reference is required")
    @Size(max = 100, message = "Reference must not exceed 100 characters")
    @Schema(description = "Unique transaction reference", example = "TXN-001")
    private String reference;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Transaction description", example = "Payment for services rendered")
    private String description;

    @NotNull(message = "Posted at timestamp is required")
    @Schema(description = "Transaction date in UTC", example = "2026-10-09T14:35:00.000Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant postedAt;
}
