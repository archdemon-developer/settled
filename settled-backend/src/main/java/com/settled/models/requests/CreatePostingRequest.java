package com.settled.models.requests;

import com.settled.enums.PostingDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostingRequest {

    @NotNull(message = "Account ID is required")
    @Schema(
            description = "The UUID of the account to which this posting applies",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Posting amount", example = "1000.50")
    private BigDecimal amount;

    @NotNull(message = "Direction (DEBIT/CREDIT) is required")
    @Schema(description = "Posting direction", enumAsRef = true, example = "DEBIT")
    private PostingDirection direction;
}
