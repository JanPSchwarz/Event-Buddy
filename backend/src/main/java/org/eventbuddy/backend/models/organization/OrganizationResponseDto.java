package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

@Builder(toBuilder = true)
public record OrganizationResponseDto(

        @Schema(
                description = "Name of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        @NotBlank

        String name,

        @Schema(
                description = "ID of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        @NotBlank

        String id,

        @Schema(
                description = "Slug of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        @NotBlank

        String slug,

        @Schema(
                description = "List of admin user IDs",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Set<AppUserDto> owners,

        @Schema(
                description = "Description of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String description,

        @Schema(
                description = "Website URL of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        @URL
        String website,
        @Schema(
                description = "Image ID of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String imageId,

        @Schema(
                description = "Location of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Location location,

        @Schema(
                description = "Contact information of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Contact contact

) {
}
