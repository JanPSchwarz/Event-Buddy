package org.eventbuddy.backend.models.base_model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
public abstract class MongoBaseModel {

    @Schema(
            description = "Unique identifier of the mongoDb entity",
            example = "60d5ec49f1d2c12a34567890",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @Id
    private String id;

    @Schema(
            description = "The timestamp when this entity was created.",
            example = "2024-01-01T12:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @CreatedDate
    private Instant createdDate;

    @Schema(
            description = "The timestamp of the last modification of this entity.",
            example = "2024-01-01T12:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED,
            nullable = false
    )
    @LastModifiedDate
    private Instant lastModifiedDate;
}
