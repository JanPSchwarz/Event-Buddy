package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.eventbuddy.backend.configs.annotations.IsAuthenticated;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.AppUserUpdateDto;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.services.AuthService;
import org.eventbuddy.backend.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "CRUD operations for users")
@Validated
public class UserController {

    AuthService authService;
    UserService userService;

    public UserController( AuthService authService, UserService userService ) {
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get an array of all users (dto's)",
            description = "Returns an array of all user dto's accounts currently stored in the system."
    )
    public List<AppUserDto> getAllUsers() {
        return userService.getAllUsersDtos();
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user dto by ID",
            description = "Returns user dto for the user with the specified ID."
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public AppUserDto getUserById( @PathVariable String userId ) {
        return userService.getUserDtoById( userId );
    }

    @PutMapping("/{userId}")
    @Operation(
            summary = "Update User Account (Requesting User / Super Admin only)",
            description = "Returns the updated user account after applying the changes."
    )
    @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied",
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
    @ApiResponse(
            responseCode = "409",
            description = "Conflict - Duplicate unique considered data",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @IsAuthenticated
    public ResponseEntity<AppUser> updateUser( @PathVariable String userId, @Valid @RequestBody AppUserUpdateDto updateUserDto ) {
        validateRequest( userId );

        return ResponseEntity.ok( userService.updateUser( updateUserDto, userId ) );
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete user account (Requesting User / Super Admin only)",
            description = "Deletes the user account."
    )
    @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied",
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
    public ResponseEntity<Void> deleteUser( @PathVariable String userId ) {

        validateRequest( userId );

        userService.deleteUserById( userId );

        return ResponseEntity.noContent().build();
    }

    private void validateRequest( String userId ) {

        userService.userExistsByIdOrThrow( userId );

        authService.isRequestUserOrSuperAdminOrThrow( userId );

    }
}
