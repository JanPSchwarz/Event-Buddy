package org.eventbuddy.backend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.event.EventRequestDto;
import org.eventbuddy.backend.models.event.EventResponseDto;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.repos.EventRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepo;

    private final OrganizationRepository orgaRepo;

    private final UserRepository userRepo;

    // === GET ===

    public List<EventResponseDto> getAllEvents() {
        List<Event> allEvents = eventRepo.findAll();

        return allEvents.stream()
                .map( this::eventToEventResponseDtoMapper )
                .toList();
    }

    public Event getRawEventById( String eventId ) {
        return eventRepo.findById( eventId ).orElseThrow(
                () -> new ResourceNotFoundException( "Event not found with id: " + eventId )
        );
    }

    // === POST ===

    public Event createEvent( EventRequestDto eventDto, String imageId ) {
        Event mappedEvent = eventDtoToEventMapper( eventDto );

        if ( imageId != null && !imageId.isEmpty() ) {
            mappedEvent = mappedEvent.toBuilder()
                    .imageId( imageId )
                    .build();
        }

        return eventRepo.save( mappedEvent );
    }

    // === Mappers ===

    private Event eventDtoToEventMapper( EventRequestDto eventDto ) {

        Organization organization = orgaRepo.findById( eventDto.organizationId() ).orElseThrow(
                () -> new ResourceNotFoundException( "Organization not found with id: " + eventDto.organizationId() )
        );

        return Event.builder()
                .eventOrganization( organization )
                .title( eventDto.title() )
                .description( eventDto.description() )
                .eventDateTime( eventDto.eventDateTime() )
                .location( eventDto.location() )
                .price( eventDto.price() )
                .maxTicketCapacity( eventDto.maxTicketCapacity() )
                .maxPerBooking( eventDto.maxPerBooking() )
                .build();
    }

    private AppUserDto appUserToAppUserDtoMapper( AppUser user ) {
        return AppUserDto.builder()
                .id( user.getId() )
                .name( user.getName() )
                .email( user.getUserSettings().showEmail() ? user.getEmail() : null )
                .avatarUrl( user.getUserSettings().showAvatar() ? user.getAvatarUrl() : null )
                .build();
    }

    private EventResponseDto eventToEventResponseDtoMapper( Event event ) {

        Organization org = event.getEventOrganization();

        List<AppUser> allOwners = userRepo.findAllById( org.getOwners() );

        if ( allOwners.size() != org.getOwners().size() ) {
            throw new ResourceNotFoundException( "One or more organization owners not found for organization with id: " + org.getId() );
        }

        Set<AppUserDto> ownersDto = allOwners.stream()
                .map( this::appUserToAppUserDtoMapper )
                .collect( Collectors.toSet() );

        OrganizationResponseDto orgDto = OrganizationResponseDto.builder()
                .id( org.getId() )
                .name( org.getName() )
                .slug( org.getSlug() )
                .location( org.getLocation() )
                .owners( ownersDto )
                .build();

        return EventResponseDto.builder()
                .id( event.getId() )
                .eventOrganization( orgDto )
                .title( event.getTitle() )
                .description( event.getDescription() )
                .eventDateTime( event.getEventDateTime() )
                .location( event.getLocation() )
                .price( event.getPrice() )
                .maxTicketCapacity( event.getMaxTicketCapacity() )
                .maxPerBooking( event.getMaxPerBooking() )
                .imageId( event.getImageId() )
                .build();
    }
}
