package org.eventbuddy.backend.models.appUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.baseModel.MongoBaseModel;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("users")
@Schema(description = "Application user entity")
public class AppUser extends MongoBaseModel {

    @Schema(
            description = "ID of the authentication provider",
            example = "github_1234567890",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @NotBlank
    @Indexed(unique = true)
    private String providerId;

    @Schema(
            description = "Email of the user",
            example = "john_doe@example.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true,
            minLength = 5,
            maxLength = 50
    )
    @Indexed(unique = true)
    @Email
    private String email;

    @Schema(
            description = "Role of the user",
            example = "USER",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false,
            implementation = Role.class
    )
    private Role role;

    @Schema(
            description = "User settings",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false,
            implementation = UserSettings.class
    )
    private UserSettings userSettings;

    @Schema(
            description = "Name of the user",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true,
            minLength = 3,
            maxLength = 20
    )
    @Indexed
    private String name;

    @Schema(
            description = "Avatar URL of the user",
            example = "https://example.com/avatar.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @URL
    private String avatarUrl;
}
