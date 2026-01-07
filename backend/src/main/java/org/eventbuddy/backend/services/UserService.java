package org.eventbuddy.backend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.AppUserUpdateDto;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final OrganizationRepository organizationRepo;

    // === Public User Methods (DTO) ===

    public AppUserDto getUserDtoById( String userId ) {
        AppUser user = getUserOrThrow( userId );

        if ( !user.getUserSettings().userVisible() ) {
            throw new ResourceNotFoundException( "User not found with id: " + userId );
        }

        return userToDtoMapper( user );
    }

    public Set<AppUserDto> getAllUserDtosById( Set<String> userIds ) {
        List<AppUser> users = userRepo.findAllById( userIds );

        if ( users.isEmpty() ) {
            throw new ResourceNotFoundException( "No users found for the provided IDs" );
        }

        return users.stream()
                .filter( user -> user.getUserSettings().userVisible() )
                .map( this::userToDtoMapper )
                .collect( Collectors.toSet() );
    }

    public List<AppUserDto> getAllUsersDtos() {
        return userRepo.findAll()
                .stream()
                .filter( user -> user.getUserSettings().userVisible() )
                .map( this::userToDtoMapper )
                .toList();
    }

    public boolean userExistsById( String userId ) {
        return userRepo.existsById( userId );
    }

    // === Raw User Methods (Admins only) ===

    public AppUser getRawUserById( String userId ) {
        return getUserOrThrow( userId );
    }

    public List<AppUser> getAllRawUsers() {
        return userRepo.findAll();
    }

    // === Update Methods ===

    public AppUser updateUser( AppUserUpdateDto updateData, String userId ) {
        AppUser existingUser = getUserOrThrow( userId );

        // Prevent setting user as not visible while being part of organizations
        if ( updateData.userSettings() != null && !updateData.userSettings().userVisible() && existingUser.getOrganizations() != null && !existingUser.getOrganizations().isEmpty() ) {
            throw new IllegalStateException( "Cannot set user as not visible while being part of organizations. Please remove the user from all organizations first." );
        }

        AppUser updatedUser = existingUser.toBuilder()
                .name( updateData.name() != null ? updateData.name() : existingUser.getName() )
                .email( updateData.email() != null ? updateData.email() : existingUser.getEmail() )
                .userSettings( updateData.userSettings() != null ? updateData.userSettings() : existingUser.getUserSettings() )
                .build();

        return userRepo.save( updatedUser );
    }

    // === Role Management ===

    public AppUser makeUserAdmin( String userId ) {
        AppUser user = getUserOrThrow( userId );

        AppUser updatedUser = user.toBuilder()
                .role( Role.ADMIN )
                .build();

        return userRepo.save( updatedUser );
    }

    public AppUser makeUserSuperAdmin( String userId ) {
        AppUser user = getUserOrThrow( userId );

        AppUser updatedUser = user.toBuilder()
                .role( Role.SUPER_ADMIN )
                .build();

        return userRepo.save( updatedUser );
    }

    // === Delete Methods ===

    public void deleteUserById( String userId ) {

        AppUser user = getUserOrThrow( userId );

        userRepo.deleteById( userId );

        // Remove user from all organizations they belong to
        Set<String> organizations = user.getOrganizations();

        if ( organizations != null ) {
            for ( String orgId : organizations ) {
                removeOwnerFromOrganization( orgId, userId );
            }
        }
    }

    // === Private Helper Methods ===

    private AppUser getUserOrThrow( String userId ) {
        return userRepo.findById( userId ).orElseThrow( () ->
                new ResourceNotFoundException( "User not found with id: " + userId )
        );
    }

    private AppUserDto userToDtoMapper( AppUser user ) {

        if ( !user.getUserSettings().showOrgas() || user.getOrganizations() == null ) {
            return AppUserDto.builder()
                    .name( user.getName() )
                    .id( user.getId() )
                    .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                    .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                    .build();
        }

        List<Organization> organizations = organizationRepo.findAllById( user.getOrganizations().stream().toList() );

        if ( organizations.isEmpty() ) {
            throw new ResourceNotFoundException( "One or more organizations not found for user with id: " + user.getId() );
        }

        List<OrganizationResponseDto> organizationsDtos = organizations.stream()
                .map( this::organizationToDtoMapper )
                .toList();

        return AppUserDto.builder()
                .name( user.getName() )
                .id( user.getId() )
                .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                .organizations( organizationsDtos )
                .build();
    }

    private Organization removeOwnerFromOrganization( String orgId, String userId ) {
        Organization organization = organizationRepo.findById( orgId ).orElseThrow(
                () -> new ResourceNotFoundException( "Organization not found with id: " + orgId )
        );

        Set<String> updatedOwners = organization.getOwners().stream()
                .filter( ownerId -> !ownerId.equals( userId ) )
                .collect( Collectors.toSet() );

        Organization updatedOrganization = organization.toBuilder()
                .owners( updatedOwners )
                .build();

        return organizationRepo.save( updatedOrganization );
    }

    private OrganizationResponseDto organizationToDtoMapper( Organization organization ) {

        List<AppUser> owners = userRepo.findAllById( organization.getOwners() );

        if ( owners.isEmpty() || owners.size() != organization.getOwners().size() ) {
            throw new ResourceNotFoundException( "One or more organization owners not found." );
        }

        
        Set<AppUserDto> ownersDtos = owners.stream()
                .map( user ->
                        AppUserDto.builder()
                                .name( user.getName() )
                                .id( user.getId() )
                                .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                                .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                                .build()
                )
                .collect( Collectors.toSet() );

        return OrganizationResponseDto.builder()
                .name( organization.getName() )
                .description( organization.getDescription() )
                .website( organization.getWebsite() )
                .slug( organization.getSlug() )
                .location( organization.getLocation() )
                .contact( organization.getContact() )
                .owners( ownersDtos )
                .build();
    }


}
