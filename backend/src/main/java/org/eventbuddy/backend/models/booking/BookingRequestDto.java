package org.eventbuddy.backend.models.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder(toBuilder = true)
public record BookingRequestDto(

        @Schema(
                description = "ID of the event for which the booking is made",
                example = "694ceebb43db708d04241ac9",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        String eventId,

        @Schema(
                description = "ID of the user who made the booking",
                example = "user12345",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        String userId,

        @Schema(
                description = "Number of tickets booked",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        @Positive
        int numberOfTickets,

        @Schema(
                description = "Name of the person booking tickets",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        @NotNull
        @NotBlank
        String name

) {
}
