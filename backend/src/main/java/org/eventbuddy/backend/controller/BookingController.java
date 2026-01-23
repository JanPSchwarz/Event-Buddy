package org.eventbuddy.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.eventbuddy.backend.configs.annotations.IsAuthenticated;
import org.eventbuddy.backend.models.booking.Booking;
import org.eventbuddy.backend.models.booking.BookingRequestDto;
import org.eventbuddy.backend.models.booking.BookingResponseDto;
import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.services.AuthService;
import org.eventbuddy.backend.services.BookingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @IsAuthenticated
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUser( @PathVariable String userId ) {

        authService.isRequestUserOrSuperAdminOrThrow( userId );

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
    @IsAuthenticated
    public ResponseEntity<BookingResponseDto> makeBooking( @RequestBody @Valid BookingRequestDto bookingRequestDto ) {
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
            description = "Booking/User not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorMessage.class)
            )
    )
    @IsAuthenticated
    public ResponseEntity<Void> deleteBookingById( @PathVariable String bookingId ) {
        Booking bookingToDelete = bookingService.getRawBookingById( bookingId );

        authService.isRequestUserOrSuperAdminOrThrow( bookingToDelete.getUserId() );

        bookingService.deleteBookingById( bookingId );

        return ResponseEntity.noContent().build();
    }
}
