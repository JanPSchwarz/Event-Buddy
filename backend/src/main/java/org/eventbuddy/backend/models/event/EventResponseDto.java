package org.eventbuddy.backend.models.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;

import java.time.Instant;

@Builder(toBuilder = true)
public record EventResponseDto(

        @Schema(
                description = "ID of the created event",
                example = "694ceebb43db708d04241ac9"
        )
        @NotNull
        String id,

        @Schema(
                description = "Organization (dto) the event belongs to",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        OrganizationResponseDto eventOrganization,

        @Schema(
                description = "Title of the event",
                example = "Annual Meetup 2024"
        )
        @NotNull
        String title,

        @Schema(
                description = "Description of the event",
                example = "The annual meetup for all members."
        )
        String description,

        @Schema(
                description = "Iso Date and time of the event",
                example = "2024-09-15T18:00:00Z"
        )
        @NotNull
        Instant eventDateTime,

        @Schema(
                description = "Location of the event",
                implementation = Location.class
        )
        Location location,

        @Schema(
                description = "Ticket price for the event",
                example = "49.99"
        )
        @NotNull
        Double price,

        @Schema(
                description = "Indicates if event is almost sold out",
                example = "true"
        )
        Boolean ticketAlarm,

        @Schema(
                description = "Indicates if event is sold out",
                example = "true"
        )
        Boolean isSoldOut,

        @Schema(
                description = "Maximum tickets allowed per booking",
                example = "5"
        )
        Integer maxPerBooking,

        @Schema(
                description = "Image ID associated with the event",
                example = "1234567890abcdef12345678"
        )
        String imageId
) {
}
