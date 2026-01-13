package org.eventbuddy.backend.models.event;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eventbuddy.backend.models.base_model.MongoBaseModel;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;
import java.util.Map;

@Document("events")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Event entity")
public class Event extends MongoBaseModel {

    @Schema(
            description = "Organization hosting the event",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    @DocumentReference
    Organization eventOrganization;

    @Schema(
            description = "Title of the event",
            example = "Annual Meetup 2024",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull(message = "Title must not be blank")
    @Size(min = 4, max = 50, message = "Title must be between 4 and 50 characters")
    @Indexed
    private String title;
    @Schema(
            description = "Description of the event",
            example = "The annual meetup for all members.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @Size(max = 1500, message = "Description must be at most 1500 characters")
    private String description;
    @Schema(
            description = "Date and time of the event in ISO 8601 format",
            example = "2024-09-15T18:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @Future(message = "Event date and time must be in the future")
    @NotNull(message = "Event date and time must not be null")
    private Instant eventDateTime;
    @Schema(
            description = "Location of the event",
            example = "123 Main St, Anytown, USA",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = true
    )
    @Valid
    @NotNull(message = "Location must be specified")
    private Location location;
    @Schema(
            description = "Price of the event ticket",
            example = "49.99",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @NotNull
    @PositiveOrZero
    private Double price;
    @Schema(
            description = "Maximum capacity of the event",
            example = "100",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @Positive
    private Integer maxTicketCapacity;
    @Schema(
            description = "Free capacity of the event",
            example = "75",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @PositiveOrZero
    private Integer freeTicketCapacity;
    @Schema(
            description = "Maximum number of tickets allowed per booking",
            example = "5",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private Integer maxPerBooking;

    @Schema(
            description = "Indicates if the event is almost sold out",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private Boolean ticketAlarm;

    @Schema(
            description = "Indicates if the event is sold out",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private Boolean isSoldOut;

    @Schema(
            description = "Guest list with guest names and their ticket counts",
            example = "{\"John Doe\": 2, \"Jane Smith\": 1}",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private Map<String, Integer> guestList;

    @Schema(
            description = "Image ID of the event's banner",
            example = "1234567890abcdef12345678",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String imageId;
}
