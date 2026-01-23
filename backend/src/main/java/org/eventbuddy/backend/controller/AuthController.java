package org.eventbuddy.backend.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.configs.CustomOAuth2User;
import org.eventbuddy.backend.configs.annotations.IsAuthenticated;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.services.AuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Get current authenticated user information")
@Validated
public class AuthController {

    AuthService authService;

    public AuthController( AuthService authService ) {
        this.authService = authService;
    }


    @GetMapping("/getMe")
    @Operation(
            summary = "Get current user",
            description = "Returns the currently authenticated user's information."
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @IsAuthenticated
    public ResponseEntity<AppUser> getMe( @AuthenticationPrincipal CustomOAuth2User user ) {

        return ResponseEntity.ok( user.getUser() );
    }
}
