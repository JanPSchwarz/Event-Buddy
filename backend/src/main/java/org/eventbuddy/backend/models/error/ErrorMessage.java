package org.eventbuddy.backend.models.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorMessage(
        @Schema(
                description = "The timestamp when the error occurred",
                example = "2024-06-15T12:34:56.789Z",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        String timeStamp,

        @Schema(
                description = "Description of the error",
                example = "Resource not found",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        String error,
        @Schema(
                description = "Unique identifier for the error instance",
                example = "err_1234567890",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        String id,
        @Schema(
                description = "HTTP status code associated with the error",
                example = "404",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        int status ) {
}
