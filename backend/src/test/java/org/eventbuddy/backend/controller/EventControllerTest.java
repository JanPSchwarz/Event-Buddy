package org.eventbuddy.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eventbuddy.backend.TestcontainersConfiguration;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.mockUser.WithCustomSuperAdmin;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@WithCustomMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventControllerTest {

    OAuth2AuthenticationToken oAuth2Token;
    String savedAuthenticatedUserId;
    OrganizationResponseDto savedOrganizationDto;
    Organization savedOrganization;
    Event savedExampleEvent;
    EventResponseDto savedExampleEventResponse;
    EventRequestDto exampleEventRequestDto;


    @Autowired
    EventRepository eventRepo;
    @Autowired
    OrganizationRepository organizationRepo;
    @Autowired
    UserRepository userRepo;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        userRepo.deleteAll();
        organizationRepo.deleteAll();
        eventRepo.deleteAll();

        // Save annotated test user to userRepo
        SecurityContext authContext = SecurityContextHolder.getContext();
        oAuth2Token = ( OAuth2AuthenticationToken ) authContext.getAuthentication();

        String provider = oAuth2Token.getAuthorizedClientRegistrationId();

        String authenticatedUserRole = oAuth2Token.getAuthorities().iterator().next().getAuthority();
        String providerId = provider + "_" + oAuth2Token.getName();
        Role role = Role.valueOf( authenticatedUserRole.replace( "ROLE_", "" ) );

        UserSettings userSettings = UserSettings.builder()
                .userVisible( true )
                .showAvatar( true )
                .showOrgas( true )
                .showEmail( true )
                .build();

        AppUser testUser = AppUser.builder()
                .role( role )
                .name( "testName" )
                .providerId( providerId )
                .userSettings( userSettings )
                .build();

        AppUserDto testUserDto = AppUserDto.builder()
                .name( testUser.getName() )
                .email( testUser.getUserSettings().showEmail() ? testUser.getEmail() : null )
                .avatarUrl( testUser.getUserSettings().showAvatar() ? testUser.getAvatarUrl() : null )
                .build();

        AppUser savedUser = userRepo.save( testUser );

        testUserDto = testUserDto.toBuilder()
                .id( savedUser.getId() )
                .build();

        savedAuthenticatedUserId = savedUser.getId();


        // Save test organization
        Location testLocation = Location.builder()
                .address( "exampleStreet 15" )
                .city( "exampleCity" )
                .zipCode( "12345" )
                .country( "ExampleCountry" )
                .build();

        Organization testOrganization = Organization.builder()
                .name( "Test Organization" )
                .id( "testId" )
                .slug( "test-organization" )
                .owners( Set.of( testUser.getId() ) )
                .location( testLocation )
                .build();

        savedOrganizationDto = OrganizationResponseDto.builder()
                .name( testOrganization.getName() )
                .id( testOrganization.getId() )
                .slug( testOrganization.getSlug() )
                .location( testOrganization.getLocation() )
                .owners( Set.of( testUserDto ) )
                .build();

        savedOrganization = organizationRepo.save( testOrganization );


        // Save test event

        Event exampleEvent = Event.builder()
                .eventOrganization( savedOrganization )
                .title( "Example Event" )
                .eventDateTime( Instant.now().truncatedTo( ChronoUnit.MILLIS ) )
                .location( testLocation )
                .build();

        savedExampleEvent = eventRepo.save( exampleEvent );

        savedExampleEventResponse = EventResponseDto.builder()
                .id( savedExampleEvent.getId() )
                .eventOrganization( savedOrganizationDto )
                .title( savedExampleEvent.getTitle() )
                .eventDateTime( savedExampleEvent.getEventDateTime() )
                .isSoldOut( false )
                .ticketAlarm( false )
                .location( savedExampleEvent.getLocation() )
                .build();

        // example event request dto
        exampleEventRequestDto = EventRequestDto.builder()
                .organizationId( savedOrganization.getId() )
                .title( "Example Event" )
                .price( 1.99 )
                .eventDateTime( Instant.now().truncatedTo( ChronoUnit.MILLIS ).plus( 1, ChronoUnit.DAYS ) )
                .location( testLocation )
                .build();
    }


    @Test
    @DisplayName("Get all event returns list of all event dtos")
    void getAllEvents() throws Exception {
        String expectedJson = objectMapper.writeValueAsString( List.of( savedExampleEventResponse ) );

        mockMvc.perform( get( "/api/events/all" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );

    }

    @Test
    @DisplayName("Get all events returns empty list when no events found")
    void getAllEvents_returnsEmpty() throws Exception {
        eventRepo.deleteAll();
        String expectedJson = objectMapper.writeValueAsString( List.of() );

        mockMvc.perform( get( "/api/events/all" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );

    }

    @Test
    @DisplayName("Returns event dto found by id")
    void getEventById() throws Exception {
        String expectedJson = objectMapper.writeValueAsString( savedExampleEventResponse );

        mockMvc.perform( get( "/api/events/" + savedExampleEvent.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Throws 404 when event not found")
    void getEventById_throws404WhenNotFound() throws Exception {
        String nonexistentEventId = "nonexistent";

        mockMvc.perform( get( "/api/events/" + nonexistentEventId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "Event not found with id: " + nonexistentEventId ) );
    }

    @Test
    @DisplayName("Returns raw event found by id")
    void getRawEventById() throws Exception {
        Event savedEvent = eventRepo.findById( savedExampleEvent.getId() ).orElseThrow();

        String expectedJson = objectMapper.writeValueAsString( savedEvent );

        mockMvc.perform( get( "/api/events/raw/" + savedExampleEvent.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Get raw event by id throws 401 when called by non-owner")
    void getRawEventById_throws401WhenNotAuthorized() throws Exception {
        Organization orgaWithForeignOwner = savedOrganization.toBuilder()
                .owners( Set.of( "foreignOwnerId" ) )
                .build();

        organizationRepo.save( orgaWithForeignOwner );

        mockMvc.perform( get( "/api/events/raw/" + savedExampleEvent.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "Your are not allowed to perform this action." ) );
    }

    @Test
    @DisplayName("Returns raw event found by id when called by super admin")
    @WithCustomSuperAdmin
    void getRawEventById_whenCalledBySuperAdmin() throws Exception {

        Organization orgaWithForeignOwner = savedOrganization.toBuilder()
                .owners( Set.of( "foreignOwnerId" ) )
                .build();

        organizationRepo.save( orgaWithForeignOwner );

        Event savedEvent = eventRepo.findById( savedExampleEvent.getId() ).orElseThrow();

        String expectedJson = objectMapper.writeValueAsString( savedEvent );

        mockMvc.perform( get( "/api/events/raw/" + savedExampleEvent.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Get raw event throws 404 when event not found by id")
    void getRawEventById_throws() throws Exception {
        String nonexistentEventId = "nonexistent";

        mockMvc.perform( get( "/api/events/raw/" + nonexistentEventId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "Event not found with id: " + nonexistentEventId ) );
    }

    @Test
    @DisplayName("Get raw event by id throws 401 when not authenticated")
    void getRawEventById_throws401WhenNotAuthenticated() throws Exception {
        oAuth2Token.setAuthenticated( false );

        mockMvc.perform( get( "/api/events/raw/" + savedExampleEvent.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "User is not logged in." ) );
    }

    @Test
    @DisplayName("Returns list of events found by orga id")
    void getEventsByOrganizationId() throws Exception {
        String expectedJson = objectMapper.writeValueAsString( List.of( savedExampleEventResponse ) );

        mockMvc.perform( get( "/api/events/byOrga/" + savedOrganization.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Returns list of events found by user id")
    void getEventsByUserId() throws Exception {
        String expectedJson = objectMapper.writeValueAsString( List.of( savedExampleEventResponse ) );

        mockMvc.perform( get( "/api/events/byUser/" + savedAuthenticatedUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Returns empty list when no events found for user")
    void getEventsByUserId_returnsEmpty() throws Exception {
        String nonexistentUserId = "nonexistent";
        String expectedJson = objectMapper.writeValueAsString( List.of() );

        mockMvc.perform( get( "/api/events/byUser/" + nonexistentUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }


    @Test
    @DisplayName("Returns created event without image")
    void createEvent() throws Exception {
        String eventRequestDtoJson = objectMapper.writeValueAsString( exampleEventRequestDto );

        MockPart eventPart = new MockPart( "event", "eventRequest.json", eventRequestDtoJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/events/create" )
                        .part( eventPart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() )
                .andExpect( jsonPath( "$.imageId" ).isEmpty() )
                .andExpect( jsonPath( "$.title" ).value( exampleEventRequestDto.title() ) );
    }

    @Test
    @DisplayName("Returns created event with image")
    void createEvent_withImage() throws Exception {
        String eventRequestDtoJson = objectMapper.writeValueAsString( exampleEventRequestDto );

        MockPart imagePart = new MockPart( "imageFile", "testImage.jpg", "imageContent".getBytes() ) {{
            getHeaders().add( "Content-Type", "image/jpeg" );
        }};

        MockPart eventPart = new MockPart( "event", "eventRequest.json", eventRequestDtoJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/events/create" )
                        .part( imagePart )
                        .part( eventPart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() )
                .andExpect( jsonPath( "$.imageId" ).isNotEmpty() )
                .andExpect( jsonPath( "$.title" ).value( exampleEventRequestDto.title() ) );
    }

    @Test
    @DisplayName("Create event throws 401 when not authorized")
    void createEvent_throws401WhenNotAuthorized() throws Exception {
        Organization orgaWithForeignOwner = savedOrganization.toBuilder()
                .owners( Set.of( "foreignOwnerId" ) )
                .build();

        organizationRepo.save( orgaWithForeignOwner );

        String eventRequestDtoJson = objectMapper.writeValueAsString( exampleEventRequestDto );

        MockPart eventPart = new MockPart( "event", "eventRequest.json", eventRequestDtoJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/events/create" )
                        .part( eventPart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "Your are not an owner of the organization with id: " + exampleEventRequestDto.organizationId() ) );
    }

    @Test
    @DisplayName("Create event throws 400 when event date is invalid (present or past)")
    void createEvent_throws400WithInvalidDate() throws Exception {

        EventRequestDto eventRequestDtoWithFalseDate = exampleEventRequestDto.toBuilder()
                .eventDateTime( Instant.now() )
                .build();

        String eventRequestDtoJson = objectMapper.writeValueAsString( eventRequestDtoWithFalseDate );

        MockPart eventPart = new MockPart( "event", "eventRequest.json", eventRequestDtoJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/events/create" )
                        .part( eventPart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.error" ).value( "Event date and time must be in the future." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }
}