package org.eventbuddy.backend.models.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder()
public record Location(

        @Schema(
                description = "Street and house number of the location",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "Example Street 1A"
        )
        @NotNull
        @NotEmpty
        String address,

        @Schema(
                description = "City of the location",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "Sample City"
        )
        @NotNull
        @NotEmpty
        String city,

        @Schema(
                description = "City zip code",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "12345"
        )
        @NotNull
        @NotEmpty
        String zipCode,

        @Schema(
                description = "Country of the location",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "Sample Country"
        )
        @NotNull
        @NotEmpty
        String country,

        @Schema(
                description = "Latitude coordinate",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "52.5200"
        )
        @NotNull
        double latitude,

        @Schema(
                description = "Longitude coordinate",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "13.4050"
        )
        @NotNull
        double longitude

) {
}
