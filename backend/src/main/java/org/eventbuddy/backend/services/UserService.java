package org.eventbuddy.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.AppUserUpdateDto;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepo;
    
    private final OrganizationService organizationService;

    public UserService( UserRepository userRepo, @Lazy OrganizationService organizationService ) {
        this.userRepo = userRepo;
        this.organizationService = organizationService;
    }

    // === Public User Methods (DTO) ===

    public AppUserDto getUserDtoById( String userId ) {
        AppUser user = getUserOrThrow( userId );

        if ( !user.getUserSettings().userVisible() ) {
            throw new ResourceNotFoundException( "User not found with id: " + userId );
        }

        return userToDtoMapper( user );
    }

    public Set<AppUserDto> getAllUserDtosById( Set<String> userIds ) {
        return userRepo.findAllById( userIds )
                .orElseThrow( () -> new ResourceNotFoundException( "No users found for the provided IDs" ) )
                .stream()
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

        AppUser updatedUser = existingUser.toBuilder()
                .name( updateData.name() != null ? updateData.name() : existingUser.getName() )
                .email( updateData.email() != null ? updateData.email() : existingUser.getEmail() )
                .userSettings( updateData.userSettings() != null ? updateData.userSettings() : existingUser.getUserSettings() )
                .build();

        return userRepo.save( updatedUser );
    }

    public AppUser addOrganizationToUser( AppUser user, String organizationId ) {
        Set<String> updatedOrganizations = new HashSet<>(
                user.getOrganizations() != null ? user.getOrganizations() : Set.of()
        );

        if ( updatedOrganizations.contains( organizationId ) ) {
            return user;
        }

        updatedOrganizations.add( organizationId );

        AppUser updatedUser = user.toBuilder()
                .organizations( updatedOrganizations )
                .build();

        return userRepo.save( updatedUser );
    }

    public AppUser addOrganizationToUser( String userId, String organizationId ) {
        AppUser user = getUserOrThrow( userId );
        return addOrganizationToUser( user, organizationId );
    }

    public AppUser removeOrganizationFromUser( AppUser user, String organizationId ) {

        if ( user.getOrganizations() == null || !user.getOrganizations().contains( organizationId ) ) {
            return user;
        }

        Set<String> updatedOrganizations = user.getOrganizations().stream()
                .filter( orgId -> !orgId.equals( organizationId ) )
                .collect( Collectors.toSet() );

        AppUser updatedUser = user.toBuilder()
                .organizations( updatedOrganizations )
                .build();

        return userRepo.save( updatedUser );
    }

    public AppUser removeOrganizationFromUser( String userId, String organizationId ) {
        AppUser user = getUserOrThrow( userId );
        return removeOrganizationFromUser( user, organizationId );
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

        // Remove user from all organizations they belong to
        Set<String> organizations = user.getOrganizations();

        if ( organizations != null ) {
            for ( String orgId : organizations ) {
                organizationService.deleteOwnerFromOrganization( orgId, userId );
            }
        }

        userRepo.deleteById( userId );
    }

    // === Private Helper Methods ===

    private AppUser getUserOrThrow( String userId ) {
        return userRepo.findById( userId ).orElseThrow( () ->
                new ResourceNotFoundException( "User not found with id: " + userId )
        );
    }

    private AppUserDto userToDtoMapper( AppUser user ) {

        List<String> organizationNames = user.getOrganizations() != null ? user.getOrganizations().stream()
                .map( organizationId -> organizationService.getOrganizationDtoById( organizationId ).name() )
                .toList()
                :
                null;

        return AppUserDto.builder()
                .name( user.getName() )
                .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                .organizationNames( organizationNames )
                .build();
    }
}
