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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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

    private static Stream<Arguments> provideTicketCapacityTestCases() {
        return Stream.of(
                // maxCapacity, freeCapacity, expectedTicketAlarm, expectedSoldOut
                Arguments.of( null, null, false, false ),              // With null capacities
                Arguments.of( 0, 0, false, false ),                    // With capacities set to 0
                Arguments.of( 100, 100, false, false ),                // All Tickets available
                Arguments.of( 100, 50, false, false ),                 // 50% available
                Arguments.of( 100, 21, false, false ),                 // 21% available
                Arguments.of( 100, 20, true, false ),                  // Edge case: 20% available - Alarm active
                Arguments.of( 100, 10, true, false ),                  // 10% available - Alarm active
                Arguments.of( 100, 1, true, false ),                   // 1% available - Alarm active
                Arguments.of( 100, 0, false, true ),                   // sold out
                Arguments.of( 100, null, false, true )                 // freeCapacity null = sold out
        );
    }

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
                .ticketAlarm( false )
                .isSoldOut( false )
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

    @ParameterizedTest
    @DisplayName("Returns correct ticketAlarm and isSoldOut based on ticket capacities")
    @MethodSource("provideTicketCapacityTestCases")
    void getEventById_calculatesTicketAlarmAndSoldOutCorrectly(
            Integer maxCapacity,
            Integer freeCapacity,
            boolean expectedTicketAlarm,
            boolean expectedSoldOut
    ) {
        Event eventWithCapacities = exampleEvent.toBuilder()
                .maxTicketCapacity( maxCapacity )
                .freeTicketCapacity( freeCapacity )
                .build();

        when( eventRepo.findById( exampleEvent.getId() ) ).thenReturn( Optional.of( eventWithCapacities ) );
        when( userRepo.findAllById( exampleOrganization.getOwners() ) ).thenReturn( List.of( exampleUser ) );

        EventResponseDto actualEvent = eventService.getEventById( exampleEvent.getId() );

        assertEquals( expectedTicketAlarm, actualEvent.ticketAlarm() );
        assertEquals( expectedSoldOut, actualEvent.isSoldOut() );

        verify( eventRepo ).findById( exampleEvent.getId() );
        verify( userRepo ).findAllById( exampleOrganization.getOwners() );
    }

    @Test
    @DisplayName("Returns event dto found by id")
    void getEventById() {
        when( eventRepo.findById( exampleEvent.getId() ) ).thenReturn( Optional.of( exampleEvent ) );
        when( userRepo.findAllById( exampleOrganization.getOwners() ) ).thenReturn( List.of( exampleUser ) );

        EventResponseDto actualEvent = eventService.getEventById( exampleEvent.getId() );

        assertEquals( exampleEventResponseDto, actualEvent );

        verify( eventRepo ).findById( exampleEvent.getId() );
        verify( userRepo ).findAllById( exampleOrganization.getOwners() );
    }

    @Test
    @DisplayName("Returns 404 when event not found by id")
    void getEventById_throws404WhenNotFound() {
        String notExistingEventId = "nonExistentEventId";
        when( eventRepo.findById( notExistingEventId ) ).thenReturn( Optional.empty() );
        assertThatThrownBy( () ->
                eventService.getEventById( notExistingEventId ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Event not found with id: " + notExistingEventId );
        verify( eventRepo ).findById( notExistingEventId );
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
    @DisplayName("Should return created event with correct ticket capacities")
    void createEvent_shouldReturnCapacityCorrect() {
        EventRequestDto eventRequestWithMaxCapacity = exampleEventRequestDto.toBuilder()
                .maxTicketCapacity( 100 )
                .build();

        Event exampleEventWithMaxCapacity = exampleEvent.toBuilder()
                .maxTicketCapacity( eventRequestWithMaxCapacity.maxTicketCapacity() )
                .freeTicketCapacity( eventRequestWithMaxCapacity.maxTicketCapacity() )
                .build();

        when( eventRepo.save( any( Event.class ) ) ).thenReturn( exampleEventWithMaxCapacity );
        when( orgaRepo.findById( exampleEventRequestDto.organizationId() ) ).thenReturn( Optional.of( exampleOrganization ) );

        Event actualCreatedEvent = eventService.createEvent( eventRequestWithMaxCapacity, null );

        Integer expectedFreeCapacity = eventRequestWithMaxCapacity.maxTicketCapacity();
        Integer actualFreeCapacity = actualCreatedEvent.getFreeTicketCapacity();

        assertEquals( expectedFreeCapacity, actualFreeCapacity );

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