package org.eventbuddy.backend.models.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import org.eventbuddy.backend.models.event.EventResponseDto;

@Builder(toBuilder = true)
public record BookingResponseDto(

        @Schema(
                description = "ID of the booking",
                example = "694ceebb43db708d04241ac9",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        String bookingId,

        @Schema(
                description = "Event for which the booking is made",
                requiredMode = Schema.RequiredMode.REQUIRED,
                implementation = EventResponseDto.class
        )
        @NotNull
        EventResponseDto hostingEvent,

        @Schema(
                description = "Number of tickets booked",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Positive
        int numberOfTickets,

        @Schema(
                description = "Name of the person booking tickets",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @NotBlank
        String name
) {
}
