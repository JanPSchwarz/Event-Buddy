package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record OrganizationCreateDto(

        @Schema(
                description = "Name of the organization",
                example = "EventBuddy GmbH",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        @NotEmpty
        String name,

        @Schema(
                description = "Description of the organization",
                example = "We organize the best events in town.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @Size(min = 4, max = 500, message = "Description must be between 4 and 1500 characters")
        String description,


        @Schema(
                description = "Website URL of the organization",
                example = "https://www.eventbuddy.com",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @URL
        String website

) {
}
