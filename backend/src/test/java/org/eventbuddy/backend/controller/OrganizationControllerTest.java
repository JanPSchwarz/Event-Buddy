package org.eventbuddy.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eventbuddy.backend.TestcontainersConfiguration;
import org.eventbuddy.backend.enums.ImageType;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.mockUser.WithCustomMockUser;
import org.eventbuddy.backend.mockUser.WithCustomSuperAdmin;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationRequestDto;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@WithCustomMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrganizationControllerTest {

    String savedOrganizationName;
    String savedAuthenticatedUserId;
    OrganizationResponseDto savedOrganizationDto;
    OAuth2AuthenticationToken oAuth2Token;

    @Autowired
    UserRepository userRepo;
    @Autowired
    private OrganizationRepository organizationRepo;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        // Save annotated test user to userRepo
        userRepo.deleteAll();

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

        savedAuthenticatedUserId = savedUser.getId();


        // save test organization to organizationRepo
        organizationRepo.deleteAll();

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

        Organization savedOrganization = organizationRepo.save( testOrganization );

        savedOrganizationName = savedOrganization.getName();
    }

    @Test
    @DisplayName("Should return list of organization dtos")
    void getAllOrganizations() throws Exception {
        String expectedJson = objectMapper.writeValueAsString( List.of( savedOrganizationDto ) );

        mockMvc.perform( get( "/api/organization/all" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );

    }

    @Test
    @DisplayName("Should return empty list when no organizations")
    void getAllOrganizations_returnsEmpty() throws Exception {
        organizationRepo.deleteAll();
        String expectedJson = objectMapper.writeValueAsString( List.of() );

        mockMvc.perform( get( "/api/organization/all" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );

    }

    @Test
    @WithCustomSuperAdmin
    @DisplayName("Should return list of organizations")
    void getAllRawOrganizations() throws Exception {
        Organization savedTestOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();
        String expectedJson = objectMapper.writeValueAsString( List.of( savedTestOrga ) );

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println( "Authenticated user: " + auth );

        mockMvc.perform( get( "/api/organization/allRaw" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Should return empty list of organizations when not found")
    @WithCustomSuperAdmin
    void getAllRawOrganizations_returnEmpty() throws Exception {
        organizationRepo.deleteAll();
        String expectedJson = objectMapper.writeValueAsString( List.of() );

        mockMvc.perform( get( "/api/organization/allRaw" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Should throw 401 when not authorized as super admin")
    void getAllRawOrganizations_throws401WhenNotSuperAdmin() throws Exception {
        mockMvc.perform( get( "/api/organization/allRaw" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not allowed to perform this action." ) );
    }

    @Test
    @DisplayName("Should throw 401 when not authenticated")
    void getAllRawOrganizations_throws401WhenNotAuthenticated() throws Exception {
        oAuth2Token.setAuthenticated( false );
        mockMvc.perform( get( "/api/organization/allRaw" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not logged in." ) );
    }

    @Test
    @DisplayName("Should return orga dto found by id")
    void getOrganizationById() throws Exception {

        Organization savedOrganization = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        String expectedJson = objectMapper.writeValueAsString( savedOrganizationDto );

        mockMvc.perform( get( "/api/organization/" + savedOrganization.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Should throw 404 when orga not found by id")
    void getOrganizationById_throws404() throws Exception {
        String nonExistingId = "nonExistingId";

        mockMvc.perform( get( "/api/organization/" + nonExistingId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "Organization not found with id: " + nonExistingId ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should return orga dto found by slug")
    void getOrganizationBySlug() throws Exception {

        Organization savedOrganization = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        String expectedJson = objectMapper.writeValueAsString( savedOrganizationDto );

        mockMvc.perform( get( "/api/organization/slug/" + savedOrganization.getSlug() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( content().json( expectedJson ) );
    }

    @Test
    @DisplayName("Should throw 404 when orga not found by slug")
    void getOrganizationBySlug_throws404() throws Exception {
        String nonExistingSlug = "nonExistingSlug";

        mockMvc.perform( get( "/api/organization/slug/" + nonExistingSlug )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "Organization not found with slug: " + nonExistingSlug ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should return created organization without image")
    void createOrganization() throws Exception {

        OrganizationRequestDto createOrgaData = OrganizationRequestDto.builder()
                .name( "New Organization without image" )
                .location( Location.builder()
                        .address( "newStreet 1" )
                        .city( "newCity" )
                        .zipCode( "54321" )
                        .country( "NewCountry" )
                        .build() )
                .build();

        String organizationJson = objectMapper.writeValueAsString( createOrgaData );

        MockPart organizationPart = new MockPart( "organization", "organization.json", organizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization" )
                        .part( organizationPart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( "New Organization without image" ) )
                .andExpect( jsonPath( "$.location.city" ).value( "newCity" ) )
                .andExpect( jsonPath( "$.imageId" ).isEmpty() )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 401 when not authenticated")
    void createOrganization_throws401WhenNotAuthenticated() throws Exception {

        oAuth2Token.setAuthenticated( false );

        OrganizationRequestDto createOrgaData = OrganizationRequestDto.builder()
                .name( "New Organization without image" )
                .location( Location.builder()
                        .address( "newStreet 1" )
                        .city( "newCity" )
                        .zipCode( "54321" )
                        .country( "NewCountry" )
                        .build() )
                .build();

        String organizationJson = objectMapper.writeValueAsString( createOrgaData );

        MockPart organizationPart = new MockPart( "organization", "organization.json", organizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization" )
                        .part( organizationPart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "User is not logged in." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @ParameterizedTest
    @ValueSource(strings = { "image/jpeg", "image/webp", "image/png", "image/heic", "image/svg+xml" })
    @DisplayName("Should return created organization with image")
    void createOrganization_withImage( String imageType ) throws Exception {

        OrganizationRequestDto createOrgaData = OrganizationRequestDto.builder()
                .name( "New Organization with image" )
                .location( Location.builder()
                        .address( "newStreet 1" )
                        .city( "newCity" )
                        .zipCode( "54321" )
                        .country( "NewCountry" )
                        .build() )
                .build();

        String organizationJson = objectMapper.writeValueAsString( createOrgaData );

        MockPart organizationPart = new MockPart( "organization", "organization.json", organizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        MockPart imagePart = new MockPart( "image", "image", "dummyImageContent".getBytes() ) {{
            getHeaders().add( "Content-Type", imageType );
        }};

        mockMvc.perform( multipart( "/api/organization" )
                        .part( organizationPart )
                        .part( imagePart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.name" ).value( "New Organization with image" ) )
                .andExpect( jsonPath( "$.location.city" ).value( "newCity" ) )
                .andExpect( jsonPath( "$.imageId" ).isNotEmpty() )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 400 when image type invalid")
    void createOrganization_Throws400WithInvalidImage() throws Exception {

        OrganizationRequestDto createOrgaData = OrganizationRequestDto.builder()
                .name( "New Organization with image" )
                .location( Location.builder()
                        .address( "newStreet 1" )
                        .city( "newCity" )
                        .zipCode( "54321" )
                        .country( "NewCountry" )
                        .build() )
                .build();

        String organizationJson = objectMapper.writeValueAsString( createOrgaData );

        MockPart organizationPart = new MockPart( "organization", "organization.json", organizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        MockPart imagePart = new MockPart( "image", "image.pdf", "dummyImageContent".getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_PDF_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization" )
                        .part( organizationPart )
                        .part( imagePart )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.error" ).value( "Unsupported image type: " + imagePart.getContentType() + " Allowed types are: " + String.join( ", ", ImageType.getAllFileTypes() ) ) );
    }

    @Test
    @DisplayName("Should throw 413 when image type too large")
    void createOrganization_Throws413WhenImageTooLarge() throws Exception {

        String maxFileSize = "5MB";

        OrganizationRequestDto createOrgaData = OrganizationRequestDto.builder()
                .name( "New Organization with image" )
                .location( Location.builder()
                        .address( "newStreet 1" )
                        .city( "newCity" )
                        .zipCode( "54321" )
                        .country( "NewCountry" )
                        .build() )
                .build();

        String organizationJson = objectMapper.writeValueAsString( createOrgaData );

        MockPart organizationPart = new MockPart( "organization", "organization.json", organizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        int fileSizeInBytes = 6 * 1024 * 1024; // 6MB
        byte[] largeImageContent = new byte[fileSizeInBytes];

        MockPart imagePart = new MockPart( "image", "image.png", largeImageContent ) {{
            getHeaders().add( "Content-Type", MediaType.IMAGE_PNG_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization" )
                        .part( organizationPart )
                        .part( imagePart )
                        .contentType( MediaType.MULTIPART_FORM_DATA )
                )
                .andExpect( status().isPayloadTooLarge() )
                .andExpect( jsonPath( "$.error" ).value( "Uploaded file exceeds the maximum allowed size: " + maxFileSize ) );
    }

    @Test
    @DisplayName("Should return updated orga")
    void updateOrganization() throws Exception {

        Organization orgaToUpdate = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        OrganizationRequestDto updateData = OrganizationRequestDto.builder()
                .name( "Updated Organization Name" )
                .build();

        String updatedOrganizationJson = objectMapper.writeValueAsString( updateData );

        MockPart organizationPart = new MockPart( "updateOrganization", "organization.json", updatedOrganizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization/" + orgaToUpdate.getId() )
                        .part( organizationPart )
                        .with( request -> {
                            request.setMethod( "PUT" );
                            return request;
                        } )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( orgaToUpdate.getId() ) )
                .andExpect( jsonPath( "$.name" ).value( updateData.name() ) );
    }

    @Test
    @DisplayName("Should return updated orga with image")
    void updateOrganization_withImage() throws Exception {

        Organization orgaToUpdate = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        OrganizationRequestDto updateData = OrganizationRequestDto.builder()
                .name( "Updated Organization Name" )
                .build();

        String updatedOrganizationJson = objectMapper.writeValueAsString( updateData );

        MockPart organizationPart = new MockPart( "updateOrganization", "organization.json", updatedOrganizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        MockPart imagePart = new MockPart( "image", "image.png", "imageContent".getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.IMAGE_PNG_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization/" + orgaToUpdate.getId() )
                        .part( organizationPart )
                        .part( imagePart )
                        .with( request -> {
                            request.setMethod( "PUT" );
                            return request;
                        } )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( orgaToUpdate.getId() ) )
                .andExpect( jsonPath( "$.name" ).value( updateData.name() ) )
                .andExpect( jsonPath( "$.imageId" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 401 when not authenticated")
    void updateOrganization_throws401WhenNotAuthenticated() throws Exception {
        oAuth2Token.setAuthenticated( false );
        Organization orgaToUpdate = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        OrganizationRequestDto updateData = OrganizationRequestDto.builder()
                .name( "Updated Organization Name" )
                .build();

        String updatedOrganizationJson = objectMapper.writeValueAsString( updateData );

        MockPart organizationPart = new MockPart( "updateOrganization", "organization.json", updatedOrganizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization/" + orgaToUpdate.getId() )
                        .part( organizationPart )
                        .with( request -> {
                            request.setMethod( "PUT" );
                            return request;
                        } )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() )
                .andExpect( jsonPath( "$.error" ).value( "User is not logged in." ) );
    }

    @Test
    @DisplayName("Should throw 401 when called by non owner (only USER role)")
    void updateOrganization_throws401WhenNotAuthorized() throws Exception {
        Organization orgaToUpdate = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        Organization orgaToUpdateWithForeignOwner = orgaToUpdate.toBuilder()
                .owners( Set.of( "otherUserId" ) )
                .build();

        organizationRepo.save( orgaToUpdateWithForeignOwner );

        OrganizationRequestDto updateData = OrganizationRequestDto.builder()
                .name( "Updated Organization Name" )
                .build();

        String updatedOrganizationJson = objectMapper.writeValueAsString( updateData );

        MockPart organizationPart = new MockPart( "updateOrganization", "organization.json", updatedOrganizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization/" + orgaToUpdate.getId() )
                        .part( organizationPart )
                        .with( request -> {
                            request.setMethod( "PUT" );
                            return request;
                        } )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() )
                .andExpect( jsonPath( "$.error" ).value( "You are not allowed to perform this action." ) );
    }

    @Test
    @DisplayName("Should return updated (foreign) orga when called by super admin")
    @WithCustomSuperAdmin
    void updateOrganization_returnsUpdatedOrgaWhenCalledBySuperAdmin() throws Exception {
        Organization orgaToUpdate = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        Organization orgaToUpdateWithForeignOwner = orgaToUpdate.toBuilder()
                .owners( Set.of( "otherUserId" ) )
                .build();

        organizationRepo.save( orgaToUpdateWithForeignOwner );

        OrganizationRequestDto updateData = OrganizationRequestDto.builder()
                .name( "Updated Organization Name" )
                .build();

        String updatedOrganizationJson = objectMapper.writeValueAsString( updateData );

        MockPart organizationPart = new MockPart( "updateOrganization", "organization.json", updatedOrganizationJson.getBytes() ) {{
            getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
        }};

        mockMvc.perform( multipart( "/api/organization/" + orgaToUpdate.getId() )
                        .part( organizationPart )
                        .with( request -> {
                            request.setMethod( "PUT" );
                            return request;
                        } )
                        .contentType( MediaType.MULTIPART_FORM_DATA ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( orgaToUpdate.getId() ) )
                .andExpect( jsonPath( "$.name" ).value( updateData.name() ) );
    }

    @Test
    @DisplayName("Should return orga when owner added")
    void addOwnerToOrganization() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        AppUser userToAdd = AppUser.builder()
                .name( "newOwner" )
                .providerId( "testProvider_newOwner" )
                .role( Role.USER )
                .email( "exmaple@example.com" )
                .userSettings( UserSettings.builder()
                        .userVisible( true )
                        .showAvatar( true )
                        .showOrgas( true )
                        .showEmail( true )
                        .build() )
                .build();

        AppUser savedAddedUser = userRepo.save( userToAdd );

        mockMvc.perform( put( "/api/organization/addOwner/" + savedOrga.getId() + "/" + savedAddedUser.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.owners" ).isArray() )
                .andExpect( jsonPath( "$.owners" ).value( Matchers.hasSize( 2 ) ) )
                .andExpect( jsonPath( "$.id" ).value( savedOrga.getId() ) );
    }


    @Test
    @DisplayName("Should throw 404 when new owner not found")
    void addOwnerToOrganization_throws404WhenUserNotFound() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        String nonExistingUserId = "nonExistingUserId";

        mockMvc.perform( put( "/api/organization/addOwner/" + savedOrga.getId() + "/" + nonExistingUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "User not found with id: " + nonExistingUserId ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 404 when new orga not found")
    void addOwnerToOrganization_throws401WhenNotLoggedIn() throws Exception {
        String nonExistingOrgaId = "nonExistingOrgaId";
        String nonExistingUserId = "nonExistingUserId";

        mockMvc.perform( put( "/api/organization/addOwner/" + nonExistingOrgaId + "/" + nonExistingUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "Organization not found with id: " + nonExistingOrgaId ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 401 when request user not authenticated")
    void addOwnerToOrganization_throws401WhenNotAuthenticated() throws Exception {
        oAuth2Token.setAuthenticated( false );

        String nonExistingOrgaId = "nonExistingOrgaId";
        String nonExistingUserId = "nonExistingUserId";

        mockMvc.perform( put( "/api/organization/addOwner/" + nonExistingOrgaId + "/" + nonExistingUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "User is not logged in." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 401 when request user not authorized")
    void addOwnerToOrganization_throws401WhenNotAuthorized() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        Organization savedOrgaWithForeignOwner = savedOrga.toBuilder()
                .owners( Set.of( "otherUserId" ) )
                .build();

        organizationRepo.save( savedOrgaWithForeignOwner );

        mockMvc.perform( put( "/api/organization/addOwner/" + savedOrga.getId() + "/" + "otherUserId" )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not allowed to perform this action." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should return (foreign) orga when owner added by super admin")
    @WithCustomSuperAdmin
    void addOwnerToOrganization_shouldReturnUpdatedOrgaWhenCalledBySuperAdmin() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        AppUser userToAdd = AppUser.builder()
                .name( "otherUser" )
                .providerId( "testProvider_otherUser" )
                .role( Role.USER )
                .email( "example@example.com" )
                .userSettings( UserSettings.builder()
                        .userVisible( true )
                        .showAvatar( true )
                        .showOrgas( true )
                        .showEmail( true )
                        .build() )
                .build();


        AppUser savedUserToAdd = userRepo.save( userToAdd );

        Organization savedOrgaWithForeignOwner = savedOrga.toBuilder()
                .owners( Set.of( "randomOwnerId" ) )
                .build();

        organizationRepo.save( savedOrgaWithForeignOwner );

        mockMvc.perform( put( "/api/organization/addOwner/" + savedOrga.getId() + "/" + savedUserToAdd.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.owners" ).isArray() )
                .andExpect( jsonPath( "$.owners" ).value( Matchers.hasSize( 2 ) ) )
                .andExpect( jsonPath( "$.id" ).value( savedOrga.getId() ) );
    }

    @Test
    @DisplayName("Should remove owner from organization")
    void removeOwnerFromOrganization() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        AppUser ownerToRemove = AppUser.builder()
                .name( "ownerToRemove" )
                .providerId( "testProvider_ownerToRemove" )
                .userSettings( UserSettings.builder()
                        .showAvatar( true )
                        .showOrgas( true )
                        .showEmail( true )
                        .userVisible( true )
                        .build() )
                .role( Role.USER )
                .organizations( Set.of( savedOrga.getId() ) )
                .email( "ownerToRemove@example.com" )
                .build();

        AppUser savedOwnerToRemove = userRepo.save( ownerToRemove );

        Organization orgaWithMultipleOwners = savedOrga.toBuilder()
                .owners( Set.of( savedAuthenticatedUserId, savedOwnerToRemove.getId() ) )
                .build();

        Organization savedOrgaWithMultipleOwners = organizationRepo.save( orgaWithMultipleOwners );

        mockMvc.perform( put( "/api/organization/removeOwner/" + savedOrgaWithMultipleOwners.getId() + "/" + savedOwnerToRemove.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.owners" ).isArray() )
                .andExpect( jsonPath( "$.owners" ).value( Matchers.hasSize( 1 ) ) )
                .andExpect( jsonPath( "$.id" ).value( savedOrga.getId() ) );
    }

    @Test
    @DisplayName("Should remove (foreign) owner from organization when called by super admin")
    @WithCustomSuperAdmin
    void removeOwnerFromOrganization_shouldRemoveWhenCalledBySuperAdmin() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        AppUser ownerToRemove = AppUser.builder()
                .name( "ownerToRemove" )
                .providerId( "testProvider_ownerToRemove" )
                .userSettings( UserSettings.builder()
                        .showAvatar( true )
                        .showOrgas( true )
                        .showEmail( true )
                        .userVisible( true )
                        .build() )
                .role( Role.USER )
                .organizations( Set.of( savedOrga.getId() ) )
                .email( "ownerToRemove@example.com" )
                .build();

        AppUser secondOwnerToRemove = AppUser.builder()
                .name( "secondOwnerToRemove" )
                .providerId( "testProvider_secondOwnerToRemove" )
                .userSettings( UserSettings.builder()
                        .showAvatar( true )
                        .showOrgas( true )
                        .showEmail( true )
                        .userVisible( true )
                        .build() )
                .role( Role.USER )
                .organizations( Set.of( savedOrga.getId() ) )
                .email( "secondowner@exmaple.com" )
                .build();

        AppUser savedOwnerToRemove = userRepo.save( ownerToRemove );
        AppUser savedSecondOwnerToRemove = userRepo.save( secondOwnerToRemove );

        Organization orgaWithMultipleOwners = savedOrga.toBuilder()
                .owners( Set.of( savedSecondOwnerToRemove.getId(), savedOwnerToRemove.getId() ) )
                .build();

        Organization savedOrgaWithMultipleOwners = organizationRepo.save( orgaWithMultipleOwners );

        mockMvc.perform( put( "/api/organization/removeOwner/" + savedOrgaWithMultipleOwners.getId() + "/" + savedOwnerToRemove.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.owners" ).isArray() )
                .andExpect( jsonPath( "$.owners" ).value( Matchers.hasSize( 1 ) ) )
                .andExpect( jsonPath( "$.id" ).value( savedOrga.getId() ) );
    }

    @Test
    @DisplayName("Should throw 401 when not authorized")
    void removeOwnerFromOrganization_throws401WhenNotAuthorized() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        AppUser ownerToRemove = AppUser.builder()
                .name( "ownerToRemove" )
                .providerId( "testProvider_ownerToRemove" )
                .userSettings( UserSettings.builder()
                        .showAvatar( true )
                        .showOrgas( true )
                        .showEmail( true )
                        .userVisible( true )
                        .build() )
                .role( Role.USER )
                .organizations( Set.of( savedOrga.getId() ) )
                .email( "ownerToRemove@example.com" )
                .build();

        AppUser secondOwnerToRemove = AppUser.builder()
                .name( "secondOwnerToRemove" )
                .providerId( "testProvider_secondOwnerToRemove" )
                .userSettings( UserSettings.builder()
                        .showAvatar( true )
                        .showOrgas( true )
                        .showEmail( true )
                        .userVisible( true )
                        .build() )
                .role( Role.USER )
                .organizations( Set.of( savedOrga.getId() ) )
                .email( "secondowner@exmaple.com" )
                .build();

        AppUser savedOwnerToRemove = userRepo.save( ownerToRemove );
        AppUser savedSecondOwnerToRemove = userRepo.save( secondOwnerToRemove );

        Organization orgaWithMultipleOwners = savedOrga.toBuilder()
                .owners( Set.of( savedSecondOwnerToRemove.getId(), savedOwnerToRemove.getId() ) )
                .build();

        Organization savedOrgaWithMultipleOwners = organizationRepo.save( orgaWithMultipleOwners );

        mockMvc.perform( put( "/api/organization/removeOwner/" + savedOrgaWithMultipleOwners.getId() + "/" + savedOwnerToRemove.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not allowed to perform this action." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 401 when not authenticated")
    void removeOwnerFromOrganization_throws401WhenNotAuthenticated() throws Exception {
        oAuth2Token.setAuthenticated( false );
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        mockMvc.perform( put( "/api/organization/removeOwner/" + savedOrga.getId() + "/" + savedAuthenticatedUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "User is not logged in." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 409 when conflict removing last owner")
    void removeOwnerFromOrganization_throws409RemovingLastOwner() throws Exception {
        Organization savedOrga = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        mockMvc.perform( put( "/api/organization/removeOwner/" + savedOrga.getId() + "/" + savedAuthenticatedUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isConflict() )
                .andExpect( jsonPath( "$.error" ).value( "Organization must have at least one owner." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should throw 404 when organization not found")
    void removeOwnerFromOrganization_throws404WhenOrgaNotFound() throws Exception {
        String nonExistingOrgaId = "nonExistingOrgaId";

        mockMvc.perform( put( "/api/organization/removeOwner/" + nonExistingOrgaId + "/" + savedAuthenticatedUserId )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNotFound() )
                .andExpect( jsonPath( "$.error" ).value( "Organization not found with id: " + nonExistingOrgaId ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should return empty (204) when deleted")
    void deleteOrganization() throws Exception {
        Organization orgaToDelete = organizationRepo.findByName( savedOrganizationName ).orElseThrow();
        AppUser savedUser = userRepo.findById( savedAuthenticatedUserId ).orElseThrow();

        AppUser savedUserAsOwner = savedUser.toBuilder()
                .organizations( Set.of( orgaToDelete.getId() ) )
                .build();

        userRepo.save( savedUserAsOwner );

        mockMvc.perform( delete( "/api/organization/" + orgaToDelete.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNoContent() )
                .andExpect( content().string( "" ) );
    }

    @Test
    @DisplayName("Should return empty 401 when not authenticated")
    void deleteOrganization_throws401WhenNotAuthenticated() throws Exception {
        oAuth2Token.setAuthenticated( false );

        Organization orgaToDelete = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        mockMvc.perform( delete( "/api/organization/" + orgaToDelete.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "User is not logged in." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should return empty 401 when not authorized")
    void deleteOrganization_throws401WhenNotAuthorized() throws Exception {
        Organization orgaToDelete = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        Organization orgaToDeleteWithForeignOwner = orgaToDelete.toBuilder()
                .owners( Set.of( "otherUserId" ) )
                .build();

        organizationRepo.save( orgaToDeleteWithForeignOwner );

        mockMvc.perform( delete( "/api/organization/" + orgaToDelete.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() )
                .andExpect( jsonPath( "$.error" ).value( "You are not allowed to perform this action." ) )
                .andExpect( jsonPath( "$.id" ).isNotEmpty() );
    }

    @Test
    @DisplayName("Should return 204 when called by super admin")
    @WithCustomSuperAdmin
    void deleteOrganization_returns204WhenCalledBySuperAdmin() throws Exception {
        Organization orgaToDelete = organizationRepo.findByName( savedOrganizationName ).orElseThrow();

        AppUser foreignUser = AppUser.builder()
                .name( "foreignUser" )
                .email( "foreign@example.com" )
                .userSettings( UserSettings.builder()
                        .userVisible( true )
                        .showEmail( true )
                        .showOrgas( true )
                        .showAvatar( true )
                        .build() )
                .organizations( Set.of( orgaToDelete.getId() ) )
                .build();

        AppUser savedForeignUser = userRepo.save( foreignUser );

        Organization orgaToDeleteWithForeignOwner = orgaToDelete.toBuilder()
                .owners( Set.of( savedForeignUser.getId() ) )
                .build();

        organizationRepo.save( orgaToDeleteWithForeignOwner );

        mockMvc.perform( delete( "/api/organization/" + orgaToDelete.getId() )
                        .contentType( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isNoContent() )
                .andExpect( content().string( "" ) );
    }
}