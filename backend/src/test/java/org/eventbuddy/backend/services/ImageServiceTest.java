package org.eventbuddy.backend.services;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.eventbuddy.backend.enums.ImageType;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.image.Image;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.repos.ImageRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    ImageRepository mockImageRepo;

    @Mock
    OrganizationRepository mockOrganizationRepo;

    @InjectMocks
    ImageService mockImageService;


    @Test
    @DisplayName("Should return Image when successfully")
    void getImageById_shouldReturnTrueWhenImageFound() {
        String imageId = "testImageId";

        Image expectedImage = Image.builder()
                .imageId( imageId )
                .build();

        when( mockImageRepo.findById( imageId ) ).thenReturn( Optional.of( expectedImage ) );

        Image actualImage = mockImageService.getImageById( imageId );

        assertEquals( expectedImage, actualImage );
        verify( mockImageRepo ).findById( imageId );
    }

    @Test
    @DisplayName("Should throw when Image not found")
    void getImageById_shouldThrowWhenNotFound() {
        String imageId = "testImageId";

        String expectedMessage = "Image not found with URL: " + imageId;

        when( mockImageRepo.findById( imageId ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () ->
                mockImageService.getImageById( imageId ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( expectedMessage );

        verify( mockImageRepo ).findById( imageId );
    }

    @ParameterizedTest
    @DisplayName("Should return true when image stored successfully")
    @ValueSource(strings = { "image/jpeg", "image/webp", "image/png", "image/heic", "image/svg+xml" })
    void storeImage_shouldReturnTrueWhenImageStored( String contentType ) throws IOException {

        byte[] imageDataBytes = "fake image content".getBytes();

        MultipartFile mockFile = new MockMultipartFile( "file", null, contentType, imageDataBytes );

        Binary imageBinary = new Binary( BsonBinarySubType.BINARY, imageDataBytes );

        String expectedImageId = "123";

        Image givenImage = Image.builder()
                .contentType( mockFile.getContentType() )
                .imageData( imageBinary )
                .build();

        Image expectedImage = givenImage.toBuilder()
                .imageId( expectedImageId )
                .build();

        when( mockImageRepo.save( givenImage ) ).thenReturn( expectedImage );

        String actualImageId = mockImageService.storeImage( mockFile );

        assertEquals( expectedImageId, actualImageId );
        verify( mockImageRepo ).save( givenImage );
    }

    @ParameterizedTest
    @DisplayName("Should throw when unsupported image type")
    @ValueSource(strings = { "application/pdf", "text/plain", "video/mp4" })
    void storeImage_shouldThrowWhenUnsupportedImageType( String contentType ) {
        byte[] imageDataBytes = "fake image content".getBytes();

        MultipartFile mockFile = new MockMultipartFile( "file", null, contentType, imageDataBytes );

        String expectedMessage = "Unsupported image type: " + mockFile.getContentType() + " Allowed types are: " + String.join( ", ", ImageType.getAllFileTypes() );

        assertThatThrownBy( () ->
                mockImageService.storeImage( mockFile ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessage( expectedMessage );
    }

    @Test
    @DisplayName("Should return true when Image updated")
    void updateImage_shouldReturnTrueWhenImageUpdated() throws IOException {

        byte[] imageDataBytes = "fake image content".getBytes();

        MultipartFile mockFile = new MockMultipartFile( "file", null, "image/jpeg", imageDataBytes );

        String givenOrganizationId = "org123";

        String givenImageId = "123";

        Image givenImage = Image.builder()
                .imageId( givenImageId )
                .contentType( mockFile.getContentType() )
                .imageData( new Binary( BsonBinarySubType.BINARY, imageDataBytes ) )
                .build();

        Organization givenOrganization = Organization.builder()
                .id( givenOrganizationId )
                .imageId( givenImageId )
                .build();

        when( mockOrganizationRepo.findById( givenOrganizationId ) ).thenReturn( Optional.of( givenOrganization ) );
        when( mockImageRepo.save( givenImage ) ).thenReturn( givenImage );
        when( mockImageRepo.findById( givenImageId ) ).thenReturn( Optional.of( givenImage ) );

        String actualImageId = mockImageService.updateImage( givenOrganizationId, mockFile );

        assertEquals( givenImageId, actualImageId );

        verify( mockImageRepo ).save( givenImage );
        verify( mockImageRepo ).findById( givenImageId );
        verify( mockOrganizationRepo ).findById( givenOrganizationId );
    }

    @Test
    @DisplayName("Should return true when Image created on update")
    void updateImage_shouldReturnTrueWhenImageCreatedOnUpdate() throws IOException {

        byte[] imageDataBytes = "fake image content".getBytes();

        MultipartFile mockFile = new MockMultipartFile( "file", null, "image/jpeg", imageDataBytes );

        String givenOrganizationId = "org123";

        String givenImageId = "123";

        Image imageToSave = Image.builder()
                .contentType( mockFile.getContentType() )
                .imageData( new Binary( BsonBinarySubType.BINARY, imageDataBytes ) )
                .build();

        Image savedImage = imageToSave.toBuilder()
                .imageId( givenImageId )
                .build();

        Organization givenOrganization = Organization.builder()
                .id( givenOrganizationId )
                .build();

        Organization updatedOrganization = givenOrganization.toBuilder()
                .imageId( givenImageId )
                .build();

        when( mockOrganizationRepo.findById( givenOrganizationId ) ).thenReturn( Optional.of( givenOrganization ) );
        when( mockImageRepo.save( imageToSave ) ).thenReturn( savedImage );
        when( mockOrganizationRepo.save( updatedOrganization ) ).thenReturn( updatedOrganization );

        String actualImageId = mockImageService.updateImage( givenOrganizationId, mockFile );

        assertEquals( givenImageId, actualImageId );

        verify( mockImageRepo ).save( imageToSave );
        verify( mockOrganizationRepo ).save( updatedOrganization );
        verify( mockOrganizationRepo ).findById( givenOrganizationId );
    }

    @Test
    @DisplayName("Should throw when Image not found on update")
    void updateImage_shouldThrowWhenImageNotFound() {
        String organizationId = "org123";
        String imageId = "img123";


        byte[] imageDataBytes = "fake image content".getBytes();

        MultipartFile mockFile = new MockMultipartFile( "file", null, "image/jpeg", imageDataBytes );

        Organization givenOrganization = Organization.builder()
                .id( organizationId )
                .imageId( imageId )
                .build();

        when( mockOrganizationRepo.findById( organizationId ) ).thenReturn( Optional.of( givenOrganization ) );
        when( mockImageRepo.findById( imageId ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () ->
                mockImageService.updateImage( organizationId, mockFile ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Image not found with ID: " + imageId );

        verify( mockOrganizationRepo ).findById( organizationId );
        verify( mockImageRepo ).findById( imageId );
    }

    @Test
    @DisplayName("Should throw when Organization not found on update")
    void updateImage_shouldThrowWhenOrganizationNotFound() {
        String organizationId = "nonExistingOrgId";

        byte[] imageDataBytes = "fake image content".getBytes();

        MultipartFile mockFile = new MockMultipartFile( "file", null, "image/jpeg", imageDataBytes );

        when( mockOrganizationRepo.findById( organizationId ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () ->
                mockImageService.updateImage( organizationId, mockFile ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Organization not found with ID: " + organizationId );

        verify( mockOrganizationRepo ).findById( organizationId );
    }
}