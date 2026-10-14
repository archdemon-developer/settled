package com.settled.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapper<T> {
    @Schema(
            description = "Outcome of the request",
            allowableValues = {"SUCCESS", "ERROR"},
            example = "SUCCESS",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Schema(
            description = "HTTP status code of the response, repeated in the body",
            example = "200",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private int code;

    @Schema(
            description = "When the response was generated (UTC)",
            example = "2026-10-09T14:35:00.000Z",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    @Schema(
            description = "Identifier of the request that produced this response",
            example = "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestId;

    @Schema(
            description = "Response payload. Present only when status is SUCCESS.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private T data;

    @Schema(
            description = "Error details. Present only when status is ERROR.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ErrorDetails error;

    @Schema(hidden = true)
    private PaginationInfo paginationInfo;
}
