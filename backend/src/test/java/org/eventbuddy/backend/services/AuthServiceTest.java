package org.eventbuddy.backend.services;

import org.eventbuddy.backend.configs.AdminConfig;
import org.eventbuddy.backend.configs.CustomOAuth2User;
import org.eventbuddy.backend.exceptions.UnauthorizedException;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.mockUser.WithCustomSuperAdmin;
import org.eventbuddy.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithCustomMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class AuthServiceTest {

    String authenticatedUserId;

    @Mock
    UserRepository mockUserRepo;

    @Mock
    AdminConfig mockAdminConfig;

    AuthService mockAuthService;

    @BeforeEach
    void setUp() {
        mockAuthService = new AuthService( mockUserRepo, mockAdminConfig );

        // Save annotated test user to userRepo
        CustomOAuth2User customOAuth2User = ( CustomOAuth2User ) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        authenticatedUserId = customOAuth2User.getUser().getId();
    }

    @Test
    @DisplayName("Returns true when user is request user")
    void isRequestUserOrSuperAdminOrThrow_userIsRequestUser_returnsTrue() {

        boolean result = mockAuthService.isRequestUserOrSuperAdminOrThrow( authenticatedUserId );

        assertTrue( result );
    }

    @Test
    @DisplayName("Throws 401 when user is not request user")
    void isRequestUserOrSuperAdminOrThrow_userIsNotRequestUser_throwsUnauthorizedException() {

        String nonExistingUserId = "666";

        assertThatThrownBy( () ->
                mockAuthService.isRequestUserOrSuperAdminOrThrow( nonExistingUserId )
        ).isInstanceOf( UnauthorizedException.class )
                .hasMessage( "You do not have permission to perform this action." );
    }

    @Test
    @DisplayName("Returns true when user is super admin")
    @WithCustomSuperAdmin
    void isRequestUserOrSuperAdminOrThrow_userIsSuperAdmin_returnsTrue() {
        String otherUserId = "other-user-id";

        boolean result = mockAuthService.isRequestUserOrSuperAdminOrThrow( otherUserId );

        assertTrue( result );
    }


}