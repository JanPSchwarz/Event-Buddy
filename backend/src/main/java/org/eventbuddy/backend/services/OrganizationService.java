package org.eventbuddy.backend.services;

import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationCreateDto;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.models.organization.OrganizationUpdateDto;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepo;

    private final UserService userService;

    public OrganizationService( OrganizationRepository organizationRepo, @Lazy UserService userService ) {
        this.organizationRepo = organizationRepo;
        this.userService = userService;
    }

    // === Public Organization Methods (DTO) ===

    public List<OrganizationResponseDto> getAllOrganizations() {
        return getAllRawOrganizations().stream()
                .map( this::organizationToDtoMapper )
                .toList();
    }

    public OrganizationResponseDto getOrganizationDtoById( String organizationId ) {
        Organization organization = getOrganizationByIdOrThrow( organizationId );
        return organizationToDtoMapper( organization );
    }

    public OrganizationResponseDto getOrganizationDtoByName( String organizationName ) {
        Organization organization = getOrganizationBySlugOrThrow( organizationName );
        return organizationToDtoMapper( organization );
    }

    // === Raw Organization Methods (Internal || Admins) ===

    public List<Organization> getAllRawOrganizations() {
        return organizationRepo.findAll();
    }

    public Organization getRawOrganizationById( String organizationId ) {
        return getOrganizationByIdOrThrow( organizationId );
    }

    public Organization updateOrganization( String organizationId, OrganizationUpdateDto updateData ) {
        Organization existingOrganization = getOrganizationByIdOrThrow( organizationId );

        return organizationRepo.save( organizationDtoToEntityMapper( existingOrganization, updateData ) );
    }

    public Organization addOwnerToOrganization( String organizationId, String userId ) {
        Organization organization = getOrganizationByIdOrThrow( organizationId );

        Set<String> updatedOwners = organization.getOwners();
        updatedOwners.add( userId );

        Organization buildOrganization = organization.toBuilder()
                .owners( updatedOwners )
                .build();

        Organization savedOrganization = organizationRepo.save( buildOrganization );

        userService.addOrganizationToUser( userId, organizationId );

        return savedOrganization;
    }

    public Organization deleteOwnerFromOrganization( String organizationId, String ownerId ) {
        Organization organization = getOrganizationByIdOrThrow( organizationId );

        Set<String> updatedOwners = organization.getOwners().stream()
                .filter( id -> !id.equals( ownerId ) )
                .collect( Collectors.toSet() );

        Organization updatedOrganization = organization.toBuilder()
                .owners( updatedOwners )
                .build();


        if ( updatedOwners.isEmpty() ) {
            throw new IllegalStateException( "Organization must have at least one owner." );
        }

        return organizationRepo.save( updatedOrganization );
    }


    // === Create Methods ===

    public Organization createOrganization( OrganizationCreateDto organizationDto, AppUser user ) {
        Organization newOrganization = Organization.builder()
                .name( organizationDto.name() )
                .owners( Set.of( user.getId() ) )
                .description( organizationDto.description() )
                .website( organizationDto.website() )
                .build();

        Organization savedOrganization = organizationRepo.save( newOrganization );

        userService.addOrganizationToUser( user, savedOrganization.getId() );

        return savedOrganization;
    }

    // === Delete Methods ===

    public void deleteOrganizationById( String organizationId ) {

        Set<String> ownerIds = getOrganizationByIdOrThrow( organizationId ).getOwners();

        organizationRepo.deleteById( organizationId );

        for ( String ownerId : ownerIds ) {
            userService.removeOrganizationFromUser( ownerId, organizationId );
        }
    }

    // === Private Helper Methods ===

    private Organization getOrganizationByIdOrThrow( String organizationId ) {
        return organizationRepo.findById( organizationId )
                .orElseThrow( () -> new ResourceNotFoundException( "Organization not found with id: " + organizationId ) );
    }

    private Organization getOrganizationBySlugOrThrow( String organizationSlug ) {
        return organizationRepo.findBySlug( organizationSlug )
                .orElseThrow( () -> new ResourceNotFoundException( "Organization not found with slug: " + organizationSlug ) );
    }

    private OrganizationResponseDto organizationToDtoMapper( Organization organization ) {
        Set<AppUserDto> owners = userService.getAllUserDtosById( organization.getOwners() );

        return OrganizationResponseDto.builder()
                .name( organization.getName() )
                .owners( owners )
                .slug( organization.getSlug() )
                .description( organization.getDescription() )
                .website( organization.getWebsite() )
                .build();
    }

    private Organization organizationDtoToEntityMapper( Organization existingOrganization, OrganizationUpdateDto updatedOrganization ) {

        return existingOrganization.toBuilder()
                .name( updatedOrganization.name() != null ? updatedOrganization.name() : existingOrganization.getName() )
                .description( updatedOrganization.description() != null ? updatedOrganization.description() : existingOrganization.getDescription() )
                .website( updatedOrganization.website() != null ? updatedOrganization.website() : existingOrganization.getWebsite() )
                .build();
    }
}
