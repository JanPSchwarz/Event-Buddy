package org.eventbuddy.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.utils.IdService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    IdService idService;

    public SecurityConfig( IdService idService ) {
        this.idService = idService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
                .csrf( AbstractHttpConfigurer::disable )
                .cors( cors -> cors.configurationSource( corsConfigurationSource() ) )
                .authorizeHttpRequests( auth -> auth
                        // TBD: Adjust the security rules as needed
                        .requestMatchers( "/api/admin/**" ).hasAnyAuthority( Role.ADMIN.toString(), Role.SUPER_ADMIN.toString() )
                        .requestMatchers( "/api/admin/super/**" ).hasAuthority( Role.SUPER_ADMIN.toString() )
                        .anyRequest().permitAll()
                )
                .exceptionHandling( exceptions -> exceptions
                        .authenticationEntryPoint( ( request, response, authException ) -> {
                            response.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                            response.setContentType( "application/json" );
                            ErrorMessage errorMessage = new ErrorMessage( Instant.now().toString(), "Unauthorized", idService.generateErrorId(), HttpServletResponse.SC_UNAUTHORIZED );
                            ObjectMapper objectMapper = new ObjectMapper();
                            response.getWriter().write( objectMapper.writeValueAsString( errorMessage ) );
                        } )
                )
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
