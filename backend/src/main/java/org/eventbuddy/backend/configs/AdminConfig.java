package org.eventbuddy.backend.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AdminConfig {

    private final List<String> adminIds;

    public AdminConfig( @Value("${app.admins}") String adminIds ) {
        this.adminIds = Arrays.asList( adminIds.split( "," ) );
    }

    public boolean isAdmin( String providerId ) {
        return adminIds.contains( providerId );
    }
}
