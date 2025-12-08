package org.eventbuddy.backend.models.appUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record UserSettings(
        @Schema(
                description = "Whether the user is visible to others",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        boolean userVisible,

        @Schema(
                description = "Whether to show the user's avatar",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        boolean showAvatar,

        @Schema(
                description = "Whether to show the user's organizations",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED,
                nullable = false
        )
        boolean showOrgas
) {
}
