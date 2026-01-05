package org.eventbuddy.backend.controller;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.eventbuddy.backend.TestcontainersConfiguration;
import org.eventbuddy.backend.models.image.Image;
import org.eventbuddy.backend.repos.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
class ImageControllerTest {

    @Autowired
    private ImageRepository imageRepo;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return image")
    void getImage() throws Exception {
        byte[] imageData = "imageData".getBytes();
        String contentType = "image/png";

        Binary imageBinary = new Binary( BsonBinarySubType.BINARY, imageData );

        Image newImage = Image.builder()
                .imageData( imageBinary )
                .contentType( contentType )
                .build();

        Image savedImage = imageRepo.save( newImage );

        mockMvc.perform( get( "/api/images/" + savedImage.getImageId() ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType( contentType ) )
                .andExpect( content().bytes( imageData ) );

    }

    @Test
    @DisplayName("Should throw 404 not found")
    void getImage_throws404() throws Exception {
        String nonExistingImageId = "nonExistingImageId";

        mockMvc.perform( get( "/api/images/" + nonExistingImageId ) )
                .andExpect( status().isNotFound() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( jsonPath( "$.error" ).value( "Image not found with URL: " + nonExistingImageId ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }
}