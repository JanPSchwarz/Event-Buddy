package org.eventbuddy.backend.services;

import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.event.EventRequestDto;
import org.eventbuddy.backend.models.event.EventResponseDto;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    EventRepository eventRepo;

    @Mock
    UserRepository userRepo;

    @Mock
    OrganizationRepository orgaRepo;

    @InjectMocks
    EventService eventService;

    Event exampleEvent;
    EventRequestDto exampleEventRequestDto;
    EventResponseDto exampleEventResponseDto;
    Organization exampleOrganization;
    AppUser exampleUser;

    @BeforeEach
    void setUp() {

        UserSettings exampleUserSettings = UserSettings.builder()
                .userVisible( true )
                .showEmail( true )
                .showOrgas( true )
                .showAvatar( true )
                .build();

        exampleUser = AppUser.builder()
                .id( "exampleUserId" )
                .providerId( "exampleProviderId" )
                .userSettings( exampleUserSettings )
                .role( Role.USER )
                .name( "Sample User" )
                .build();

        AppUserDto exampleUserDto = AppUserDto.builder()
                .id( exampleUser.getId() )
                .name( exampleUser.getName() )
                .email( exampleUser.getEmail() )
                .avatarUrl( exampleUser.getAvatarUrl() )
                .build();

        Location exampleLocation = Location.builder()
                .address( "123 Sample Street" )
                .city( "Sample City" )
                .zipCode( "12345" )
                .country( "Sample Country" )
                .latitude( 52.5200 )
                .longitude( 13.4050 )
                .build();

        exampleOrganization = Organization.builder()
                .id( "exampleOrgId" )
                .name( "Sample Organization" )
                .slug( "sample-organization-slug" )
                .location( exampleLocation )
                .owners( Set.of( "exampleUserId" ) )
                .description( "This is a sample organization." )
                .build();

        OrganizationResponseDto exampleOrganizationResponseDto = OrganizationResponseDto.builder()
                .id( exampleOrganization.getId() )
                .name( exampleOrganization.getName() )
                .slug( exampleOrganization.getSlug() )
                .location( exampleOrganization.getLocation() )
                .owners( Set.of( exampleUserDto ) )
                .build();

        exampleEvent = Event.builder()
                .id( "exampleEventId" )
                .eventOrganization( exampleOrganization )
                .title( "Sample Event" )
                .eventDateTime( Instant.now() )
                .location( exampleLocation )
                .build();

        exampleEventRequestDto = EventRequestDto.builder()
                .organizationId( exampleOrganization.getId() )
                .title( exampleEvent.getTitle() )
                .eventDateTime( exampleEvent.getEventDateTime() )
                .location( exampleLocation )
                .build();

        exampleEventResponseDto = EventResponseDto.builder()
                .id( "exampleEventId" )
                .eventOrganization( exampleOrganizationResponseDto )
                .title( exampleEvent.getTitle() )
                .eventDateTime( exampleEvent.getEventDateTime() )
                .location( exampleLocation )
                .build();
    }


    @Test
    @DisplayName("Returns list of event dtos")
    void getAllEvents() {
        when( eventRepo.findAll() ).thenReturn( List.of( exampleEvent ) );
        when( userRepo.findAllById( exampleOrganization.getOwners() ) ).thenReturn( List.of( exampleUser ) );

        List<EventResponseDto> actualEvents = eventService.getAllEvents();

        assertEquals( List.of( exampleEventResponseDto ), actualEvents );

        verify( eventRepo ).findAll();
        verify( userRepo ).findAllById( exampleOrganization.getOwners() );
    }

    @Test
    @DisplayName("Returns 404 when orga owner not found")
    void getAllEvents_throws404OrgaOwnerNotFound() {
        when( eventRepo.findAll() ).thenReturn( List.of( exampleEvent ) );
        when( userRepo.findAllById( exampleOrganization.getOwners() ) ).thenReturn( List.of() );

        assertThatThrownBy( () -> eventService.getAllEvents() )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "One or more organization owners not found for organization with id: " + exampleOrganization.getId() );

        verify( eventRepo ).findAll();
        verify( userRepo ).findAllById( exampleOrganization.getOwners() );
    }

    @Test
    @DisplayName("Returns raw event when found by id")
    void getRawEventById() {
        when( eventRepo.findById( exampleEvent.getId() ) ).thenReturn( Optional.of( exampleEvent ) );

        Event actualEvent = eventService.getRawEventById( exampleEvent.getId() );

        assertEquals( exampleEvent, actualEvent );
        verify( eventRepo ).findById( exampleEvent.getId() );
    }

    @Test
    @DisplayName("Returns 404 when raw event not found by id")
    void getRawEventById_throws404WhenNotFound() {
        String notExistingEventId = "nonExistentEventId";
        when( eventRepo.findById( notExistingEventId ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () ->
                eventService.getRawEventById( notExistingEventId ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Event not found with id: " + notExistingEventId );

        verify( eventRepo ).findById( notExistingEventId );
    }

    @Test
    @DisplayName("Should return created event without image")
    void createEvent_withoutImage() {
        when( eventRepo.save( any( Event.class ) ) ).thenReturn( exampleEvent );
        when( orgaRepo.findById( exampleEventRequestDto.organizationId() ) ).thenReturn( Optional.of( exampleOrganization ) );

        Event actualCreatedEvent = eventService.createEvent( exampleEventRequestDto, null );

        assertEquals( exampleEvent, actualCreatedEvent );

        verify( orgaRepo ).findById( exampleEventRequestDto.organizationId() );
    }

    @Test
    @DisplayName("Should return created event with image")
    void createEvent_withImage() {
        Event exampleEventWithImage = exampleEvent.toBuilder()
                .imageId( "exampleImageId" )
                .build();

        when( eventRepo.save( any( Event.class ) ) ).thenReturn( exampleEventWithImage );
        when( orgaRepo.findById( exampleEventRequestDto.organizationId() ) ).thenReturn( Optional.of( exampleOrganization ) );

        Event actualCreatedEvent = eventService.createEvent( exampleEventRequestDto, "exampleImageId" );

        assertEquals( exampleEventWithImage, actualCreatedEvent );

        verify( orgaRepo ).findById( exampleEventRequestDto.organizationId() );
    }

    @Test
    @DisplayName("Should throw 404 when orga not found")
    void createEvent_throws404WhenOrgaNotFound() {
        when( orgaRepo.findById( exampleEventRequestDto.organizationId() ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () ->
                eventService.createEvent( exampleEventRequestDto, null ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Organization not found with id: " + exampleEventRequestDto.organizationId() );

        verify( orgaRepo ).findById( exampleEventRequestDto.organizationId() );
    }
}