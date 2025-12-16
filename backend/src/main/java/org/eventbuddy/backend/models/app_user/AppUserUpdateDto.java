package org.eventbuddy.backend.models.app_user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Data Transfer Object for updating user profile")
public record AppUserUpdateDto(
        @Schema(
                description = "Email of the user",
                example = "john_doe@example.com",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        @Email(message = "Email should be valid")
        String email,

        @Schema(
                description = "Name of the user",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true,
                minLength = 3,
                maxLength = 20
        )
        @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
        String name,

        @Schema(
                description = "User settings",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true,
                implementation = UserSettings.class
        )
        UserSettings userSettings
) {
}
