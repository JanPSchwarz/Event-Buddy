package org.eventbuddy.backend.services;

import org.eventbuddy.backend.configs.AdminConfig;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.exceptions.UnauthorizedException;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WithCustomMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class AuthServiceTest {

    @Mock
    UserRepository mockUserRepo;

    @Mock
    AdminConfig mockAdminConfig;
    
    AuthService mockAuthService;

    @BeforeEach
    void setUp() {
        mockAuthService = new AuthService( mockUserRepo, mockAdminConfig );
    }

    @Test
    void loadUser() {
        OAuth2UserRequest userRequest = buildOAuth2UserRequest();

        AuthService spyAuthService = spy( mockAuthService );

        Map<String, Object> attributes = new HashMap<>();
        attributes.put( "id", "123" );

        OAuth2User mockOAuth2User = new DefaultOAuth2User( List.of(), attributes, "id" );

        String expectedProviderId = "github_123";

        AppUser existingUser = AppUser.builder()
                .providerId( expectedProviderId )
                .role( Role.USER )
                .build();

        doReturn( mockOAuth2User ).when( spyAuthService ).loadUser( userRequest );

        // Mock Repository-Aufruf für existierenden User
        when( mockUserRepo.findByProviderId( expectedProviderId ) ).thenReturn( Optional.of( existingUser ) );

        // WHEN
        OAuth2User result = spyAuthService.loadUser( userRequest );

        System.out.println( result );

        // THEN
        assertNotNull( result );
        assertEquals( "123", result.getName() );
//        assertTrue( result.getAuthorities().stream()
//                .anyMatch( auth -> auth.getAuthority().equals( "USER" ) ) );
        assertEquals( attributes, result.getAttributes() );

        verify( mockUserRepo ).findByProviderId( expectedProviderId );

    }

    @Test
    @DisplayName("Should return user when found by auth token")
    void getAppUserByAuthToken_shouldReturnTrueWhenSuccess() {
        // GIVEN
        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        String providerId = "github_123";

        AppUser expectedUser = AppUser.builder()
                .providerId( providerId )
                .build();

        when( mockUserRepo.findByProviderId( providerId ) ).thenReturn( Optional.of( expectedUser ) );

        // WHEN
        AppUser actualUser = mockAuthService.getAppUserByAuthToken( authToken );

        // THEN
        assertNotNull( actualUser );
        assertEquals( expectedUser.getProviderId(), actualUser.getProviderId() );
        verify( mockUserRepo ).findByProviderId( providerId );
    }

    @Test
    @DisplayName("Should throw when user not found by auth token")
    void getAppUserByAuthToken_shouldThrowWhenUserNotFound() {
        // GIVEN
        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        String providerId = "github_123";

        when( mockUserRepo.findByProviderId( providerId ) ).thenReturn( Optional.empty() );


        assertThatThrownBy( () -> {
            mockAuthService.getAppUserByAuthToken( authToken );
        } )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "User not found with providerId: " + providerId );

        verify( mockUserRepo ).findByProviderId( providerId );
    }

    @Test
    @DisplayName("Should throw when auth token null")
    void getAppUserByAuthToken_shouldThrowWhenAuthTokenNull() {
        // GIVEN
        OAuth2AuthenticationToken authToken = null;

        assertThatThrownBy( () -> {
            mockAuthService.getAppUserByAuthToken( authToken );
        } )
                .isInstanceOf( UnauthorizedException.class )
                .hasMessage( "User is not logged in." );
    }

    @Test
    @DisplayName("Should throw when auth token unauthenticated")
    void isRequestUserOrSuperAdmin_throwsWhenCalledUnauthenticated() {
        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        authToken.setAuthenticated( false );

        String falseUserId = "123";

        assertThatThrownBy( () -> {
            mockAuthService.isRequestUserOrSuperAdmin( authToken, falseUserId );
        } )
                .isInstanceOf( UnauthorizedException.class )
                .hasMessage( "You are not logged in." );
    }

    @Test
    @DisplayName("Should throw when auth token null")
    void isRequestUserOrSuperAdmin_throwsWhenCalledNull() {
        String falseUserId = "123";

        assertThatThrownBy( () -> {
            mockAuthService.isRequestUserOrSuperAdmin( null, falseUserId );
        } )
                .isInstanceOf( UnauthorizedException.class )
                .hasMessage( "You are not logged in." );
    }

    @Test
    @DisplayName("Should return true when user is request user")
    void isRequestUserOrSuperAdmin_trueWhenUserIsRequestUser() {

        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        String providerId = "github_123";

        String userId = "123";

        AppUser user = AppUser.builder()
                .id( userId )
                .providerId( providerId )
                .role( Role.USER )
                .build();

        when( mockUserRepo.findByProviderId( providerId ) ).thenReturn( Optional.of( user ) );

        assertTrue( mockAuthService.isRequestUserOrSuperAdmin( authToken, userId ) );

        verify( mockUserRepo ).findByProviderId( providerId );
    }

    @Test
    @DisplayName("Should return true when user is super admin")
    void isRequestUserOrSuperAdmin_trueWhenUserIsSuperAdmin() {

        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        String providerId = "github_123";

        String userId = "123";

        AppUser user = AppUser.builder()
                .id( "anotherId" )
                .role( Role.SUPER_ADMIN )
                .build();

        when( mockUserRepo.findByProviderId( providerId ) ).thenReturn( Optional.of( user ) );

        assertTrue( mockAuthService.isRequestUserOrSuperAdmin( authToken, userId ) );

        verify( mockUserRepo ).findByProviderId( providerId );
    }

    @Test
    @DisplayName("Should return true when user is super admin")
    void isSuperAdmin_shouldReturnTrue() {
        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        String providerId = "github_123";

        AppUser user = AppUser.builder()
                .role( Role.SUPER_ADMIN )
                .build();

        when( mockUserRepo.findByProviderId( providerId ) ).thenReturn( Optional.of( user ) );

        assertTrue( mockAuthService.isSuperAdmin( authToken ) );

        verify( mockUserRepo ).findByProviderId( providerId );
    }

    @Test
    @DisplayName("Should return false when user is not super admin")
    void isSuperAdmin_shouldReturnFalse() {
        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        String providerId = "github_123";

        AppUser user = AppUser.builder()
                .role( Role.USER )
                .build();

        when( mockUserRepo.findByProviderId( providerId ) ).thenReturn( Optional.of( user ) );

        assertFalse( mockAuthService.isSuperAdmin( authToken ) );

        verify( mockUserRepo ).findByProviderId( providerId );
    }

    @Test
    @DisplayName("Should throw when user is not authenticated")
    void isSuperAdmin_shouldThrowWhenNotAuthenticated() {
        OAuth2AuthenticationToken authToken = ( OAuth2AuthenticationToken )
                SecurityContextHolder.getContext().getAuthentication();

        authToken.setAuthenticated( false );

        assertThatThrownBy( () -> {
            mockAuthService.isSuperAdmin( authToken );
        } )
                .isInstanceOf( UnauthorizedException.class )
                .hasMessage( "You are not logged in." );

    }

    private OAuth2UserRequest buildOAuth2UserRequest() {
        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId( "github" )
                .clientId( "client-id" )
                .clientSecret( "client-secret" )
                .redirectUri( "redirect" )
                .authorizationUri( "https://github.com/login/oauth/authorize" )
                .tokenUri( "https://github.com/login/oauth/access_token" )
                .userInfoUri( "https://api.github.com/user" )
                .userNameAttributeName( "id" )
                .authorizationGrantType( AuthorizationGrantType.AUTHORIZATION_CODE )
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                null,
                null
        );

        return new OAuth2UserRequest( clientRegistration, accessToken );

    }
}