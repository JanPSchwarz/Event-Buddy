package org.eventbuddy.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eventbuddy.backend.TestcontainersConfiguration;
import org.eventbuddy.backend.configs.CustomOAuth2User;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.mockUser.WithCustomSuperAdmin;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.booking.Booking;
import org.eventbuddy.backend.models.booking.BookingRequestDto;
import org.eventbuddy.backend.models.booking.BookingResponseDto;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.event.EventResponseDto;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.repos.BookingRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@WithCustomMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTest {

    OAuth2AuthenticationToken oAuth2Token;
    String savedAuthenticatedUserId;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepo;

    @Autowired
    OrganizationRepository organizationRepo;

    @Autowired
    EventRepository eventRepo;

    @Autowired
    BookingRepository bookingRepo;

    Booking testBooking;
    BookingRequestDto testBookingRequestDto;
    BookingResponseDto testBookingResponseDto;

    @BeforeEach
    void setUp() {

        userRepo.deleteAll();
        organizationRepo.deleteAll();
        eventRepo.deleteAll();
        bookingRepo.deleteAll();

        // Save annotated test user to userRepo
        CustomOAuth2User customOAuth2User = ( CustomOAuth2User ) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        AppUser testUser = userRepo.save( customOAuth2User.getUser() );

        savedAuthenticatedUserId = testUser.getId();

        Location testLocation = Location.builder()
                .locationName( "Test Location" )
                .address( "Example Street 1" )
                .city( "Example City" )
                .country( "Example Country" )
                .build();

        Organization testOrganization = Organization.builder()
                .name( "Test Organization" )
                .id( "test-orga" )
                .slug( "test-organization" )
                .owners( Set.of( testUser.getId() ) )
                .location( testLocation )
                .build();

        OrganizationResponseDto testOrganizationResponseDto = OrganizationResponseDto.builder()
                .id( testOrganization.getId() )
                .name( testOrganization.getName() )
                .slug( testOrganization.getSlug() )
                .owners( Set.of() )
                .description( testOrganization.getDescription() )
                .location( testOrganization.getLocation() )
                .build();


        Event testEvent = Event.builder()
                .title( "Test Event" )
                .id( "test-event" )
                .eventDateTime( Instant.now().truncatedTo( ChronoUnit.MILLIS ) )
                .price( 0.0 )
                .bookedTicketsCount( 1 )
                .location( testLocation )
                .eventOrganization( testOrganization )
                .build();


        testBooking = Booking.builder()
                .name( "Test Booking" )
                .id( "test-booking" )
                .numberOfTickets( 2 )
                .event( testEvent )
                .userId( savedAuthenticatedUserId )
                .build();

        EventResponseDto eventResponseDto = EventResponseDto.builder()
                .id( testEvent.getId() )
                .eventOrganization( testOrganizationResponseDto )
                .title( testEvent.getTitle() )
                .eventDateTime( testEvent.getEventDateTime() )
                .price( testEvent.getPrice() )
                .location( testEvent.getLocation() )
                .bookedTicketsCount( testEvent.getBookedTicketsCount() )
                .build();

        testBookingRequestDto = BookingRequestDto.builder()
                .name( testBooking.getName() )
                .numberOfTickets( testBooking.getNumberOfTickets() )
                .eventId( testEvent.getId() )
                .userId( savedAuthenticatedUserId )
                .build();

        testBookingResponseDto = BookingResponseDto.builder()
                .bookingId( testBooking.getId() )
                .name( testBooking.getName() )
                .numberOfTickets( testBooking.getNumberOfTickets() )
                .hostingEvent( eventResponseDto )
                .build();

        userRepo.save( testUser );
        organizationRepo.save( testOrganization );
        eventRepo.save( testEvent );
        bookingRepo.save( testBooking );
    }

    @Test
    @DisplayName("Returns all bookings by user id")
    void getBookingsByUser() throws Exception {

        String expectedJson = objectMapper.writeValueAsString( List.of( testBookingResponseDto ) );

        mockMvc.perform( get( "/api/booking/byUser/" + savedAuthenticatedUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );

    }

    @Test
    @DisplayName("Returns all bookings by foreign user id when super admin")
    @WithCustomSuperAdmin
    void getBookingsByUser_whenSuperAdmin() throws Exception {
        // change booking user id to a different one
        testBooking = testBooking.toBuilder()
                .userId( "other-user" )
                .build();

        bookingRepo.save( testBooking );

        String expectedJson = objectMapper.writeValueAsString( List.of( testBookingResponseDto ) );

        mockMvc.perform( get( "/api/booking/byUser/" + testBooking.getUserId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }


    @Test
    @DisplayName("Returns 403 when not authorized")
    void getBookingsByUser_unauthorized() throws Exception {

        String otherUserId = "other-user";

        mockMvc.perform( get( "/api/booking/byUser/" + otherUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() )
                .andExpect( jsonPath( "$.error" ).value( "You do not have permission to perform this action." ) );
    }

    @Test
    @DisplayName("Returns booking when completed")
    void makeBooking() throws Exception {

        bookingRepo.deleteAll();

        testBookingResponseDto = testBookingResponseDto.toBuilder()
                .hostingEvent( testBookingResponseDto.hostingEvent().toBuilder()
                        .bookedTicketsCount( 3 ).build() )
                .build();

        String requestBody = objectMapper.writeValueAsString( testBookingRequestDto );

        mockMvc.perform( post( "/api/booking/makeBooking" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( requestBody ) )
                .andExpect( jsonPath( "$.hostingEvent.id" ).isNotEmpty() )
                .andExpect( jsonPath( "$.name" ).value( "Test Booking" ) )
                .andExpect( jsonPath( "$.bookingId" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Returns 403 when not logged in")
    void makeBooking_unauthenticated() throws Exception {

        SecurityContextHolder.getContext().getAuthentication().setAuthenticated( false );

        bookingRepo.deleteAll();

        String requestBody = objectMapper.writeValueAsString( testBookingRequestDto );

        mockMvc.perform( post( "/api/booking/makeBooking" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( requestBody ) )
                .andExpect( status().isForbidden() )
                .andExpect( jsonPath( "$.error" ).value( "You are not logged in or not allowed to perform this Action." ) );
    }

    @Test
    @DisplayName("Deletes booking")
    void deleteBookingById() throws Exception {
        mockMvc.perform( delete( "/api/booking/" + testBooking.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNoContent() );
    }

    @Test
    @DisplayName("Delete booking returns 403 when not authorized")
    void deleteBookingById_unauthorized() throws Exception {
        // change booking user id to a different one
        testBooking = testBooking.toBuilder()
                .userId( "other-user" )
                .build();

        bookingRepo.save( testBooking );

        mockMvc.perform( delete( "/api/booking/" + testBooking.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() )
                .andExpect( jsonPath( "$.error" ).value( "You do not have permission to perform this action." ) );
    }

    @Test
    @DisplayName("Delete foreign booking allowed when called by super admin")
    @WithCustomSuperAdmin
    void deleteBookingById_whenSuperAdmin() throws Exception {
        // change booking user id to a different one
        testBooking = testBooking.toBuilder()
                .userId( "other-user" )
                .build();

        bookingRepo.save( testBooking );

        mockMvc.perform( delete( "/api/booking/" + testBooking.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNoContent() );
    }
}