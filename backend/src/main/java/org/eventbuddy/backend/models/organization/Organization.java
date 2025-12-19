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
            description = "List of admin user IDs",
            example = "[\"admin1\", \"admin2\"]",
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
    @Size(min = 4, max = 500, message = "Description must be between 4 and 500 characters")
    private String description;
    @Schema(
            description = "Website URL of the organization",
            example = "https://www.eventbuddy.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @URL
    private String website;

    private String imageId;
}

