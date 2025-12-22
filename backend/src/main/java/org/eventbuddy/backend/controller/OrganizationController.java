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
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationCreateDto;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.models.organization.OrganizationUpdateDto;
import org.eventbuddy.backend.services.AuthService;
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
@RequestMapping("/api/organization")
@Tag(name = "Organization", description = "CRUD operations for organizations")
@Validated
public class OrganizationController {

    private final OrganizationService organizationService;
    private final AuthService authService;
    private final ImageService imageService;

    public OrganizationController( OrganizationService organizationService, AuthService authService, ImageService imageService ) {
        this.organizationService = organizationService;
        this.authService = authService;
        this.imageService = imageService;
    }

    // == GET Endpoints ==

    @GetMapping("/all")
    @Operation(
            summary = "Get an array of all organizations (dto's)",
            description = "Returns an array of all organization dto's currently stored in the system."
    )
    public ResponseEntity<List<OrganizationResponseDto>> getAllOrganizations() {
        return ResponseEntity.ok( organizationService.getAllOrganizations() );
    }

    @GetMapping("/allRaw")
    @Operation(
            summary = "Get an array of all raw organizations (Super Admin only)",
            description = "Returns an array of all raw organization objects currently stored in the system."
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not authenticated or not authorized",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<List<Organization>> getAllRawOrganizations( OAuth2AuthenticationToken authToken ) {
        authService.isSuperAdmin( authToken );

        return ResponseEntity.ok( organizationService.getAllRawOrganizations() );
    }

    @GetMapping("/{organizationId}")
    @Operation(
            summary = "Get organization dto by ID",
            description = "Returns organization dto for the organization with the specified ID."
    )
    @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<OrganizationResponseDto> getOrganizationById( @PathVariable String organizationId ) {
        return ResponseEntity.ok( organizationService.getOrganizationDtoById( organizationId ) );
    }

    @GetMapping("/slug/{organizationSlug}")
    @Operation(
            summary = "Get organization dto by Slug",
            description = "Returns organization dto for the organization with the specified slug."
    )
    @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<OrganizationResponseDto> getOrganizationBySlug( @PathVariable String organizationSlug ) {
        return ResponseEntity.ok( organizationService.getOrganizationDtoByName( organizationSlug ) );
    }

    // == POST Endpoints ==

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Create a new organization",
            description = "Creates a new organization with the provided details and returns the created organization."
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
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
    public ResponseEntity<Organization> createOrganization(
            @Nullable @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("organization") OrganizationCreateDto organization,
            OAuth2AuthenticationToken authToken
    ) throws IOException {
        AppUser loggedInUser = authService.getAppUserByAuthToken( authToken );

        String imageId = file != null ? imageService.storeImage( file ) : null;

        return ResponseEntity.ok( organizationService.createOrganization( organization, loggedInUser, imageId ) );
    }

    // == PUT Endpoints ==

    @PutMapping(
            path = "/{organizationId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Update an organization (Organization Owners / Super Admin only)",
            description = "Updates the organization with the specified ID and returns the updated organization."
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
            responseCode = "401",
            description = "User not authenticated",
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
    public ResponseEntity<Organization> updateOrganization(
            OAuth2AuthenticationToken authToken,
            @Nullable @RequestPart("file") MultipartFile file,
            @PathVariable String organizationId,
            @Valid @RequestPart("updateOrganization") OrganizationUpdateDto updatedOrganization
    ) throws IOException {
        isOrgaOwnerOrSuperAdmin( authToken, organizationId );

        if ( file != null ) {
            imageService.updateImage( organizationId, file );
        }

        Organization organization = organizationService.updateOrganization( organizationId, updatedOrganization );

        return ResponseEntity.ok( organization );
    }

    @PutMapping("addOwner/{organizationId}/{userId}")
    @Operation(
            summary = "Add an owner to an organization (Organization Owners / Super Admin only)",
            description = "Adds a new owner to the organization with the specified ID and returns the updated organization."
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
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<Organization> addOwnerToOrganization(
            OAuth2AuthenticationToken authToken,
            @PathVariable String organizationId,
            @PathVariable String userId ) {
        isOrgaOwnerOrSuperAdmin( authToken, organizationId );

        Organization organization = organizationService.addOwnerToOrganization( organizationId, userId );
        return ResponseEntity.ok( organization );
    }

    @PutMapping("removerOwner/{organizationId}/{userId}")
    @Operation(
            summary = "Remove an owner from an organization (Organization Owners / Super Admin only)",
            description = "Removes an owner to the organization with the specified ID and returns the updated organization."
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
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<Organization> removeOwnerFromOrganization(
            OAuth2AuthenticationToken authToken,
            @PathVariable String organizationId,
            @PathVariable String userId ) {

        isOrgaOwnerOrSuperAdmin( authToken, organizationId );

        Organization organization = organizationService.deleteOwnerFromOrganization( organizationId, userId );
        return ResponseEntity.ok( organization );
    }

    // == DELETE Endpoints ==

    @DeleteMapping("/{organizationId}")
    @Operation(
            summary = "Delete an organization (Organization Owners / Super Admin only)",
            description = "Deletes the organization with the specified ID."
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
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<Void> deleteOrganization(
            OAuth2AuthenticationToken authToken,
            @PathVariable String organizationId
    ) {
        isOrgaOwnerOrSuperAdmin( authToken, organizationId );
        organizationService.deleteOrganizationById( organizationId );
        return ResponseEntity.noContent().build();
    }

    // == Helper Methods ==

    private void isOrgaOwnerOrSuperAdmin( OAuth2AuthenticationToken authToken, String organizationId ) {
        AppUser loggedInUser = authService.getAppUserByAuthToken( authToken );
        Set<String> organizationOwners = organizationService.getRawOrganizationById( organizationId ).getOwners();

        if ( !organizationOwners.contains( loggedInUser.getId() ) && loggedInUser.getRole() != Role.SUPER_ADMIN ) {
            throw new UnauthorizedException( "You are not allowed to perform this action." );
        }
    }
}
