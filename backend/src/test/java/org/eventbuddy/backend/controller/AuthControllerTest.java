package org.eventbuddy.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eventbuddy.backend.TestcontainersConfiguration;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    OAuth2AuthenticationToken oAuth2Token;

    String savedUserId;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        SecurityContext authContext = SecurityContextHolder.getContext();
        oAuth2Token = ( OAuth2AuthenticationToken ) authContext.getAuthentication();

        String provider = oAuth2Token.getAuthorizedClientRegistrationId();

        String authenticatedUserRole = oAuth2Token.getAuthorities().iterator().next().getAuthority();
        String providerId = provider + "_" + oAuth2Token.getName();
        Role role = Role.valueOf( authenticatedUserRole.replace( "ROLE_", "" ) );

        UserSettings userSettings = UserSettings.builder()
                .userVisible( true )
                .showAvatar( true )
                .showOrgas( true )
                .showEmail( true )
                .build();

        AppUser testUser = AppUser.builder()
                .role( role )
                .name( "testName" )
                .providerId( providerId )
                .userSettings( userSettings )
                .build();

        AppUser savedUser = userRepo.save( testUser );

        savedUserId = savedUser.getId();
    }

    @Test
    @DisplayName("Should return current authenticated user when user is logged in")
    void getMe() throws Exception {

        AppUser expectedUser = userRepo.findById( savedUserId ).orElseThrow();

        String expectedJson = objectMapper.writeValueAsString( expectedUser );

        mockMvc.perform( get( "/api/auth/getMe" ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( content().string( expectedJson ) );
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void getMe_notAuthenticated() throws Exception {
        oAuth2Token.setAuthenticated( false );

        mockMvc.perform( get( "/api/auth/getMe" ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( jsonPath( "$.error" ).value( "You are not logged in." ) );
    }

    @Test
    @DisplayName("Should return 404 when authenticated user is not found in database")
    void getMe_ShouldReturn404_WhenUserNotFoundInDatabase() throws Exception {
        userRepo.deleteById( savedUserId );

        mockMvc.perform( get( "/api/auth/getMe" ) )
                .andExpect( status().isNotFound() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) );

    }
}