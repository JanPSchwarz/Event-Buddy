package org.eventbuddy.backend.services;

import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.booking.Booking;
import org.eventbuddy.backend.models.booking.BookingRequestDto;
import org.eventbuddy.backend.models.booking.BookingResponseDto;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.repos.BookingRepository;
import org.eventbuddy.backend.repos.EventRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private BookingService bookingService;

    private Organization testOrganization;
    private Event testEvent;
    private BookingRequestDto bookingRequestDto;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testOrganization = Organization.builder()
                .id( "org-1" )
                .name( "Test Organization" )
                .slug( "test-org" )
                .build();

        testEvent = Event.builder()
                .id( "event-1" )
                .title( "Test Event" )
                .maxTicketCapacity( 100 )
                .freeTicketCapacity( 50 )
                .bookedTicketsCount( 10 )
                .eventOrganization( testOrganization )
                .eventDateTime( Instant.now().plus( 12, ChronoUnit.DAYS ) )
                .ticketAlarm( false )
                .isSoldOut( false )
                .build();

        bookingRequestDto = BookingRequestDto.builder()
                .eventId( "event-1" )
                .userId( "user-1" )
                .name( "John Doe" )
                .numberOfTickets( 5 )
                .build();

        testBooking = Booking.builder()
                .userId( "user-1" )
                .name( "John Doe" )
                .numberOfTickets( 5 )
                .event( testEvent )
                .id( "booking-1" )
                .build();
    }

    @Test
    @DisplayName("Returns booking dto when created")
    void makeBooking_shouldCreateBookingSuccessfully() {

        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );
        when( eventRepository.save( any( Event.class ) ) )
                .thenReturn( testEvent );
        when( bookingRepository.save( any( Booking.class ) ) ).thenReturn( testBooking );

        BookingResponseDto result = bookingService.makeBooking( bookingRequestDto );


        assertNotNull( result );
        assertEquals( "John Doe", result.name() );
        assertEquals( 5, result.numberOfTickets() );
        assertEquals( "Test Event", result.hostingEvent().title() );
        verify( eventRepository ).save( any( Event.class ) );
    }

    @Test
    @DisplayName("Throws 404 when event not found")
    void makeBooking_shouldThrowExceptionWhenEventNotFound() {

        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.empty() );


        assertThrows( ResourceNotFoundException.class, () ->
                bookingService.makeBooking( bookingRequestDto )
        );
        verify( eventRepository, never() ).save( any( Event.class ) );
    }

    @Test
    @DisplayName("Throws 409 when not enough tickets")
    void makeBooking_shouldThrowExceptionWhenNotEnoughTickets() {

        testEvent = testEvent.toBuilder().freeTicketCapacity( 3 ).build();
        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );


        IllegalStateException exception = assertThrows( IllegalStateException.class, () ->
                bookingService.makeBooking( bookingRequestDto )
        );
        assertTrue( exception.getMessage().contains( "Not enough tickets available" ) );
        verify( eventRepository, never() ).save( any( Event.class ) );
    }

    @Test
    @DisplayName("Set ticket alarm when below 20 percent")
    void makeBooking_shouldSetTicketAlarmWhenBelow20Percent() {

        testEvent = testEvent.toBuilder()
                .maxTicketCapacity( 100 )
                .freeTicketCapacity( 25 )
                .build();
        BookingRequestDto largeBooking = bookingRequestDto.toBuilder()
                .numberOfTickets( 10 )
                .build();
        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );
        when( eventRepository.save( any( Event.class ) ) )
                .thenReturn( testEvent );
        when( bookingRepository.save( any( Booking.class ) ) ).thenReturn( testBooking );


        bookingService.makeBooking( largeBooking );


        verify( eventRepository ).save( argThat( event ->
                event.getTicketAlarm() && event.getFreeTicketCapacity() == 15
        ) );
    }

    @Test
    @DisplayName("Flags event as sold out after booking")
    void makeBooking_shouldSetSoldOutWhenNoTicketsLeft() {

        testEvent = testEvent.toBuilder().freeTicketCapacity( 5 ).build();
        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );
        when( eventRepository.save( any( Event.class ) ) )
                .thenReturn( testEvent );
        when( bookingRepository.save( any( Booking.class ) ) ).thenReturn( testBooking );

        bookingService.makeBooking( bookingRequestDto );


        verify( eventRepository ).save( argThat( event ->
                event.getIsSoldOut() && event.getFreeTicketCapacity() == 0
        ) );
    }

    @Test
    @DisplayName("Returns booking correct with limitless tickets")
    void makeBooking_shouldHandleLimitlessTicketsCorrectly() {

        testEvent = testEvent.toBuilder()
                .maxTicketCapacity( null )
                .freeTicketCapacity( null )
                .build();
        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );
        when( eventRepository.save( any( Event.class ) ) )
                .thenReturn( testEvent );
        when( bookingRepository.save( any( Booking.class ) ) ).thenReturn( testBooking );

        BookingResponseDto result = bookingService.makeBooking( bookingRequestDto );

        assertNotNull( result );
        assertEquals( "John Doe", result.name() );
        assertEquals( 5, result.numberOfTickets() );
        assertEquals( "Test Event", result.hostingEvent().title() );

        verify( eventRepository, times( 2 ) ).findById( "event-1" );
        verify( eventRepository ).save( argThat( event ->
                event.getBookedTicketsCount() == testEvent.getBookedTicketsCount() + bookingRequestDto.numberOfTickets()
        ) );
        verify( bookingRepository ).save( any( Booking.class ) );
    }

    @Test
    @DisplayName("Returns 409 when numberOfTickets is higher than maxPerBooking")
    void makeBooking_shouldThrowExceptionWhenMaxPerBookingExceeded() {

        testEvent = testEvent.toBuilder().maxPerBooking( 3 ).build();
        BookingRequestDto largeBooking = bookingRequestDto.toBuilder()
                .numberOfTickets( 5 )
                .build();
        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );
        IllegalStateException exception = assertThrows( IllegalStateException.class, () ->
                bookingService.makeBooking( largeBooking )
        );
        assertTrue( exception.getMessage().contains( "You cannot book more than 3 tickets for this event." ) );
        verify( eventRepository, never() ).save( any( Event.class ) );
    }

    @Test
    @DisplayName("Returns bookings for a specific user")
    void getBookingsByUser_shouldReturnBookingsForUser() {
        when( bookingRepository.findAll() ).thenReturn( List.of( testBooking ) );

        List<BookingResponseDto> result = bookingService.getBookingsByUser( "user-1" );

        assertNotNull( result );
        assertEquals( 1, result.size() );
        assertEquals( "John Doe", result.get( 0 ).name() );
        verify( bookingRepository ).findAll();
    }

    @Test
    @DisplayName("Returns raw booking by id")
    void getRawBookingById_shouldReturnBookingById() {
        when( bookingRepository.findById( "booking-1" ) ).thenReturn( Optional.of( testBooking ) );

        Booking result = bookingService.getRawBookingById( "booking-1" );

        assertNotNull( result );
        assertEquals( "John Doe", result.getName() );
        verify( bookingRepository ).findById( "booking-1" );
    }

    @Test
    @DisplayName("Get raw booking returns 404 when not found")
    void getRawBookingById_shouldThrowExceptionWhenNotFound() {
        when( bookingRepository.findById( "booking-1" ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () ->
                bookingService.getRawBookingById( "booking-1" )
        );
        verify( bookingRepository ).findById( "booking-1" );
    }

    @Test
    @DisplayName("Throws exception when user has already booked the event")
    void checkIfUserHasBookedEvent_shouldThrowExceptionIfUserAlreadyBooked() {
        when( bookingRepository.findAll() ).thenReturn( List.of( testBooking ) );

        IllegalStateException exception = assertThrows( IllegalStateException.class, () ->
                bookingService.checkIfUserHasBookedEvent( "user-1", "event-1" )
        );

        assertTrue( exception.getMessage().contains( "You cannot book the same event more than once." ) );
        verify( bookingRepository ).findAll();
    }

    @Test
    @DisplayName("Does not throw exception when user has not booked the event")
    void checkIfUserHasBookedEvent_shouldNotThrowExceptionIfUserHasNotBooked() {
        when( bookingRepository.findAll() ).thenReturn( List.of() );

        assertDoesNotThrow( () ->
                bookingService.checkIfUserHasBookedEvent( "user-1", "event-1" )
        );

        verify( bookingRepository ).findAll();
    }

    @Test
    @DisplayName("Deletes booking by id")
    void deleteBooking_shouldDeleteBookingById() {
        when( bookingRepository.findById( "booking-1" ) ).thenReturn( Optional.of( testBooking ) );
        when( eventRepository.save( any( Event.class ) ) ).thenReturn( testEvent );

        bookingService.deleteBookingById( "booking-1" );

        verify( bookingRepository ).deleteById( "booking-1" );
        verify( eventRepository ).save( any( Event.class ) );
    }

    @Test
    @DisplayName("Deletes booking by id without updating ticketAlarm flag")
    void deleteBooking_shouldDeleteBookingByIdWithoutUpdatingFlags() {
        Event testEventWithMaxCapacityNull = testEvent.toBuilder()
                .maxTicketCapacity( null )
                .ticketAlarm( false )
                .build();

        Booking testBookingWithEventWithMaxCapacityNull = testBooking.toBuilder()
                .event( testEventWithMaxCapacityNull )
                .build();

        when( bookingRepository.findById( "booking-1" ) ).thenReturn( Optional.of( testBookingWithEventWithMaxCapacityNull ) );
        when( eventRepository.save( any( Event.class ) ) ).thenReturn( testEventWithMaxCapacityNull );

        bookingService.deleteBookingById( "booking-1" );

        verify( bookingRepository ).deleteById( "booking-1" );
        verify( eventRepository ).save( any( Event.class ) );
    }

    @Test
    @DisplayName("Deletes booking and updates event with ticketAlarm flag true")
    void deleteBooking_shouldUpdateEventWithTicketAlarmTrue() {
        testEvent = testEvent.toBuilder()
                .freeTicketCapacity( 15 )
                .ticketAlarm( true )
                .build();
        Booking bookingToDelete = Booking.builder()
                .id( "booking-1" )
                .numberOfTickets( 10 )
                .event( testEvent )
                .build();

        when( bookingRepository.findById( "booking-1" ) ).thenReturn( Optional.of( bookingToDelete ) );
        when( eventRepository.save( any( Event.class ) ) ).thenReturn( testEvent );

        bookingService.deleteBookingById( "booking-1" );

        verify( bookingRepository ).deleteById( "booking-1" );
        verify( eventRepository ).save( argThat( event ->
                event.getFreeTicketCapacity() == 15 + 10 &&
                        !event.getTicketAlarm() &&
                        !event.getIsSoldOut()
        ) );
    }

    @Test
    @DisplayName("Throws 404 when booking not found")
    void deleteBooking_shouldThrowExceptionWhenBookingNotFound() {
        when( bookingRepository.findById( "booking-1" ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () ->
                bookingService.deleteBookingById( "booking-1" )
        );

        verify( bookingRepository, never() ).deleteById( anyString() );
        verify( eventRepository, never() ).save( any( Event.class ) );
    }
}
