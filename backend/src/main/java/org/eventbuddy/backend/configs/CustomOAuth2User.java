package org.eventbuddy.backend.configs;

import lombok.Getter;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User defaultOAuth2User;

    @Getter
    private final AppUser user;

    public CustomOAuth2User( OAuth2User defaultOAuth2User, AppUser user ) {
        this.defaultOAuth2User = defaultOAuth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return defaultOAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return defaultOAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return defaultOAuth2User.getName();
    }
}
