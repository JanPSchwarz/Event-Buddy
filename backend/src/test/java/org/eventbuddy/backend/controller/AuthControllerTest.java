package org.eventbuddy.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eventbuddy.backend.TestcontainersConfiguration;
import org.eventbuddy.backend.configs.CustomOAuth2User;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@WithCustomMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    AppUser authenticatedUser;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        CustomOAuth2User customOAuth2User = ( CustomOAuth2User ) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        authenticatedUser = userRepo.save( customOAuth2User.getUser() );
    }

    @Test
    @DisplayName("Should return current authenticated user when user is logged in")
    void getMe() throws Exception {

        String expectedJson = objectMapper.writeValueAsString( authenticatedUser );

        mockMvc.perform( get( "/api/auth/getMe" ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( content().string( expectedJson ) );
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void getMe_notAuthenticated() throws Exception {
        SecurityContextHolder.getContext().getAuthentication().setAuthenticated( false );

        mockMvc.perform( get( "/api/auth/getMe" ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( jsonPath( "$.error" ).value( "You are not logged in or not allowed to perform this Action." ) );
    }
}