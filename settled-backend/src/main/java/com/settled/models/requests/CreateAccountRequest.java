package com.settled.models.requests;

import com.settled.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @NotBlank(message = "Code is required")
    @Schema(description = "Unique account code", example = "CHK001")
    @Size(min = 1, max = 50, message = "Code must be between 1 and 50 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Schema(description = "Human-readable account name", example = "Main Checking")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotNull(message = "Type is required")
    @Schema(description = "Account type", enumAsRef = true, example = "CHECKING")
    private AccountType type;
}
