package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.eventbuddy.backend.configs.CustomOAuth2User;
import org.eventbuddy.backend.configs.annotations.IsAuthenticated;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.event.EventRequestDto;
import org.eventbuddy.backend.models.event.EventResponseDto;
import org.eventbuddy.backend.services.EventService;
import org.eventbuddy.backend.services.ImageService;
import org.eventbuddy.backend.services.OrganizationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Controller", description = "CRUD operations for managing events")
@Validated
public class EventController {


    private final EventService eventService;
    private final OrganizationService organizationService;
    private final ImageService imageService;

    public EventController( EventService eventService, ImageService imageService, OrganizationService organizationService ) {
        this.eventService = eventService;
        this.organizationService = organizationService;
        this.imageService = imageService;
    }

    // === GET Endpoints ===

    @GetMapping("/all")
    @Operation(
            summary = "Get an array of all Event dtos",
            description = "Retrieve a list of all events"
    )
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok( eventService.getAllEvents() );
    }

    @GetMapping("/{eventId}")
    @Operation(
            summary = "Get Event dto by ID",
            description = "Retrieve an event by its unique ID"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Event not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<EventResponseDto> getEventById( @PathVariable String eventId ) {
        return ResponseEntity.ok( eventService.getEventById( eventId ) );
    }

    @GetMapping("/byOrga/{organizationId}")
    @Operation(
            summary = "Get Event dtos by orga id",
            description = "Get list of event dtos by orga id"

    )
    public ResponseEntity<List<EventResponseDto>> getEventsByOrgaId( @PathVariable String organizationId ) {

        return ResponseEntity.ok( eventService.getEventsByOrganizationId( organizationId ) );
    }

    @GetMapping("/byUser/{userId}")
    @Operation(
            summary = "Get Event dtos by orga id",
            description = "Get list of event dtos by orga id"

    )
    public ResponseEntity<List<EventResponseDto>> getEventsByUserId( @PathVariable String userId ) {

        return ResponseEntity.ok( eventService.getEventByUserId( userId ) );
    }

    @GetMapping("/raw/{eventId}")
    @Operation(
            summary = "Get Event by ID",
            description = "Retrieve an event by its unique ID"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Event not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @IsAuthenticated
    public ResponseEntity<Event> getRawEventById( @PathVariable String eventId, @AuthenticationPrincipal CustomOAuth2User user ) {
        checkIsOrganizationOwnerOrSuperAdmin( eventId, user.getUser() );
        return ResponseEntity.ok( eventService.getRawEventById( eventId ) );
    }

    // === POST Endpoints ===

    @PostMapping(
            path = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new Event",
            description = "Create a new event with the provided details"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request, invalid event data",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "413",
            description = "Payload too large",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @IsAuthenticated
    public ResponseEntity<Event> createEvent(
            @Nullable @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @Valid @RequestPart("event") EventRequestDto eventRequestDto,
            @AuthenticationPrincipal CustomOAuth2User user
    ) throws IOException {
        checkIsOrganizationOwner( eventRequestDto.organizationId(), user.getUser() );

        String imageId = null;

        if ( imageFile != null ) {
            imageId = imageService.storeImage( imageFile );
        }

        return ResponseEntity.ok( eventService.createEvent( eventRequestDto, imageId ) );
    }

    // === PUT Endpoints ===

    @PutMapping(
            path = "/{eventId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Update an event (Organization Owners / Super Admin only)",
            description = "Updates the event with the specified ID and returns the updated event."
    )
    @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Event/organization not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "413",
            description = "Payload too large",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @IsAuthenticated
    public ResponseEntity<Event> updateEvent(
            @AuthenticationPrincipal CustomOAuth2User user,
            @Nullable @RequestPart(value = "imageFile", required = false) MultipartFile file,
            Optional<Boolean> deleteImage,
            @PathVariable String eventId,
            @Valid @RequestPart("updateEvent") EventRequestDto updateEventData
    ) throws IOException {
        checkIsOrganizationOwnerOrSuperAdmin( eventId, user.getUser() );

        if ( file != null ) {
            imageService.updateEventImage( eventId, file );
        } else if ( deleteImage.orElse( false ) ) {
            imageService.deleteImageFromEvent( eventId );
        }

        Event updatedEvent = eventService.updateEvent( eventId, updateEventData );

        return ResponseEntity.ok( updatedEvent );
    }

    // === DELETE Endpoints ===
    @DeleteMapping("/{eventId}")
    @Operation(
            summary = "Delete an event (Organization Owners / Super Admin only)",
            description = "Deletes the event with the specified ID."
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not authenticated/authorized",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Event not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @IsAuthenticated
    public ResponseEntity<Void> deleteEventById(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable String eventId
    ) {
        checkIsOrganizationOwnerOrSuperAdmin( eventId, user.getUser() );
        eventService.deleteEventById( eventId );
        return ResponseEntity.noContent().build();
    }

    // == Helper methods ==

    private void checkIsOrganizationOwner( String organizationId, AppUser user ) {
        Set<String> organizationOwners = organizationService.getRawOrganizationById( organizationId )
                .getOwners();

        if ( !organizationOwners.contains( user.getId() ) ) {
            throw new AccessDeniedException( "Your are not an owner of the organization with id: " + organizationId );
        }
    }

    private void checkIsOrganizationOwnerOrSuperAdmin( String eventId, AppUser user ) {
        Event event = eventService.getRawEventById( eventId );

        String organizationId = event.getEventOrganization().getId();

        Set<String> organizationOwners = organizationService.getRawOrganizationById( organizationId )
                .getOwners();

        if ( !organizationOwners.contains( user.getId() ) && user.getRole() != Role.SUPER_ADMIN ) {
            throw new AccessDeniedException( "Your are not allowed to perform this action." );
        }
    }
}
