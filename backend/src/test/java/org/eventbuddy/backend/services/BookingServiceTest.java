package org.eventbuddy.backend.services;

import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.booking.BookingRequestDto;
import org.eventbuddy.backend.models.booking.BookingResponseDto;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.organization.Organization;
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
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private BookingService bookingService;

    private Organization testOrganization;
    private Event testEvent;
    private BookingRequestDto bookingRequestDto;

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
    }

    @Test
    @DisplayName("Returns booking dto when created")
    void makeBooking_shouldCreateBookingSuccessfully() {

        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );
        when( eventRepository.save( any( Event.class ) ) )
                .thenReturn( testEvent );

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


        bookingService.makeBooking( largeBooking );


        verify( eventRepository ).save( argThat( event ->
                event.getTicketAlarm() && event.getFreeTicketCapacity() == 15
        ) );
    }

    @Test
    void makeBooking_shouldSetSoldOutWhenNoTicketsLeft() {

        testEvent = testEvent.toBuilder().freeTicketCapacity( 5 ).build();
        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );
        when( eventRepository.save( any( Event.class ) ) )
                .thenReturn( testEvent );

        bookingService.makeBooking( bookingRequestDto );


        verify( eventRepository ).save( argThat( event ->
                event.getIsSoldOut() && event.getFreeTicketCapacity() == 0
        ) );
    }

    @Test
    void makeBooking_shouldNotUpdateCapacityForLimitlessTickets() {

        testEvent = testEvent.toBuilder()
                .maxTicketCapacity( null )
                .freeTicketCapacity( null )
                .build();
        when( eventRepository.findById( "event-1" ) ).thenReturn( Optional.of( testEvent ) );


        bookingService.makeBooking( bookingRequestDto );


        verify( eventRepository, never() ).save( any( Event.class ) );
    }
}
