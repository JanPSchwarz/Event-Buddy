package org.eventbuddy.backend.models.app_user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AppUserDto(
        @Schema(
                description = "Email of the user",
                accessMode = Schema.AccessMode.READ_ONLY)
        String email,

        @Schema(
                description = "Name of the user",
                accessMode = Schema.AccessMode.READ_ONLY)
        String name,
        @Schema(
                description = "Avatar URL of the user",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String avatarUrl
) {
}
