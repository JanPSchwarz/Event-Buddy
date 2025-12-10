package org.eventbuddy.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserUpdateDto;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.stereotype.Service;

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

    public AppUser updateUserSettings( UserSettings newUserSettings, String userId ) {
        AppUser existingUser = getUserOrThrow( userId );

        AppUser updatedUser = existingUser.toBuilder()
                .userSettings( newUserSettings )
                .build();

        return userRepo.save( updatedUser );
    }

    public void deleteUserById( String userId ) {
        userRepo.deleteById( userId );
    }

    private AppUser getUserOrThrow( String userId ) {
        return userRepo.findById( userId ).orElseThrow( () ->
                new ResourceNotFoundException( "User not found with id: " + userId )
        );
    }

}
