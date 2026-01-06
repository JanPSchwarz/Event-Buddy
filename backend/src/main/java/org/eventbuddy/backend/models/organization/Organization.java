package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eventbuddy.backend.models.base_model.MongoBaseModel;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document("organizations")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Organization entity")
public class Organization extends MongoBaseModel {

    @Schema(
            description = "Contact information of the organization",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    Contact contact;

    @Schema(
            description = "Name of the organization",
            example = "EventBuddy GmbH",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    @NotEmpty
    @Indexed(unique = true)
    private String name;
    @Schema(
            description = "Slug of the organization",
            example = "event-buddy-gmbh",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    @NotEmpty
    @Indexed(unique = true)
    private String slug;
    @Schema(
            description = "List of admin user IDs (mongo object ids)",
            example = "[\"694ceebb43db708d04241ac9\", \"694ceebb43db708d04241ac8\"]",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @Valid
    @NotEmpty(message = "List cannot be empty")
    private Set<String> owners;
    @Schema(
            description = "Description of the organization",
            example = "We organize the best events in town.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @Size(min = 4, max = 1500, message = "Description must be between 4 and 500 characters")
    private String description;
    @Schema(
            description = "Website URL of the organization",
            example = "https://www.eventbuddy.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @URL
    private String website;
    @Schema(
            description = "Image ID of the organization's logo",
            example = "1234567890abcdef12345678",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String imageId;
    @Schema(
            description = "Location of the organization",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotNull
    @NotEmpty
    private Location location;
}

