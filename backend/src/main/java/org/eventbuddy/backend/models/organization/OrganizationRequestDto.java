package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

@Builder
public record OrganizationRequestDto(

        @Schema(
                description = "Name of the organization",
                example = "EventBuddy GmbH",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        @NotEmpty
        @Size(min = 1, max = 40, message = "Name must be between 1 and 40 characters")
        String name,

        @Schema(
                description = "Description of the organization",
                example = "We organize the best events in town.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @Size(max = 1500, message = "Description must be at most 1500 characters")
        String description,


        @Schema(
                description = "Website URL of the organization",
                example = "https://www.eventbuddy.com",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true,
                format = "uri"
        )
        @URL
        String website,

        @Schema(
                description = "Location of the organization",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = false
        )
        @Valid
        Location location,

        @Schema(
                description = "Contact information of the organization",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @Valid
        Contact contact
) {
}
