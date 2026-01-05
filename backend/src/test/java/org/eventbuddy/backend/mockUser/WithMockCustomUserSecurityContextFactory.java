package org.eventbuddy.backend.mockUser;

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


        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of( new SimpleGrantedAuthority( customUser.role() ) ),
                attributes,
                "id"
        );

        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                customUser.registrationId()
        );

        context.setAuthentication( auth );

        return context;
    }

}
