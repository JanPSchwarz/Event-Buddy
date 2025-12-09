package org.eventbuddy.backend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eventbuddy.backend.exceptions.UnauthorizedException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Get current authenticated user information")
@Validated
public class AuthController {

    UserService userService;

    public AuthController( UserService userService ) {
        this.userService = userService;
    }


    @GetMapping("/getMe")
    @Operation(
            summary = "Get current user",
            description = "Returns the currently authenticated user's information."
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<AppUser> getMe( OAuth2AuthenticationToken authentication ) {

        if ( authentication == null || !authentication.isAuthenticated() ) {
            throw new UnauthorizedException( "You are not logged in." );
        }

        OAuth2User user = authentication.getPrincipal();
        String provider = authentication.getAuthorizedClientRegistrationId();
        String providerId = provider + "_" + user.getName();

        AppUser appUser = userService.getAppUserByProviderId( providerId );

        return ResponseEntity.ok( appUser );
    }
}
