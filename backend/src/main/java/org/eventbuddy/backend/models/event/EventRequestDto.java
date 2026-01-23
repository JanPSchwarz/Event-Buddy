package org.eventbuddy.backend.models.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.eventbuddy.backend.models.organization.Location;

import java.time.Instant;

@Builder(toBuilder = true)
public record EventRequestDto(

        @Schema(
                description = "Organization ID the event belongs to",
                example = "694ceebb43db708d04241ac9",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull(message = "Organization must be provided")
        @NotBlank(message = "Organization must be provided")
        String organizationId,

        @Schema(
                description = "Title of the event",
                example = "Annual Meetup 2024",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull(message = "Title must be provided")
        @NotBlank(message = "Title must not be blank")
        @Size(min = 4, max = 50, message = "Title must be between 4 and 50 characters")
        String title,

        @Schema(
                description = "Description of the event",
                example = "The annual meetup for all members.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @Size(max = 1500, message = "Description must be at most 1500 characters")
        String description,

        @Schema(
                description = "Date and time of the event in ISO 8601 format",
                example = "2024-09-15T18:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull(message = "Event date and time must not be null")
        @Future(message = "Event date and time must be in the future")
        Instant eventDateTime,

        @Schema(
                description = "Location of the event",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        @Valid
        Location location,

        @Schema(
                description = "Price of the event",
                example = "29.99",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        @Min(0)
        @PositiveOrZero
        Double price,

        @Schema(
                description = "Maximum ticket capacity of the event",
                example = "100",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @Nullable
        @Min(0)
        @Positive
        Integer maxTicketCapacity,

        @Schema(
                description = "Maximum tickets allowed per booking",
                example = "5",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @Positive
        @Min(0)
        Integer maxPerBooking
) {
}
