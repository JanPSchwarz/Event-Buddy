package org.eventbuddy.backend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.eventbuddy.backend.enums.ImageType;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.image.Image;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.repos.EventRepository;
import org.eventbuddy.backend.repos.ImageRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
public class ImageService {

    private final ImageRepository imageRepo;

    private final OrganizationRepository organizationRepo;

    private final EventRepository eventRepo;

    public Image getImageById( String imageUrl ) {
        return imageRepo.findById( imageUrl ).orElseThrow(
                () -> new ResourceNotFoundException( "Image not found with URL: " + imageUrl )
        );
    }

    public String storeImage( MultipartFile imageData ) throws IOException {

        if ( !ImageType.isSupported( imageData.getContentType() ) ) {
            throw new IllegalArgumentException( "Unsupported image type: " + imageData.getContentType() + " Allowed types are: " + String.join( ", ", ImageType.getAllFileTypes() ) );
        }

        byte[] bytes = imageData.getBytes();

        // in MB, adjust as needed
        int maxFileSize = 5;

        if ( bytes.length > 1024 * 1024 * maxFileSize ) {
            throw new MaxUploadSizeExceededException( maxFileSize );
        }

        Binary imageBinary = new Binary( BsonBinarySubType.BINARY, imageData.getBytes() );

        Image buildImage = Image.builder()
                .imageData( imageBinary )
                .contentType( imageData.getContentType() )
                .build();

        Image savedImage = imageRepo.save( buildImage );

        return savedImage.getImageId();
    }

    public String updateOrganizationImage( String organizationId, MultipartFile imageData ) throws IOException {

        Organization organization = organizationRepo.findById( organizationId ).orElseThrow(
                () -> new ResourceNotFoundException( "Organization not found with ID: " + organizationId )
        );

        String existingImageId = organization.getImageId();

        if ( existingImageId != null ) {
            Image image = imageRepo.findById( existingImageId ).orElseThrow(
                    () -> new ResourceNotFoundException( "Image not found with ID: " + existingImageId )
            );

            Binary imageBinary = new org.bson.types.Binary( BsonBinarySubType.BINARY, imageData.getBytes() );

            Image updatedImage = image.toBuilder()
                    .imageData( imageBinary )
                    .contentType( imageData.getContentType() )
                    .build();

            Image savedImage = imageRepo.save( updatedImage );

            return savedImage.getImageId();
        } else {
            String newImageId = storeImage( imageData );

            Organization updatedOrganization = organization.toBuilder()
                    .imageId( newImageId )
                    .build();

            Organization savedOrganization = organizationRepo.save( updatedOrganization );

            return savedOrganization.getImageId();
        }
    }

    public String updateEventImage( String eventId, MultipartFile imageData ) throws IOException {

        Event event = eventRepo.findById( eventId ).orElseThrow(
                () -> new ResourceNotFoundException( "Event not found with ID: " + eventId )
        );
        String existingImageId = event.getImageId();

        if ( existingImageId != null ) {
            Image image = imageRepo.findById( existingImageId ).orElseThrow(
                    () -> new ResourceNotFoundException( "Image not found with ID: " + existingImageId )
            );
            Binary imageBinary = new org.bson.types.Binary( BsonBinarySubType.BINARY, imageData.getBytes() );
            Image updatedImage = image.toBuilder()
                    .imageData( imageBinary )
                    .contentType( imageData.getContentType() )
                    .build();
            Image savedImage = imageRepo.save( updatedImage );
            return savedImage.getImageId();
        } else {
            String newImageId = storeImage( imageData );
            Event updatedEvent = event.toBuilder()
                    .imageId( newImageId )
                    .build();
            Event savedEvent = eventRepo.save( updatedEvent );

            return savedEvent.getImageId();
        }
    }

    public void deleteImageFromEvent( String eventId ) {
        Event event = eventRepo.findById( eventId ).orElseThrow(
                () -> new ResourceNotFoundException( "Event not found with ID: " + eventId )
        );

        if ( event.getImageId() != null ) {
            Event updatedEvent = event.toBuilder()
                    .imageId( null )
                    .build();

            eventRepo.save( updatedEvent );
            imageRepo.deleteById( event.getImageId() );
        }
    }

    public void deleteImageFromOrganization( String organizationId ) {
        Organization organization = organizationRepo.findById( organizationId ).orElseThrow(
                () -> new ResourceNotFoundException( "Organization not found with ID: " + organizationId )
        );

        if ( organization.getImageId() != null ) {
            Organization updatedOrganization = organization.toBuilder()
                    .imageId( null )
                    .build();

            organizationRepo.save( updatedOrganization );
            imageRepo.deleteById( organization.getImageId() );
        }
    }
}
