package org.eventbuddy.backend.models.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eventbuddy.backend.models.base_model.MongoBaseModel;
import org.eventbuddy.backend.models.event.Event;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document("bookings")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Booking entity")
public class Booking extends MongoBaseModel {

    @Schema(
            description = "Name of the person booking tickets",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    @NotBlank
    String name;

    @Schema(
            description = "Number of tickets booked",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    @Positive
    Integer numberOfTickets;


    @Schema(
            description = "Event for which the booking is made",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    @DocumentReference
    Event event;

    @Schema(
            description = "ID of the user who made the booking",
            example = "user12345",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    String userId;
}
