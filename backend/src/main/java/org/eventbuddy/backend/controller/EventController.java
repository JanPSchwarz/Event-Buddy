package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.UnauthorizedException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.event.EventRequestDto;
import org.eventbuddy.backend.models.event.EventResponseDto;
import org.eventbuddy.backend.services.AuthService;
import org.eventbuddy.backend.services.EventService;
import org.eventbuddy.backend.services.ImageService;
import org.eventbuddy.backend.services.OrganizationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Controller", description = "CRUD operations for managing events")
@Validated
public class EventController {


    private final EventService eventService;
    private final OrganizationService organizationService;
    private final AuthService authService;
    private final ImageService imageService;

    public EventController( EventService eventService, ImageService imageService, OrganizationService organizationService, AuthService authService ) {
        this.eventService = eventService;
        this.organizationService = organizationService;
        this.authService = authService;
        this.imageService = imageService;
    }

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
            responseCode = "404",
            description = "Event not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<Event> getRawEventById( @PathVariable String eventId, OAuth2AuthenticationToken authToken ) {
        checkIsOrganizationOwnerOrSuperAdmin( eventId, authToken );
        return ResponseEntity.ok( eventService.getRawEventById( eventId ) );
    }

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
            description = "Not authorized to create event for this organization",
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
    public ResponseEntity<Event> createEvent(
            @Nullable @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @Valid @RequestPart("event") EventRequestDto eventRequestDto,
            OAuth2AuthenticationToken authToken
    ) throws IOException {
        checkIsOrganizationOwner( eventRequestDto.organizationId(), authToken );

        String imageId = null;

        if ( imageFile != null ) {
            imageId = imageService.storeImage( imageFile );
        }

        return ResponseEntity.ok( eventService.createEvent( eventRequestDto, imageId ) );
    }

    // == Helper methods ==

    private void checkIsOrganizationOwner( String organizationId, OAuth2AuthenticationToken authToken ) {
        AppUser loggedInUser = authService.getAppUserByAuthToken( authToken );
        Set<String> organizationOwners = organizationService.getRawOrganizationById( organizationId )
                .getOwners();

        if ( !organizationOwners.contains( loggedInUser.getId() ) ) {
            throw new UnauthorizedException( "Your are not an owner of the organization with id: " + organizationId );
        }
    }

    private void checkIsOrganizationOwnerOrSuperAdmin( String eventId, OAuth2AuthenticationToken authToken ) {
        AppUser loggedInUser = authService.getAppUserByAuthToken( authToken );
        Event event = eventService.getRawEventById( eventId );

        System.out.println( "Event Organization ID: " + event.toString() );

        String organizationId = event.getEventOrganization().getId();

        Set<String> organizationOwners = organizationService.getRawOrganizationById( organizationId )
                .getOwners();

        if ( !organizationOwners.contains( loggedInUser.getId() ) && loggedInUser.getRole() != Role.SUPER_ADMIN ) {
            throw new UnauthorizedException( "Your are not allowed to perform this action." );
        }
    }
}
