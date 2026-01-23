package org.eventbuddy.backend.mockUser;

import org.eventbuddy.backend.configs.CustomOAuth2User;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext( WithCustomMockUser customUser ) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Map<String, Object> attributes = new HashMap<>();

        attributes.put( "id", customUser.id() );


        DefaultOAuth2User defaultOAuth2User = new DefaultOAuth2User(
                List.of( new SimpleGrantedAuthority( customUser.role() ) ),
                attributes,
                "id"
        );

        // AppUser erstellen wie im AuthService
        UserSettings settings = UserSettings.builder()
                .userVisible( true )
                .showOrgas( true )
                .showAvatar( true )
                .showEmail( true )
                .build();

        AppUser appUser = AppUser.builder()
                .id( customUser.id() )
                .providerId( customUser.registrationId() + "_" + customUser.id() )
                .name( "test-user" )
                .role( Role.valueOf( customUser.role().replace( "ROLE_", "" ) ) )
                .email( "test@test.com" )
                .userSettings( settings )
                .build();

        // CustomOAuth2User erstellen
        CustomOAuth2User principal = new CustomOAuth2User( defaultOAuth2User, appUser );

        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                customUser.registrationId()
        );

        context.setAuthentication( auth );

        return context;
    }

}
