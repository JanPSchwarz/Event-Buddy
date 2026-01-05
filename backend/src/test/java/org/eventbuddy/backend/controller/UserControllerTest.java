package org.eventbuddy.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eventbuddy.backend.TestcontainersConfiguration;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.mockUser.WithCustomSuperAdmin;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.AppUserUpdateDto;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@WithCustomMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

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
    @DisplayName("Return user dto when found")
    void getAllUsers() throws Exception {
        AppUser savedUser = userRepo.findById( savedUserId ).orElseThrow();
        AppUserDto dtoFromSavedUser = AppUserDto.builder()
                .name( savedUser.getName() )
                .email( savedUser.getEmail() )
                .avatarUrl( savedUser.getAvatarUrl() )
                .build();

        List<AppUserDto> expectedUsers = List.of( dtoFromSavedUser );

        String expectedJson = objectMapper.writeValueAsString( expectedUsers );

        mockMvc.perform( get( "/api/users/all" ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Return empty list when no users found")
    void getAllUsers_returnEmpty() throws Exception {
        userRepo.deleteAll();

        List<AppUserDto> expectedUsers = List.of();

        String expectedJson = objectMapper.writeValueAsString( expectedUsers );

        mockMvc.perform( get( "/api/users/all" ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Returns user dto when found by id")
    void getUserById() throws Exception {
        AppUser savedUser = userRepo.findById( savedUserId ).orElseThrow();
        AppUserDto dtoFromSavedUser = AppUserDto.builder()
                .name( savedUser.getName() )
                .email( savedUser.getEmail() )
                .avatarUrl( savedUser.getAvatarUrl() )
                .build();

        String expectedJson = objectMapper.writeValueAsString( dtoFromSavedUser );

        mockMvc.perform( get( "/api/users/" + savedUserId ) )
                .andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Throws 404 when user not found by id")
    void getUserById_throws404WhenNotFound() throws Exception {
        userRepo.deleteAll();

        String nonExistingId = "nonExistingId";

        mockMvc.perform( get( "/api/users/" + nonExistingId ) )
                .andExpect( status().isNotFound() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( jsonPath( "$.error" ).value( "User not found with id: " + nonExistingId ) );
    }

    @Test
    @DisplayName("Throws 404 when user not visible")
    void getUserById_throws404WhenNotVisible() throws Exception {
        AppUser savedUser = userRepo.findById( savedUserId ).orElseThrow();

        AppUser invisibleUser = savedUser.toBuilder()
                .userSettings( UserSettings.builder().userVisible( false ).build() )
                .build();

        userRepo.save( invisibleUser );

        mockMvc.perform( get( "/api/users/" + invisibleUser.getId() ) )
                .andExpect( status().isNotFound() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( jsonPath( "$.error" ).value( "User not found with id: " + invisibleUser.getId() ) );
    }

    @Test
    @DisplayName("Should return updated user")
    void updateUser() throws Exception {
        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .name( "Updated Name" )
                .build();

        String requestBody = objectMapper.writeValueAsString( updateData );

        mockMvc.perform( put( "/api/users/" + savedUserId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( requestBody )
                )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( updateData.name() ) )
                .andExpect( jsonPath( "$.id" ).value( savedUserId ) );
    }

    @Test
    @DisplayName("Should throw 404 when user does not exist")
    void updateUser_throws404() throws Exception {
        String nonExistingId = "nonExistingId";

        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .name( "Updated Name" )
                .build();

        String requestBody = objectMapper.writeValueAsString( updateData );

        mockMvc.perform( put( "/api/users/" + nonExistingId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( requestBody )
                )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "User does not exist." ) );
    }

    @Test
    @DisplayName("Should throw 401 when user is not authenticated")
    void updateUser_throws401WhenNotAuthenticated() throws Exception {

        oAuth2Token.setAuthenticated( false );

        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .name( "Updated Name" )
                .build();

        String requestBody = objectMapper.writeValueAsString( updateData );

        mockMvc.perform( put( "/api/users/" + savedUserId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( requestBody )
                )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not logged in." ) );
    }

    @Test
    @DisplayName("Should throw 401 when user is not authorized")
    void updateUser_throws401WhenNotAuthorized() throws Exception {
        AppUser otherUser = AppUser.builder()
                .name( "Other User" )
                .email( "example@example.com" )
                .id( "someId" )
                .build();

        userRepo.save( otherUser );

        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .name( "Updated other name" )
                .build();

        String requestBody = objectMapper.writeValueAsString( updateData );

        mockMvc.perform( put( "/api/users/" + otherUser.getId() )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( requestBody )
                )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not allowed to perform this action." ) );
    }

    @Test
    @DisplayName("Should return updated foreign user when super admin")
    @WithCustomSuperAdmin
    void updateUser_shouldReturnUserWhenCalledBySuperAdmin() throws Exception {
        AppUser otherUser = AppUser.builder()
                .name( "Other User" )
                .email( "example@example.com" )
                .id( "someId" )
                .build();

        AppUser savedOtherUser = userRepo.save( otherUser );

        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .name( "Updated other name" )
                .build();

        String requestBody = objectMapper.writeValueAsString( updateData );

        mockMvc.perform( put( "/api/users/" + otherUser.getId() )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( requestBody )
                )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( updateData.name() ) )
                .andExpect( jsonPath( "$.id" ).value( savedOtherUser.getId() ) );
    }

    @Test
    @DisplayName("Should return nothing when deleted")
    void deleteUser() throws Exception {
        mockMvc.perform( delete( "/api/users/" + savedUserId ) )
                .andExpect( status().isNoContent() );
    }

    @Test
    @DisplayName("Should throw 401 when not authorized")
    void deleteUser_returns404WhenNotAuthorized() throws Exception {
        AppUser otherUser = AppUser.builder()
                .name( "Other User" )
                .email( "example@example.com" )
                .id( "someId" )
                .build();

        AppUser savedOtherUser = userRepo.save( otherUser );

        mockMvc.perform( delete( "/api/users/" + savedOtherUser.getId() ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not allowed to perform this action." ) );
    }

    @Test
    @DisplayName("Should allow when delete other user when super admin")
    @WithCustomSuperAdmin
    void deleteUser_shouldAllowSuperAdmin() throws Exception {
        AppUser otherUser = AppUser.builder()
                .name( "Other User" )
                .email( "example@example.com" )
                .id( "someId" )
                .build();

        AppUser savedOtherUser = userRepo.save( otherUser );

        mockMvc.perform( delete( "/api/users/" + savedOtherUser.getId() ) )
                .andExpect( status().isNoContent() );
    }
}