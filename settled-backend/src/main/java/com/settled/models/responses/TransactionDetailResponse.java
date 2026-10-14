package com.settled.models.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse extends TransactionResponse {
    @Schema(
            description = "All postings added to this transaction so far, in no guaranteed order",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PostingResponse> postings;
}
