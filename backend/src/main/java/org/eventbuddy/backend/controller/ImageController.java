package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.models.image.Image;
import org.eventbuddy.backend.services.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;


@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController( ImageService imageService ) {
        this.imageService = imageService;
    }

    @GetMapping(path = "/{imageId}")
    @Operation(
            summary = "Get image by ID",
            description = "Retrieve an image by its unique identifier."
    )
    @ApiResponse(
            responseCode = "404",
            description = "No image found with url",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<byte[]> getImage( @PathVariable String imageId ) {
        Image image = imageService.getImageById( imageId );

        return ResponseEntity.ok()
                .contentType( MediaType.parseMediaType( image.getContentType() ) )
                .body( image.getImageData().getData() );
    }

    @GetMapping(path = "/data-url/{imageId}")
    @Operation(
            summary = "Get image as data URL",
            description = "Retrieve an image as data URL for frontend usage."
    )
    public ResponseEntity<String> getImageAsDataUrl( @PathVariable String imageId ) {
        Image image = imageService.getImageById( imageId );

        String base64Data = Base64.getEncoder().encodeToString( image.getImageData().getData() );
        String dataUrl = "data:" + image.getContentType() + ";base64," + base64Data;

        return ResponseEntity.ok()
                .contentType( MediaType.TEXT_PLAIN )
                .body( dataUrl );
    }

}
