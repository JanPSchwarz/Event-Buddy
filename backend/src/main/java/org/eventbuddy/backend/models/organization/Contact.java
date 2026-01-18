package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record Contact(
        @Schema(
                description = "Email address of the organization",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "test@example.com",
                format = "email"
        )
        @Email
        String email,
        @Schema(
                description = "Phone number of the organization",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "+1234567890"
        )
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$|^\\+?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
        String phoneNumber
) {
}
