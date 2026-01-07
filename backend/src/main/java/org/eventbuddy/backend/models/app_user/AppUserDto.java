package org.eventbuddy.backend.models.app_user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;

import java.util.List;

@Builder(toBuilder = true)
public record AppUserDto(
        @Schema(
                description = "Email of the user",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String email,

        @Schema(
                description = "Unique identifier of the user",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String id,

        @Schema(
                description = "Name of the user",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        @NotBlank
        String name,
        @Schema(
                description = "Avatar URL of the user",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String avatarUrl,
        @Schema(
                description = "List of organizations the user is associated with",
                example = "[\"{OrganizationResponseDto Object}\"]",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        List<OrganizationResponseDto> organizations
) {
}
