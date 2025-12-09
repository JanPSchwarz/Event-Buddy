package org.eventbuddy.backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
                .csrf( AbstractHttpConfigurer::disable )
                .cors( cors -> cors.configurationSource( corsConfigurationSource() ) )
                .authorizeHttpRequests( auth -> auth
                        // TBD: Adjust the security rules as needed
                        .anyRequest().permitAll() )
                .logout( logout -> logout.logoutSuccessUrl( "http://localhost:5173" ) )
                .oauth2Login( o -> o
                        .defaultSuccessUrl( "http://localhost:5173", true ) );

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsCustomizer = new CorsConfiguration();
        corsCustomizer.addAllowedOrigin( "http://localhost:5173" );
        corsCustomizer.addAllowedHeader( "*" );
        corsCustomizer.setAllowedMethods( List.of( "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE" ) );
        corsCustomizer.setAllowCredentials( true );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration( "/api/**", corsCustomizer );
        return source;
    }
}
