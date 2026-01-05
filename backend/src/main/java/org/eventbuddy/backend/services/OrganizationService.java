package org.eventbuddy.backend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationCreateDto;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.models.organization.OrganizationUpdateDto;
import org.eventbuddy.backend.repos.ImageRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepo;

    private final UserRepository userRepo;

    private final ImageRepository imageRepo;

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

    public OrganizationResponseDto getOrganizationDtoBySlug( String organizationName ) {
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

        AppUser user = userRepo.findById( userId )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found with id: " + userId ) );

        Set<String> updatedOwners = new HashSet<>( organization.getOwners() );
        updatedOwners.add( user.getId() );

        Organization buildOrganization = organization.toBuilder()
                .owners( updatedOwners )
                .build();

        Organization savedOrganization = organizationRepo.save( buildOrganization );

        addOrganizationToUser( organizationId, userId );

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

        removeOrganizationFromUser( organizationId, ownerId );

        return organizationRepo.save( updatedOrganization );
    }


    // === Create Methods ===

    public Organization createOrganization( OrganizationCreateDto organizationDto, AppUser user, String imageId ) {

        Organization newOrganization = Organization.builder()
                .name( organizationDto.name() )
                .owners( Set.of( user.getId() ) )
                .description( organizationDto.description() )
                .website( organizationDto.website() )
                .imageId( imageId )
                .location( organizationDto.location() )
                .contact( organizationDto.contact() )
                .build();

        Organization savedOrganization = organizationRepo.save( newOrganization );

        addOrganizationToUser( savedOrganization.getId(), user.getId() );

        return savedOrganization;
    }

    // === Delete Methods ===

    public void deleteOrganizationById( String organizationId ) {

        Organization organization = getOrganizationByIdOrThrow( organizationId );

        if ( organization.getImageId() != null ) {
            imageRepo.deleteById( organization.getImageId() );
        }

        Set<String> ownerIds = organization.getOwners();


        for ( String ownerId : ownerIds ) {
            removeOrganizationFromUser( organizationId, ownerId );
        }

        organizationRepo.deleteById( organizationId );
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


    private AppUser addOrganizationToUser( String organizationId, String userId ) {

        AppUser user = userRepo.findById( userId ).orElseThrow( () -> new ResourceNotFoundException( "User not found with id: " + userId ) );

        Set<String> updatedOrganizations = new HashSet<>( user.getOrganizations() != null ? user.getOrganizations() : Set.of() );

        updatedOrganizations.add( organizationId );

        UserSettings updatedSettings = user.getUserSettings().toBuilder()
                .userVisible( true )
                .build();

        AppUser updatedUser = user.toBuilder()
                .organizations( updatedOrganizations )
                .userSettings( updatedSettings )
                .build();

        return userRepo.save( updatedUser );
    }

    private AppUser removeOrganizationFromUser( String organizationId, String userId ) {
        AppUser user = userRepo.findById( userId ).orElseThrow( () ->
                new ResourceNotFoundException( "User not found with id: " + userId ) );

        if ( user.getOrganizations() == null || user.getOrganizations().isEmpty() ) {
            throw new IllegalStateException( "User with id " + userId + " is not part of any organizations." );
        }

        Set<String> updatedOrganizations = user.getOrganizations().stream()
                .filter( orgId -> !orgId.equals( organizationId ) )
                .collect( Collectors.toSet() );

        AppUser updatedUser = user.toBuilder()
                .organizations( updatedOrganizations )
                .build();

        return userRepo.save( updatedUser );
    }

    private AppUserDto userToDtoMapper( AppUser user ) {

        return AppUserDto.builder()
                .name( user.getName() )
                .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                .build();
    }

    private Organization organizationDtoToEntityMapper( Organization existingOrganization, OrganizationUpdateDto updatedOrganization ) {

        return existingOrganization.toBuilder()
                .name( updatedOrganization.name() != null ? updatedOrganization.name() : existingOrganization.getName() )
                .description( updatedOrganization.description() != null ? updatedOrganization.description() : existingOrganization.getDescription() )
                .website( updatedOrganization.website() != null ? updatedOrganization.website() : existingOrganization.getWebsite() )
                .location( updatedOrganization.location() != null ? updatedOrganization.location() : existingOrganization.getLocation() )
                .contact( updatedOrganization.contact() != null ? updatedOrganization.contact() : existingOrganization.getContact() )
                .build();
    }


    private OrganizationResponseDto organizationToDtoMapper( Organization organization ) {
        List<AppUser> owners = userRepo.findAllById( organization.getOwners() ).orElseThrow(
                () -> new ResourceNotFoundException( "One or more organization owners not found." )
        );

        Set<AppUserDto> ownersDtos = owners.stream()
                .map( this::userToDtoMapper )
                .collect( Collectors.toSet() );

        return OrganizationResponseDto.builder()
                .name( organization.getName() )
                .owners( ownersDtos )
                .slug( organization.getSlug() )
                .description( organization.getDescription() )
                .website( organization.getWebsite() )
                .imageId( organization.getImageId() )
                .location( organization.getLocation() )
                .contact( organization.getContact() )
                .build();
    }
}
