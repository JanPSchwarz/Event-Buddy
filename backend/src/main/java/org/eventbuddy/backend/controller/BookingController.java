package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.UnauthorizedException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.booking.Booking;
import org.eventbuddy.backend.models.booking.BookingRequestDto;
import org.eventbuddy.backend.models.booking.BookingResponseDto;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.services.AuthService;
import org.eventbuddy.backend.services.BookingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@Tag(name = "Booking Controller", description = "CRUD operations for booking events")
@Validated
public class BookingController {

    private final AuthService authService;

    private final BookingService bookingService;

    public BookingController( AuthService authService, BookingService bookingService ) {
        this.authService = authService;
        this.bookingService = bookingService;
    }

    // === GET Endpoints ===

    @GetMapping("/byUser/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUser( @PathVariable String userId, OAuth2AuthenticationToken authToken ) {
        AppUser user = authService.getAppUserByAuthToken( authToken );

        if ( !user.getId().equals( userId ) && user.getRole() != Role.SUPER_ADMIN ) {
            throw new UnauthorizedException( "User not authorized to view bookings of other users." );
        }

        List<BookingResponseDto> bookingResponseDto = bookingService.getBookingsByUser( userId );

        return ResponseEntity.ok( bookingResponseDto );
    }

    // === POST Endpoints ===
    @PostMapping("/makeBooking")
    @Operation(
            summary = "Make a booking for an event"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request, invalid booking data",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Not authorized to book tickets for this event",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Associated Event not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Not enough tickets available for the booking",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<BookingResponseDto> makeBooking( @RequestBody @Valid BookingRequestDto bookingRequestDto, OAuth2AuthenticationToken authToken ) {
        boolean notLoggedIn = authService.isNotAuthenticated( authToken );

        if ( notLoggedIn ) {
            throw new UnauthorizedException( "User must be logged in to make a booking." );
        }

        BookingResponseDto bookingResponseDto = bookingService.makeBooking( bookingRequestDto );

        return ResponseEntity.ok( bookingResponseDto );
    }


    // === DELETE Endpoints ===

    @DeleteMapping("/{bookingId}")
    @Operation(
            summary = "Delete a booking by its ID"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Not authorized to delete this booking",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Booking/User not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    public ResponseEntity<Void> deleteBookingById( @PathVariable String bookingId, OAuth2AuthenticationToken authToken ) {
        AppUser user = authService.getAppUserByAuthToken( authToken );

        Booking bookingToDelete = bookingService.getRawBookingById( bookingId );

        if ( !bookingToDelete.getUserId().equals( user.getId() ) && user.getRole() != Role.SUPER_ADMIN ) {
            throw new UnauthorizedException( "You are not authorized to delete this booking." );
        }

        bookingService.deleteBookingById( bookingId );

        return ResponseEntity.noContent().build();
    }

    // === Helper Methods ===

}
