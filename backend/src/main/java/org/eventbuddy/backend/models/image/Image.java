package org.eventbuddy.backend.models.image;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document("images")
@Schema(description = "Image entity")
public class Image {


    @Id
    private String imageId;

    @NotBlank
    private String contentType;

    @NotBlank
    private Binary imageData;
}
