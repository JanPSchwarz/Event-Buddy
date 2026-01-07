package org.eventbuddy.backend.services;

import ch.qos.logback.classic.Logger;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.organization.*;
import org.eventbuddy.backend.repos.ImageRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


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
                .id( "exampleOwnerId" )
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
                .id( "exampleOrgaId" )
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

    @AfterEach
    void teardown() {
        ( ( Logger ) LoggerFactory.getLogger( OrganizationService.class ) ).detachAndStopAllAppenders();
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
    @DisplayName("Should return true when list with orga dto")
    void getAllOrganizations_shouldReturnTrueWhenListWithOrgaDto() {

        when( mockOrgaRepo.findAll() ).thenReturn( List.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( List.of( exampleUser ) );

        List<OrganizationResponseDto> actualList = organizationService.getAllOrganizations();

        assertEquals( actualList, List.of( exampleOrgaResponseDto ) );
        verify( mockUserRepo ).findAllById( exampleOrga.getOwners() );
        verify( mockOrgaRepo ).findAll();
    }

    @Test
    @DisplayName("Should throw true when owners not found for organization")
    void getAllOrganizations_shouldThrowWhenOwnersNotFoundForOrganization() {
        when( mockOrgaRepo.findAll() ).thenReturn( List.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( List.of() );

        assertThatThrownBy( () -> organizationService.getAllOrganizations() )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessageContaining( "One or more organization owners not found." );

        verify( mockOrgaRepo ).findAll();
    }

    @Test
    @DisplayName("Should return true when orga found")
    void getOrganizationDtoById_shouldReturnTrueWhenOrgaFound() {
        when( mockOrgaRepo.findById( "exampleOrgaId" ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( List.of( exampleUser ) );

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
                .id( "exampleOwnerId" )
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
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( List.of( exampleUserWithHiddenInfo ) );

        OrganizationResponseDto actualOrgaDto = organizationService.getOrganizationDtoById( "exampleOrgaId" );
        assertEquals( actualOrgaDto, mutatedOrgaResponseDto );
        verify( mockOrgaRepo ).findById( "exampleOrgaId" );
        verify( mockUserRepo ).findAllById( exampleOrga.getOwners() );
    }

    @Test
    @DisplayName("Should return true when orga found")
    void getOrganizationDtoBySlug_shouldReturnTrueWhenOrgaFound() {

        String givenSlug = "exampleSlug";

        when( mockOrgaRepo.findBySlug( givenSlug ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockUserRepo.findAllById( exampleOrga.getOwners() ) ).thenReturn( List.of( exampleUser ) );

        OrganizationResponseDto actualOrgaDto = organizationService.getOrganizationDtoBySlug( givenSlug );
        assertEquals( actualOrgaDto, exampleOrgaResponseDto );
        verify( mockOrgaRepo ).findBySlug( givenSlug );
        verify( mockUserRepo ).findAllById( exampleOrga.getOwners() );
    }

    @Test
    @DisplayName("Should throw when orga not found")
    void getOrganizationDtoBySlug_shouldThrowWhenOrgaNotFound() {
        String givenSlug = "exampleSlug";

        when( mockOrgaRepo.findBySlug( givenSlug ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> organizationService.getOrganizationDtoBySlug( givenSlug ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessageContaining( "Organization not found with slug: " + givenSlug );

        verify( mockOrgaRepo ).findBySlug( givenSlug );
    }

    @Test
    @DisplayName("Should return true when raw organization found")
    void getRawOrganizationById_shouldReturnTrueWhenFound() {
        when( mockOrgaRepo.findById( "exampleOrgaId" ) ).thenReturn( Optional.of( exampleOrga ) );

        Organization actualOrga = organizationService.getRawOrganizationById( "exampleOrgaId" );
        assertEquals( actualOrga, exampleOrga );
        verify( mockOrgaRepo ).findById( "exampleOrgaId" );
    }

    @Test
    @DisplayName("Should return raw organization when updated (maximal update)")
    void updateOrganization_shouldReturnWhenUpdatedFully() {

        String orgaIdToUpdate = "exampleOrgaId";

        Location updatedLocation = Location.builder()
                .address( "updatedAddress" )
                .city( "updatedCity" )
                .zipCode( "updatedZipCode" )
                .country( "updatedCountry" )
                .latitude( 987.654 )
                .longitude( 32.10 )
                .build();

        Contact updatedContact = Contact.builder()
                .email( "updatedEmail@example.com" )
                .phoneNumber( "9876543210" )
                .build();

        OrganizationRequestDto updatedOrga = OrganizationRequestDto.builder()
                .name( "updateName" )
                .description( "updatedDescription" )
                .website( "updatedWebsite" )
                .contact( updatedContact )
                .location( updatedLocation )
                .build();

        when( mockOrgaRepo.findById( orgaIdToUpdate ) ).thenReturn( Optional.of( exampleOrga ) );

        Organization updatedOrganization = exampleOrga.toBuilder()
                .name( "updateName" )
                .description( "updatedDescription" )
                .location( updatedLocation )
                .contact( updatedContact )
                .website( "updatedWebsite" )
                .build();

        when( mockOrgaRepo.save( updatedOrganization ) ).thenReturn( updatedOrganization );

        Organization actualUpdatedOrga = organizationService.updateOrganization( orgaIdToUpdate, updatedOrga );

        assertEquals( actualUpdatedOrga, updatedOrganization );
        verify( mockOrgaRepo ).findById( orgaIdToUpdate );
        verify( mockOrgaRepo ).save( updatedOrganization );
    }

    @Test
    @DisplayName("Should return true when owner added to orga and vice versa")
    void addOwnerToOrganization_shouldReturnTrueWhenOwnerAdded() {
        String orgaId = "exampleOrgaId";
        String userIdToAdd = "newOwnerId";

        AppUser newOwnerUser = exampleUser.toBuilder()
                .id( userIdToAdd )
                .name( "newOwnerName" )
                .build();

        AppUser updatedUser = newOwnerUser.toBuilder()
                .organizations( Set.of( orgaId ) )
                .build();

        Set<String> updatedOwners = Set.of( "exampleOwnerId", userIdToAdd );

        Organization updatedOrganization = exampleOrga.toBuilder()
                .owners( updatedOwners )
                .build();

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockOrgaRepo.save( updatedOrganization ) ).thenReturn( updatedOrganization );
        when( mockUserRepo.findById( userIdToAdd ) ).thenReturn( Optional.of( newOwnerUser ) );
        when( mockUserRepo.save( updatedUser ) ).thenReturn( updatedUser );

        Organization actualUpdatedOrga = organizationService.addOwnerToOrganization( orgaId, userIdToAdd );

        assertEquals( actualUpdatedOrga, updatedOrganization );
        verify( mockOrgaRepo ).findById( orgaId );
        verify( mockOrgaRepo ).save( updatedOrganization );
        verify( mockUserRepo, times( 2 ) ).findById( userIdToAdd );
        verify( mockUserRepo ).save( updatedUser );
    }

    @Test
    @DisplayName("Should throw when user to add not found")
    void addOwnerToOrganization_shouldThrowWhenUserNotFound() {
        String orgaId = "exampleOrgaId";
        String userIdToAdd = "nonExistingUserId";

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockUserRepo.findById( userIdToAdd ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> organizationService.addOwnerToOrganization( orgaId, userIdToAdd ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "User not found with id: " + userIdToAdd );

        verify( mockOrgaRepo ).findById( orgaId );
        verify( mockUserRepo ).findById( userIdToAdd );
    }

    @Test
    @DisplayName("Should throw when orga not found")
    void addOwnerToOrganization_shouldThrowWhenOrgaNotFound() {
        String orgaId = "nonExistingOrgaId";
        String userIdToAdd = "existingUserId";

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> organizationService.addOwnerToOrganization( orgaId, userIdToAdd ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Organization not found with id: " + orgaId );

        verify( mockOrgaRepo ).findById( orgaId );
    }

    @Test
    @DisplayName("Should return true when organization updated after owner deletion")
    void deleteOwnerFromOrganization_shouldReturnTrueWhenUpdated() {
        String orgaId = "exampleOrgaId";
        String ownerIdToDelete = "exampleOwnerId";

        AppUser modifiedExampleUser = exampleUser.toBuilder()
                .organizations( Set.of() )
                .build();

        Organization exampleOrgaWithTwoOwners = exampleOrga.toBuilder()
                .owners( Set.of( "exampleOwnerId", "anotherOwnerId" ) )
                .build();

        Organization expectedUpdatedOrga = exampleOrga.toBuilder()
                .owners( Set.of( "anotherOwnerId" ) )
                .build();

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.of( exampleOrgaWithTwoOwners ) );
        when( mockOrgaRepo.save( expectedUpdatedOrga ) ).thenReturn( expectedUpdatedOrga );
        when( mockUserRepo.findById( ownerIdToDelete ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.save( modifiedExampleUser ) ).thenReturn( modifiedExampleUser );

        Organization actualUpdatedOrga = organizationService.deleteOwnerFromOrganization( orgaId, ownerIdToDelete );


        assertEquals( expectedUpdatedOrga, actualUpdatedOrga );
        verify( mockOrgaRepo ).findById( orgaId );
        verify( mockOrgaRepo ).save( any( Organization.class ) );
        verify( mockUserRepo ).findById( ownerIdToDelete );
        verify( mockUserRepo ).save( modifiedExampleUser );
    }

    @Test
    @DisplayName("Should throw when organization not found")
    void deleteOwnerFromOrganization_shouldThrowWhenOrgaNotFound() {
        String orgaId = "nonExistingOrgaId";
        String ownerIdToDelete = "exampleOwnerId";

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> organizationService.deleteOwnerFromOrganization( orgaId, ownerIdToDelete ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Organization not found with id: " + orgaId );

        verify( mockOrgaRepo ).findById( orgaId );
    }

    @Test
    @DisplayName("Should throw when user cannot be removed from organization")
    void deleteOwnerFromOrganization_shouldThrowWhenConflict() {
        String orgaId = "exampleOrgaId";
        String ownerIdToDelete = "exampleOwnerId";

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.of( exampleOrga ) );

        assertThatThrownBy( () -> organizationService.deleteOwnerFromOrganization( orgaId, ownerIdToDelete ) )
                .isInstanceOf( IllegalStateException.class )
                .hasMessage( "Organization must have at least one owner." );

        verify( mockOrgaRepo ).findById( orgaId );
    }

    @Test
    @DisplayName("Should throw when user not found")
    void deleteOwnerFromOrganization_shouldThrowWhenUserNotFound() {
        String orgaId = "exampleOrgaId";
        String ownerIdToDelete = "nonExistingUserId";

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockUserRepo.findById( ownerIdToDelete ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> organizationService.deleteOwnerFromOrganization( orgaId, ownerIdToDelete ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "User not found with id: " + ownerIdToDelete );

        verify( mockOrgaRepo ).findById( orgaId );
        verify( mockUserRepo ).findById( ownerIdToDelete );
    }

    @Test
    @DisplayName("Should throw when user does not have organization to be removed from")
    void deleteOwnerFromOrganization_shouldThrowWhenUserHasIllegalState() {
        String orgaId = "exampleOrgaId";
        String userIdToDeleteOrgaFrom = "userToDeleteOrgaFrom";

        AppUser exampleUserWithoutOrga = AppUser.builder()
                .organizations( Set.of() )
                .build();

        Organization exampleOrgaWithFalseOwner = exampleOrga.toBuilder()
                .owners( Set.of( userIdToDeleteOrgaFrom, "someOtherOwner" ) )
                .build();

        when( mockOrgaRepo.findById( orgaId ) ).thenReturn( Optional.of( exampleOrgaWithFalseOwner ) );
        when( mockUserRepo.findById( userIdToDeleteOrgaFrom ) ).thenReturn( Optional.of( exampleUserWithoutOrga ) );

        assertThatThrownBy( () -> organizationService.deleteOwnerFromOrganization( orgaId, userIdToDeleteOrgaFrom ) )
                .isInstanceOf( IllegalStateException.class )
                .hasMessage( "User with id " + userIdToDeleteOrgaFrom + " is not part of any organizations." );

        verify( mockOrgaRepo ).findById( orgaId );
        verify( mockUserRepo ).findById( userIdToDeleteOrgaFrom );
    }

    @Test
    @DisplayName("Should return true when organization created")
    void createOrganization_shouldReturnTrueWhenCreated() {
        OrganizationRequestDto orgaToCreate = OrganizationRequestDto.builder()
                .name( "newOrgaName" )
                .description( "newOrgaDescription" )
                .website( "newOrgaWebsite" )
                .contact( exampleOrga.getContact() )
                .location( exampleOrga.getLocation() )
                .build();

        String imageId = "newOrgaImageId";

        Organization organizationToSave = Organization.builder()
                .name( orgaToCreate.name() )
                .description( orgaToCreate.description() )
                .website( orgaToCreate.website() )
                .contact( orgaToCreate.contact() )
                .location( orgaToCreate.location() )
                .owners( Set.of( exampleUser.getId() ) )
                .imageId( imageId )
                .build();

        Organization savedOrganization = organizationToSave.toBuilder()
                .id( "newOrgaId" )
                .createdDate( Instant.now() )
                .lastModifiedDate( Instant.now() )
                .slug( "neworga-slug" )
                .build();

        AppUser updatedExampleUser = exampleUser.toBuilder()
                .organizations( Set.of( "newOrgaId", "exampleOrgaId" ) )
                .build();

        when( mockOrgaRepo.save( organizationToSave ) ).thenReturn( savedOrganization );
        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.save( updatedExampleUser ) ).thenReturn( updatedExampleUser );

        Organization actualCreatedOrga = organizationService.createOrganization( orgaToCreate, exampleUser, imageId );

        assertEquals( actualCreatedOrga, savedOrganization );
        verify( mockOrgaRepo ).save( organizationToSave );
        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).save( updatedExampleUser );
    }

    @Test
    @DisplayName("Should return true when organization deleted")
    void deleteOrganizationById_shouldReturnTrueWhenDeleted() {
        String orgaIdToDelete = "exampleOrgaId";

        AppUser modifiedExampleUser = exampleUser.toBuilder()
                .organizations( Set.of() )
                .build();

        when( mockOrgaRepo.findById( orgaIdToDelete ) ).thenReturn( Optional.of( exampleOrga ) );
        when( mockUserRepo.findById( "exampleOwnerId" ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.save( modifiedExampleUser ) ).thenReturn( modifiedExampleUser );

        organizationService.deleteOrganizationById( orgaIdToDelete );

        verify( mockOrgaRepo ).findById( orgaIdToDelete );
        verify( mockOrgaRepo ).deleteById( orgaIdToDelete );
        verify( mockImageRepo ).deleteById( exampleOrga.getImageId() );
        verify( mockUserRepo ).findById( "exampleOwnerId" );
        verify( mockUserRepo ).save( modifiedExampleUser );
    }
}