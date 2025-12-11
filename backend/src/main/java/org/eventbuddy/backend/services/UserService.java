package org.eventbuddy.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.AppUserUpdateDto;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public AppUser updateUser( AppUserUpdateDto updateData, String userId ) {

        AppUser existingUser = getUserOrThrow( userId );

        AppUser updatedUser = existingUser.toBuilder()
                .name( updateData.name() != null ? updateData.name() : existingUser.getName() )
                .email( updateData.email() != null ? updateData.email() : existingUser.getEmail() )
                .userSettings( updateData.userSettings() != null ? updateData.userSettings() : existingUser.getUserSettings() )
                .build();

        return userRepo.save( updatedUser );
    }

    public void deleteUserById( String userId ) {
        userRepo.deleteById( userId );
    }

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

    public AppUserDto getUserById( String userId ) {
        AppUser user = getUserOrThrow( userId );

        if ( !user.getUserSettings().userVisible() ) {
            throw new ResourceNotFoundException( "User not found with id: " + userId );
        }

        return AppUserDto.builder()
                .name( user.getName() )
                .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                .build();
    }

    public List<AppUserDto> getAllUsers() {

        List<AppUser> users = userRepo.findAll();

        return users.stream()
                .filter(
                        user -> !user.getUserSettings().userVisible() )
                .map( user -> AppUserDto.builder()
                        .name( user.getName() )
                        .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                        .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                        .build() )
                .toList();
    }

    public List<AppUser> getAllRawUsers() {
        return userRepo.findAll();
    }

    public AppUser getRawUserById( String userId ) {
        return getUserOrThrow( userId );
    }

    private AppUser getUserOrThrow( String userId ) {
        return userRepo.findById( userId ).orElseThrow( () ->
                new ResourceNotFoundException( "User not found with id: " + userId )
        );
    }

}
