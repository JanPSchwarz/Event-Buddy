package org.eventbuddy.backend.models.app_user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.base_model.MongoBaseModel;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;


@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
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
    @NotBlank(message = "Provider ID must not be empty")
    @Indexed(unique = true)
    private String providerId;

    @Schema(
            description = "Email of the user",
            example = "john_doe@example.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @Indexed(unique = true)
    @Email(message = "Email should be valid")
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
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false,
            minLength = 3,
            maxLength = 20
    )
    @Indexed
    @NotBlank(message = "Name must not be empty")
    @Size(min = 3, max = 20, message = "Name must be between 3 and 20 characters")
    private String name;

    @Schema(
            description = "Avatar URL of the user",
            example = "https://example.com/avatar.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    @URL
    private String avatarUrl;

    @Schema(
            description = "List of organizations the user is associated with",
            example = "[\"exampleId-123\", \"exampleId-234\"]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private Set<String> organizations;
}
