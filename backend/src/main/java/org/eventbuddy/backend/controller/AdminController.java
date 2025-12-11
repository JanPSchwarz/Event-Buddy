package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrative operations")
public class AdminController {

    UserService userService;

    public AdminController( UserService userService ) {
        this.userService = userService;
    }

    @GetMapping("/super/get-all-users")
    @Operation(
            summary = "Get All Users (Super Admin only)",
            description = "Returns an array of all user accounts currently stored in the system."
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
            responseCode = "403",
            description = "User not authorized",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<List<AppUser>> getAllRawUsers() {
        return ResponseEntity.ok( userService.getAllRawUsers() );
    }

    @GetMapping("/super/{userId}")
    @Operation(
            summary = "Get raw user by ID (Super Admin only)",
            description = "Returns user for the user with the specified ID."
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public AppUser getRawUserById( @PathVariable String userId ) {
        return userService.getRawUserById( userId );
    }

    @PutMapping("/make-admin/{userId}")
    @Operation(
            summary = "Make User Admin (Admin only)",
            description = "Returns the updated user account after applying the change."
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
    @ApiResponse(
            responseCode = "403",
            description = "User not authorized",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<AppUser> makeUserAdmin( @PathVariable String userId ) {

        return ResponseEntity.ok( userService.makeUserAdmin( userId ) );
    }

    @PutMapping("/super/make-super-admin/{userId}")
    @Operation(
            summary = "Make User Super Admin (Super Admin only)",
            description = "Returns the updated user account after applying the change."
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
    @ApiResponse(
            responseCode = "403",
            description = "User not authorized",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<AppUser> makeUserSuperAdmin( @PathVariable String userId ) {

        return ResponseEntity.ok( userService.makeUserSuperAdmin( userId ) );
    }
}
