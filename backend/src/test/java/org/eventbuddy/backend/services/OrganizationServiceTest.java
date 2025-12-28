package org.eventbuddy.backend.services;

import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.organization.Contact;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.repos.ImageRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    OrganizationRepository mockOrgaRepo;

    @Mock
    UserRepository mockUserRepo;

    @Mock
    ImageRepository mockImageRepo;

    @InjectMocks
    OrganizationService organizationService;

    Organization exampleOrga;
    OrganizationResponseDto exampleOrgaResponseDto;
    AppUser exampleUser;
    AppUserDto exampleUserDto;

    @BeforeEach
    void setUp() {

        Contact exampleContact = Contact.builder()
                .email( "example@example.com" )
                .phoneNumber( "1234567890" )
                .build();

        Location exampleLocation = Location.builder()
                .address( "123 Example St" )
                .city( "Example City" )
                .zipCode( "12345" )
                .country( "Example Country" )
                .latitude( 123.456 )
                .longitude( 78.90 )
                .build();

        AppUserDto exampleOwner = AppUserDto.builder()
                .name( "exampleUserName" )
                .email( "exampleUser@example.com" )
                .avatarUrl( "exampleUserAvatarUrl" )
                .build();

        UserSettings exampleUserSettings = UserSettings.builder()
                .showOrgas( true )
                .userVisible( true )
                .showAvatar( true )
                .showEmail( true )
                .build();

        exampleOrga = Organization.builder()
                .imageId( "exampleImageId" )
                .slug( "exampleSlug" )
                .contact( exampleContact )
                .location( exampleLocation )
                .createdDate( Instant.now() )
                .lastModifiedDate( Instant.now() )
                .id( "exampleOrgaId" )
                .description( "exampleDescription" )
                .name( "exampleName" )
                .owners( Set.of( "exampleOwnerId" ) )
                .website( "exampleWebsite" )
                .build();

        exampleOrgaResponseDto = OrganizationResponseDto.builder()
                .slug( "exampleSlug" )
                .description( "exampleDescription" )
                .name( "exampleName" )
                .contact( exampleContact )
                .location( exampleLocation )
                .imageId( "exampleImageId" )
                .website( "exampleWebsite" )
                .owners( Set.of( exampleOwner ) )
                .build();

        exampleUser = AppUser.builder()
                .id( "exampleOwnerId" )
                .name( "exampleUserName" )
                .role( Role.USER )
                .organizations( Set.of( "exampleOrgaId" ) )
                .userSettings( exampleUserSettings )
                .email( "exampleUser@example.com" )
                .avatarUrl( "exampleUserAvatarUrl" )
                .providerId( "github_123" )
                .build();

        exampleUserDto = exampleOwner.toBuilder()
                .organizations( List.of( exampleOrgaResponseDto ) )
                .build();
    }

    @Test
    @DisplayName("Should return true when list is empty")
    void getAllOrganizations_shouldReturnTrueWhenEmpty() {
        List<OrganizationResponseDto> expectedList = List.of();

        when( mockOrgaRepo.findAll() ).thenReturn( List.of() );

        List<OrganizationResponseDto> actualList = organizationService.getAllOrganizations();

        assertEquals( expectedList, actualList );
        verify( mockOrgaRepo ).findAll();
    }

    @Test
    @DisplayName("Should return true when list with OrganizationDto")
    void getAllOrganizations_shouldReturnTrueWhenListWithOrgaDto() {

        when( mockOrgaRepo.findAll() ).thenReturn( List.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( Optional.of( List.of( exampleUser ) ) );

        List<OrganizationResponseDto> actualList = organizationService.getAllOrganizations();

        assertEquals( actualList, List.of( exampleOrgaResponseDto ) );
        verify( mockUserRepo ).findAllById( exampleOrga.getOwners() );
        verify( mockOrgaRepo ).findAll();
    }

    @Test
    @DisplayName("Should throw true when owners not found for organization")
    void getAllOrganizations_shouldThrowWhenOwnersNotFoundForOrganization() {
        when( mockOrgaRepo.findAll() ).thenReturn( List.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> organizationService.getAllOrganizations() )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessageContaining( "One or more organization owners not found." );

        verify( mockOrgaRepo ).findAll();
    }

    @Test
    @DisplayName("Should return true when orga found")
    void getOrganizationDtoById_shouldReturnTrueWhenOrgaFound() {
        when( mockOrgaRepo.findById( "exampleOrgaId" ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( Optional.of( List.of( exampleUser ) ) );

        OrganizationResponseDto actualOrgaDto = organizationService.getOrganizationDtoById( "exampleOrgaId" );
        assertEquals( actualOrgaDto, exampleOrgaResponseDto );
        verify( mockOrgaRepo ).findById( "exampleOrgaId" );
        verify( mockUserRepo ).findAllById( exampleOrga.getOwners() );
    }

    @Test
    @DisplayName("Should return true when orga found (with hidden user info)")
    void getOrganizationDtoById_shouldReturnTrueWhenOrgaFoundWithHiddenUserInfo() {

        AppUserDto exampleUserDtoWithHiddenInfo = AppUserDto.builder()
                .name( "exampleUserName" )
                .email( null )
                .avatarUrl( null )
                .build();

        AppUser exampleUserWithHiddenInfo = exampleUser.toBuilder()
                .userSettings( UserSettings.builder()
                        .userVisible( true )
                        .showOrgas( false )
                        .showAvatar( false )
                        .showEmail( false )
                        .build() )
                .build();

        OrganizationResponseDto mutatedOrgaResponseDto = exampleOrgaResponseDto.toBuilder()
                .owners( Set.of( exampleUserDtoWithHiddenInfo ) )
                .build();

        when( mockOrgaRepo.findById( "exampleOrgaId" ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( Optional.of( List.of( exampleUserWithHiddenInfo ) ) );

        OrganizationResponseDto actualOrgaDto = organizationService.getOrganizationDtoById( "exampleOrgaId" );
        assertEquals( actualOrgaDto, mutatedOrgaResponseDto );
        verify( mockOrgaRepo ).findById( "exampleOrgaId" );
        verify( mockUserRepo ).findAllById( exampleOrga.getOwners() );
    }

    @Test
    void getOrganizationDtoBySlug() {
    }

    @Test
    void getAllRawOrganizations() {
    }

    @Test
    void getRawOrganizationById() {
    }

    @Test
    void updateOrganization() {
    }

    @Test
    void addOwnerToOrganization() {
    }

    @Test
    void deleteOwnerFromOrganization() {
    }

    @Test
    void createOrganization() {
    }

    @Test
    void deleteOrganizationById() {
    }
}