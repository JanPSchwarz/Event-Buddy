package org.eventbuddy.backend.services;

import lombok.RequiredArgsConstructor;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepo;


    @Override
    public OAuth2User loadUser( OAuth2UserRequest userRequest ) {
        OAuth2User oAuthUser = super.loadUser( userRequest );

        String uniqueProviderId = getProviderId( userRequest, oAuthUser );

        // create user if not exists
        AppUser user = userRepo.findByProviderId( uniqueProviderId ).orElseGet( () ->
                createAndSaveUser( oAuthUser, userRequest )
        );

        return oAuthUser;
    }

    public AppUser getAppUserByProviderId( String providerId ) {
        return userRepo.findByProviderId( providerId )
                .orElseThrow( () -> new RuntimeException( "User not found with providerId: " + providerId ) );
    }

    private AppUser createAndSaveUser( OAuth2User oAuthUser, OAuth2UserRequest userRequest ) {

        String uniqueProviderId = getProviderId( userRequest, oAuthUser );

        UserSettings settings = UserSettings.builder()
                .userVisible( true )
                .showOrgas( true )
                .showAvatar( true )
                .build();

        AppUser newUser = AppUser.builder()
                .providerId( uniqueProviderId )
                .name( oAuthUser.getAttribute( "login" ) )
                .role( Role.USER )
                .email( oAuthUser.getAttribute( "email" ) )
                .avatarUrl( oAuthUser.getAttribute( "avatar_url" ) )
                .userSettings( settings )
                .build();

        return userRepo.save( newUser );
    }

    private String getProviderId( OAuth2UserRequest userRequest, OAuth2User oAuthUser ) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuthUser.getName();
        return provider + "_" + providerId;
    }
}
