package org.eventbuddy.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpaController.class)
class SpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void forward_shouldForwardToIndexHtml_whenRootPath() throws Exception {
        // Root path is handled by Spring Boot's WelcomePageHandlerMapping
        // which forwards to index.html (without leading slash)
        mockMvc.perform( get( "/" ) )
                .andExpect( status().isOk() )
                .andExpect( forwardedUrl( "index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenSingleLevelPath() throws Exception {
        mockMvc.perform( get( "/events" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenNestedPath() throws Exception {
        mockMvc.perform( get( "/event/123" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenDeeplyNestedPath() throws Exception {
        mockMvc.perform( get( "/event/123/orga/456" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenPathWithHyphens() throws Exception {
        mockMvc.perform( get( "/my-orga/123" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenPathWithUnderscores() throws Exception {
        mockMvc.perform( get( "/my_orga/123" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain( HttpSecurity http ) throws Exception {
            return http
                    .csrf( csrf -> csrf.disable() )
                    .authorizeHttpRequests( auth -> auth.anyRequest().permitAll() )
                    .build();
        }
    }
}