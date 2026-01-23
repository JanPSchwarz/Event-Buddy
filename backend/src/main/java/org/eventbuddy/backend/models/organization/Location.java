package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder()
public record Location(

        @Schema(
                description = "Name of the location",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "Main Office"
        )
        String locationName,

        @Schema(
                description = "Street and house number of the location",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "Example Street 1A"
        )
        @NotNull
        @NotBlank
        @Size(min = 1, message = "Address must be not be empty")
        String address,

        @Schema(
                description = "City of the location",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "Sample City"
        )
        @NotNull
        @NotBlank
        @Size(min = 1, message = "Address must be not be empty")
        String city,

        @Schema(
                description = "City zip code",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "12345"
        )
        @NotNull
        @NotBlank
        @Size(min = 1, message = "Address must be not be empty")
        String zipCode,

        @Schema(
                description = "Country of the location",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "Sample Country"
        )
        @NotNull
        @NotBlank
        @Size(min = 1, message = "Address must be not be empty")
        String country,

        @Schema(
                description = "Latitude coordinate",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "52.5200"
        )
        Double latitude,

        @Schema(
                description = "Longitude coordinate",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "13.4050"
        )
        Double longitude

) {
}
