package org.eventbuddy.backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
                .csrf( AbstractHttpConfigurer::disable )
                .authorizeHttpRequests( auth -> auth
                        // TBD: Adjust the security rules as needed
                        .anyRequest().permitAll() )
                .logout( logout -> logout.logoutSuccessUrl( "http://localhost:5173" ) )
                .oauth2Login( o -> o
                        .defaultSuccessUrl( "http://localhost:5173", true ) );

        return http.build();
    }
}
