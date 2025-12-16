package org.eventbuddy.backend.services;

import lombok.RequiredArgsConstructor;
import org.eventbuddy.backend.configs.AdminConfig;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.UnauthorizedException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService extends DefaultOAuth2UserService {
    private final UserRepository userRepo;
    private final AdminConfig adminConfig;


    @Override
    public OAuth2User loadUser( OAuth2UserRequest userRequest ) {
        OAuth2User oAuthUser = super.loadUser( userRequest );

        String uniqueProviderId = getProviderIdByRequestAndUser( userRequest, oAuthUser );

        AppUser user = userRepo.findByProviderId( uniqueProviderId ).orElseGet( () ->
                createAndSaveUser( oAuthUser, userRequest )
        );

        return new DefaultOAuth2User( List.of( new SimpleGrantedAuthority( user.getRole().toString() ) ), oAuthUser.getAttributes(), "id" );
    }

    public boolean userExistsById( String userId ) {
        return userRepo.existsById( userId );
    }

    public AppUser getAppUserByAuthToken( OAuth2AuthenticationToken authToken ) {
        String providerId = getProviderIdByAuthToken( authToken );
        return getAppUserByProviderId( providerId );
    }

    public boolean isNotAuthenticated( OAuth2AuthenticationToken authToken ) {
        return authToken == null || !authToken.isAuthenticated();
    }


    public boolean isRequestUserOrSuperAdmin( OAuth2AuthenticationToken authToken, String userId ) {
        if ( isNotAuthenticated( authToken ) ) {
            throw new UnauthorizedException( "You are not logged in." );
        }

        AppUser user = getAppUserByAuthToken( authToken );

        return user.getId().equals( userId ) || user.getRole() == Role.SUPER_ADMIN;
    }

    private AppUser getAppUserByProviderId( String providerId ) {
        return userRepo.findByProviderId( providerId )
                .orElseThrow( () -> new RuntimeException( "User not found with providerId: " + providerId ) );
    }

    private AppUser createAndSaveUser( OAuth2User oAuthUser, OAuth2UserRequest userRequest ) {

        String uniqueProviderId = getProviderIdByRequestAndUser( userRequest, oAuthUser );

        UserSettings settings = UserSettings.builder()
                .userVisible( true )
                .showOrgas( true )
                .showAvatar( true )
                .build();

        String login = oAuthUser.getAttribute( "login" );
        String email = oAuthUser.getAttribute( "email" );
        String avatarUrl = oAuthUser.getAttribute( "avatar_url" );

        AppUser newUser = AppUser.builder()
                .providerId( uniqueProviderId )
                .name( login )
                .role( adminConfig.isAdmin( uniqueProviderId ) ? Role.SUPER_ADMIN : Role.USER )
                .email( email )
                .avatarUrl( avatarUrl )
                .userSettings( settings )
                .build();

        return userRepo.save( newUser );
    }

    private String getProviderIdByRequestAndUser( OAuth2UserRequest userRequest, OAuth2User oAuthUser ) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuthUser.getName();
        return provider + "_" + providerId;
    }

    private String getProviderIdByAuthToken( OAuth2AuthenticationToken authToken ) {
        OAuth2User user = authToken.getPrincipal();
        String providerId = user.getName();
        String provider = authToken.getAuthorizedClientRegistrationId();
        return provider + "_" + providerId;
    }


}
