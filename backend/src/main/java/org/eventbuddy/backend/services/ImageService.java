package org.eventbuddy.backend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.eventbuddy.backend.enums.ImageType;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.image.Image;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.repos.ImageRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
public class ImageService {

    ImageRepository imageRepo;

    OrganizationRepository organizationRepo;

    public Image getImageById( String imageUrl ) {
        return imageRepo.findById( imageUrl ).orElseThrow(
                () -> new ResourceNotFoundException( "Image not found with URL: " + imageUrl )
        );
    }

    public String storeImage( MultipartFile imageData ) throws IOException {

        if ( !ImageType.isSupported( imageData.getContentType() ) ) {
            throw new IllegalArgumentException( "Unsupported image type: " + imageData.getContentType() + " Allowed types are: " + String.join( ", ", ImageType.getAllFileTypes() ) );
        }

        Binary imageBinary = new org.bson.types.Binary( BsonBinarySubType.BINARY, imageData.getBytes() );

        Image buildImage = Image.builder()
                .imageData( imageBinary )
                .contentType( imageData.getContentType() )
                .build();

        Image savedImage = imageRepo.save( buildImage );

        return savedImage.getImageId();
    }

    public void updateImage( String organizationId, MultipartFile imageData ) throws IOException {

        Organization organization = organizationRepo.findById( organizationId ).orElseThrow(
                () -> new ResourceNotFoundException( "Organization not found with ID: " + organizationId )
        );

        String imageId = organization.getImageId();

        if ( imageId != null ) {
            Image image = imageRepo.findById( imageId ).orElseThrow(
                    () -> new ResourceNotFoundException( "Image not found with ID: " + imageId )
            );

            Binary imageBinary = new org.bson.types.Binary( BsonBinarySubType.BINARY, imageData.getBytes() );

            Image updatedImage = image.toBuilder()
                    .imageData( imageBinary )
                    .contentType( imageData.getContentType() )
                    .build();

            imageRepo.save( updatedImage );
        } else {
            String newImageId = storeImage( imageData );

            Organization updatedOrganization = organization.toBuilder()
                    .imageId( newImageId )
                    .build();

            organizationRepo.save( updatedOrganization );
        }
    }
}
