package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

@Builder
public record OrganizationResponseDto(

        @Schema(
                description = "Name of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        @NotNull
        @NotEmpty
        String name,

        @Schema(
                description = "Slug of the organization",
                accessMode = Schema.AccessMode.READ_ONLY
        )
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
        String imageId

) {
}
