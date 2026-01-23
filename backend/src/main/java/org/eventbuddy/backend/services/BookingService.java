package org.eventbuddy.backend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.booking.Booking;
import org.eventbuddy.backend.models.booking.BookingRequestDto;
import org.eventbuddy.backend.models.booking.BookingResponseDto;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.event.EventResponseDto;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.repos.BookingRepository;
import org.eventbuddy.backend.repos.EventRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class BookingService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final BookingRepository bookingRepository;

    // === GET Methods ===

    public List<BookingResponseDto> getBookingsByUser( String userId ) {
        return bookingRepository.findAll().stream()
                .filter( booking -> booking.getUserId().equals( userId ) )
                .map( this::bookingToBookingResponseDto )
                .toList();
    }

    public Booking getRawBookingById( String bookingId ) {
        return bookingRepository.findById( bookingId ).orElseThrow( () ->
                new ResourceNotFoundException( "Booking not found with id: " + bookingId ) );
    }


    // === POST Methods ===
    public BookingResponseDto makeBooking( BookingRequestDto bookingRequestDto ) {

        Event associatedEvent = eventRepository.findById( bookingRequestDto.eventId() ).orElseThrow( () ->
                new ResourceNotFoundException( "Event not found with id: " + bookingRequestDto.eventId() ) );

        checkIfUserHasBookedEvent( bookingRequestDto.userId(), bookingRequestDto.eventId() );


        boolean maxPerBookingExceeded = associatedEvent.getMaxPerBooking() != null &&
                bookingRequestDto.numberOfTickets() > associatedEvent.getMaxPerBooking();

        if ( maxPerBookingExceeded ) {
            throw new IllegalStateException( "You cannot book more than " + associatedEvent.getMaxPerBooking() + " tickets for this event." );
        }

        boolean hasLimitlessTickets = associatedEvent.getMaxTicketCapacity() == null;
        boolean hasEnoughTickets = hasLimitlessTickets || associatedEvent.getFreeTicketCapacity() >= bookingRequestDto.numberOfTickets();

        if ( !hasEnoughTickets ) {
            throw new IllegalStateException( "Not enough tickets available for your booking. Tickets left: " + associatedEvent.getFreeTicketCapacity() );
        }

        Event updatedEvent = associatedEvent.toBuilder()
                .bookedTicketsCount( associatedEvent.getBookedTicketsCount() + bookingRequestDto.numberOfTickets() )
                .build();

        // calculate and update free ticket capacity and isSoldOut/ticketAlarm flags
        if ( !hasLimitlessTickets ) {
            int updatedFreeTicketCapacity = associatedEvent.getFreeTicketCapacity() - bookingRequestDto.numberOfTickets();

            boolean isSoldOut = updatedFreeTicketCapacity == 0;

            boolean isTicketAlarm = ( ( double ) updatedFreeTicketCapacity / associatedEvent.getMaxTicketCapacity() ) <= 0.2;

            updatedEvent = updatedEvent.toBuilder()
                    .freeTicketCapacity( updatedFreeTicketCapacity )
                    .ticketAlarm( isTicketAlarm )
                    .isSoldOut( isSoldOut )
                    .build();

        }

        eventRepository.save( updatedEvent );

        Booking newBooking = requestToBookingMapper( bookingRequestDto );

        Booking savedBooking = bookingRepository.save( newBooking );

        return bookingToBookingResponseDto( savedBooking );
    }


    // === DELETE Methods ===

    public void deleteBookingById( String bookingId ) {
        Booking bookingToDelete = bookingRepository.findById( bookingId ).orElseThrow( () ->
                new ResourceNotFoundException( "Booking not found with id: " + bookingId ) );

        Event associatedEvent = bookingToDelete.getEvent();

        Event updatedEvent = associatedEvent.toBuilder()
                .bookedTicketsCount( associatedEvent.getBookedTicketsCount() - bookingToDelete.getNumberOfTickets() )
                .build();

        // calculate and update free ticket capacity
        if ( associatedEvent.getMaxTicketCapacity() != null ) {
            int updatedFreeTicketCapacity = associatedEvent.getFreeTicketCapacity() + bookingToDelete.getNumberOfTickets();

            boolean isSoldOut = updatedFreeTicketCapacity == 0;

            boolean isTicketAlarm = ( ( double ) updatedFreeTicketCapacity / associatedEvent.getMaxTicketCapacity() ) <= 0.2;

            updatedEvent = updatedEvent.toBuilder()
                    .freeTicketCapacity( updatedFreeTicketCapacity )
                    .ticketAlarm( isTicketAlarm )
                    .isSoldOut( isSoldOut )
                    .build();

        }

        eventRepository.save( updatedEvent );

        bookingRepository.deleteById( bookingId );
    }

    // === Helper Methods ===

    private Booking requestToBookingMapper( BookingRequestDto bookingRequestDto ) {

        Event associatedEvent = eventRepository.findById( bookingRequestDto.eventId() ).orElseThrow( () ->
                new ResourceNotFoundException( "Event not found with id: " + bookingRequestDto.eventId() ) );

        return Booking.builder()
                .name( bookingRequestDto.name() )
                .numberOfTickets( bookingRequestDto.numberOfTickets() )
                .event( associatedEvent )
                .userId( bookingRequestDto.userId() )
                .build();
    }

    public void checkIfUserHasBookedEvent( String userId, String eventId ) {

        List<Booking> allBookings = bookingRepository.findAll();

        if ( allBookings.isEmpty() ) {
            return;
        }

        List<Booking> userBookings = allBookings.stream()
                .filter( booking -> booking.getUserId().equals( userId ) )
                .filter( booking -> booking.getEvent().getId().equals( eventId ) )
                .toList();

        if ( !userBookings.isEmpty() ) {
            throw new IllegalStateException( "You cannot book the same event more than once." );
        }
    }

    private BookingResponseDto bookingToBookingResponseDto( Booking booking ) {

        Organization associatedOrganization = booking.getEvent().getEventOrganization();

        Event associatedEvent = booking.getEvent();

        OrganizationResponseDto organizationResponseDto = OrganizationResponseDto.builder()
                .name( associatedOrganization.getName() )
                .id( associatedOrganization.getId() )
                .slug( associatedOrganization.getSlug() )
                .owners( Set.of() )
                .description( associatedOrganization.getDescription() )
                .website( associatedOrganization.getWebsite() )
                .imageId( associatedOrganization.getImageId() )
                .location( associatedOrganization.getLocation() )
                .contact( associatedOrganization.getContact() )
                .build();


        EventResponseDto eventResponseDto = EventResponseDto.builder()
                .id( associatedEvent.getId() )
                .eventOrganization( organizationResponseDto )
                .title( associatedEvent.getTitle() )
                .description( associatedEvent.getDescription() )
                .location( associatedEvent.getLocation() )
                .eventDateTime( associatedEvent.getEventDateTime() )
                .imageId( associatedEvent.getImageId() )
                .price( associatedEvent.getPrice() )
                .bookedTicketsCount( associatedEvent.getBookedTicketsCount() )
                .ticketAlarm( associatedEvent.getTicketAlarm() )
                .isSoldOut( associatedEvent.getIsSoldOut() )
                .maxPerBooking( associatedEvent.getMaxPerBooking() )
                .build();

        return BookingResponseDto.builder()
                .hostingEvent( eventResponseDto )
                .name( booking.getName() )
                .bookingId( booking.getId() )
                .numberOfTickets( booking.getNumberOfTickets() )
                .build();
    }


}
