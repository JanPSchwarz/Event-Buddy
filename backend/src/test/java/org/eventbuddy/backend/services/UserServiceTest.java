package org.eventbuddy.backend.services;

import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.AppUserDto;
import org.eventbuddy.backend.models.app_user.AppUserUpdateDto;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.organization.Contact;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.models.organization.OrganizationResponseDto;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository mockUserRepo;

    @Mock
    OrganizationRepository mockOrganizationRepo;

    @InjectMocks
    UserService userService;

    UserSettings exampleUserSettings = UserSettings.builder()
            .userVisible( true )
            .showEmail( true )
            .showAvatar( true )
            .showOrgas( true )
            .build();

    Contact exampleOrgaContact = Contact.builder()
            .email( "exampleOrga@example.com" )
            .phoneNumber( "1234567890" )
            .build();

    Location exampleOrgaLocation = Location.builder()
            .address( "exampleStreet 1" )
            .city( "exampleCity" )
            .zipCode( "12345" )
            .country( "exampleCountry" )
            .build();

    AppUser exampleUser = AppUser.builder()
            .providerId( "github_123" )
            .id( "exampleUserId" )
            .email( "example@example.com" )
            .role( Role.USER )
            .userSettings( exampleUserSettings )
            .name( "exampleName" )
            .avatarUrl( "exampleAvatarUrl" )
            .organizations( Set.of( "exampleOrgaId" ) )
            .build();

    Organization exampleOrganization = Organization.builder()
            .name( "exampleOrgaName" )
            .slug( "exampleSlug" )
            .id( "exampleOrgaId" )
            .contact( exampleOrgaContact )
            .owners( Set.of( exampleUser.getId() ) )
            .imageId( "exampleImageId" )
            .location( exampleOrgaLocation )
            .website( "exampleWebsite" )
            .description( "exampleDescription" )
            .build();

    OrganizationResponseDto exampleOrgaDto = OrganizationResponseDto.builder()
            .name( exampleOrganization.getName() )
            .id( exampleOrganization.getId() )
            .slug( exampleOrganization.getSlug() )
            .contact( exampleOrganization.getContact() )
            .location( exampleOrganization.getLocation() )
            .website( exampleOrganization.getWebsite() )
            .description( exampleOrganization.getDescription() )
            .build();

    AppUserDto exampleUserDto = AppUserDto.builder()
            .email( exampleUser.getEmail() )
            .id( "exampleUserId" )
            .name( exampleUser.getName() )
            .organizations( List.of() )
            .avatarUrl( exampleUser.getAvatarUrl() )
            .build();


    @Test
    @DisplayName("Should return true when user found")
    void getUserDtoById_shouldReturnTrueWhenFound() {
        AppUserDto userDtoWithoutOrgas = AppUserDto.builder()
                .email( exampleUser.getEmail() )
                .id( exampleUser.getId() )
                .name( exampleUser.getName() )
                .avatarUrl( exampleUser.getAvatarUrl() )
                .build();

        exampleOrgaDto = exampleOrgaDto.toBuilder()
                .owners( Set.of( userDtoWithoutOrgas ) )
                .build();

        exampleUserDto = exampleUserDto.toBuilder()
                .organizations( List.of( exampleOrgaDto ) )
                .build();

        List<String> orgIds = exampleUser.getOrganizations().stream().toList();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.findAllById( exampleOrganization.getOwners() ) )
                .thenReturn( List.of( exampleUser ) );
        when( mockOrganizationRepo.findAllById( orgIds ) )
                .thenReturn( List.of( exampleOrganization ) );

        AppUserDto actualUserDto = userService.getUserDtoById( exampleUser.getId() );

        assertEquals( exampleUserDto, actualUserDto );
        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).findAllById( exampleOrganization.getOwners() );
        verify( mockOrganizationRepo ).findAllById( orgIds );
    }


    @Test
    @DisplayName("Should return true when minimal user found")
    void getUserDtoById_shouldReturnTrueWhenMinimalUserFound() {
        exampleUser = exampleUser.toBuilder()
                .userSettings( exampleUserSettings.toBuilder()
                        .showEmail( false )
                        .showAvatar( false )
                        .build() )
                .build();

        AppUserDto userDtoWithoutOrgas = AppUserDto.builder()
                .id( exampleUser.getId() )
                .name( exampleUser.getName() )
                .build();

        exampleOrgaDto = exampleOrgaDto.toBuilder()
                .owners( Set.of( userDtoWithoutOrgas ) )
                .build();

        exampleUserDto = AppUserDto.builder()
                .name( exampleUser.getName() )
                .id( exampleUser.getId() )
                .organizations( List.of( exampleOrgaDto ) )
                .build();

        List<String> orgIds = exampleUser.getOrganizations().stream().toList();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.findAllById( exampleOrganization.getOwners() ) )
                .thenReturn( List.of( exampleUser ) );
        when( mockOrganizationRepo.findAllById( orgIds ) )
                .thenReturn( List.of( exampleOrganization ) );

        AppUserDto actualUserDto = userService.getUserDtoById( exampleUser.getId() );

        assertEquals( exampleUserDto, actualUserDto );
        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).findAllById( exampleOrganization.getOwners() );
        verify( mockOrganizationRepo ).findAllById( orgIds );
    }


    @Test
    @DisplayName("Should throw when user not found")
    void getUserDtoById_shouldThrowWhenUserNotFound() {
        String notExistingUserId = "notExistingUserId";

        when( mockUserRepo.findById( notExistingUserId ) ).thenReturn( Optional.empty() );

        assertThatThrownBy( () -> userService.getUserDtoById( notExistingUserId ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "User not found with id: " + notExistingUserId );

        verify( mockUserRepo ).findById( notExistingUserId );
    }

    @Test
    @DisplayName("Should throw when at least one orga not found")
    void getUserDtoById_shouldThrowWhenOrgaNotFound() {
        exampleUser = exampleUser.toBuilder()
                .organizations( Set.of( "notExistingOrgaId" ) )
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );

        assertThatThrownBy( () -> userService.getUserDtoById( exampleUser.getId() ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "One or more organizations not found for user with id: " + exampleUser.getId() );

        verify( mockUserRepo ).findById( exampleUser.getId() );
    }

    @Test
    @DisplayName("Should throw when orga owner not found")
    void getUserDtoById_shouldThrowWhenOrgaOwnerNotFound() {

        exampleOrganization = exampleOrganization.toBuilder()
                .owners( Set.of( "notExistingUserId" ) )
                .build();

        exampleUser = exampleUser.toBuilder()
                .organizations( Set.of( exampleOrganization.getId() ) )
                .build();


        List<String> orgIds = exampleUser.getOrganizations().stream().toList();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.findAllById( exampleOrganization.getOwners() ) )
                .thenReturn( List.of() );
        when( mockOrganizationRepo.findAllById( orgIds ) )
                .thenReturn( List.of( exampleOrganization ) );

        assertThatThrownBy( () -> userService.getUserDtoById( exampleUser.getId() ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "One or more organization owners not found." );

        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).findAllById( exampleOrganization.getOwners() );
        verify( mockOrganizationRepo ).findAllById( orgIds );
    }

    @Test
    @DisplayName("Should throw when user not visible")
    void getUserDtoById_shouldThrowWhenUserNotVisible() {
        AppUser invisibleExampleUser = exampleUser.toBuilder()
                .userSettings( exampleUserSettings.toBuilder().userVisible( false ).build() )
                .build();

        when( mockUserRepo.findById( invisibleExampleUser.getId() ) ).thenReturn( Optional.of( invisibleExampleUser ) );

        assertThatThrownBy( () -> userService.getUserDtoById( invisibleExampleUser.getId() ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "User not found with id: " + invisibleExampleUser.getId() );

        verify( mockUserRepo ).findById( invisibleExampleUser.getId() );
    }

    @Test
    @DisplayName("Should return true when user found without organizations")
    void getUserDtoById_shouldReturnTrueWhenUserFoundWithoutOrgas() {
        AppUser exampleUserWithoutOrgas = exampleUser.toBuilder()
                .organizations( null )
                .build();

        when( mockUserRepo.findById( exampleUserWithoutOrgas.getId() ) ).thenReturn( Optional.of( exampleUserWithoutOrgas ) );

        AppUserDto actualUserDto = userService.getUserDtoById( exampleUserWithoutOrgas.getId() );

        assertEquals( exampleUserDto, actualUserDto );

        verify( mockUserRepo ).findById( exampleUserWithoutOrgas.getId() );
    }

    @Test
    @DisplayName("Should return true when user found without organizations and hidden user info")
    void getUserDtoById_shouldReturnTrueWhenMinimalUserFoundWithoutOrgas() {
        AppUser exampleUserWithoutOrgas = exampleUser.toBuilder()
                .organizations( null )
                .userSettings( exampleUserSettings.toBuilder().showEmail( false ).showAvatar( false ).build() )
                .build();

        AppUserDto expectedUserDto = AppUserDto.builder()
                .name( exampleUserWithoutOrgas.getName() )
                .id( exampleUserWithoutOrgas.getId() )
                .organizations( List.of() )
                .build();

        when( mockUserRepo.findById( exampleUserWithoutOrgas.getId() ) ).thenReturn( Optional.of( exampleUserWithoutOrgas ) );

        AppUserDto actualUserDto = userService.getUserDtoById( exampleUserWithoutOrgas.getId() );

        assertEquals( expectedUserDto, actualUserDto );

        verify( mockUserRepo ).findById( exampleUserWithoutOrgas.getId() );
    }


    @Test
    @DisplayName("Should return true when set of users found by ids")
    void getAllUserDtosById_shouldReturnTrueWhenFound() {
        exampleUser = exampleUser.toBuilder()
                .organizations( null )
                .build();

        Set<String> userIds = Set.of( exampleUser.getId() );

        Set<AppUserDto> expectedUserDtos = Set.of( exampleUserDto );

        when( mockUserRepo.findAllById( userIds ) ).thenReturn(
                List.of( exampleUser ) );

        Set<AppUserDto> actualUserDtos = userService.getAllUserDtosById( userIds );

        assertEquals( expectedUserDtos, actualUserDtos );
    }

    @Test
    @DisplayName("Should return throw when user not found")
    void getAllUserDtosById_shouldThrowWhenUserNotFound() {
        Set<String> userIds = Set.of( exampleUser.getId() );

        when( mockUserRepo.findAllById( userIds ) ).thenReturn(
                List.of() );

        assertThatThrownBy( () -> userService.getAllUserDtosById( userIds ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "No users found for the provided IDs" );
    }

    @Test
    @DisplayName("Should return true when users found")
    void getAllUsersDtos_shouldReturnTrueWhenFound() {
        exampleUser = exampleUser.toBuilder()
                .organizations( null )
                .build();

        List<AppUserDto> expectedUserDtos = List.of( exampleUserDto );

        when( mockUserRepo.findAll() ).thenReturn( List.of( exampleUser ) );

        List<AppUserDto> actualUserDtos = userService.getAllUsersDtos();

        assertEquals( expectedUserDtos, actualUserDtos );
    }

    @Test
    @DisplayName("Should return true when invisible user not found")
    void getAllUsersDtos_shouldReturnTrueWhenInvisibleUserNotFound() {
        exampleUser = exampleUser.toBuilder()
                .organizations( null )
                .userSettings( exampleUserSettings.toBuilder().showEmail( false ).userVisible( false ).build() )
                .build();


        when( mockUserRepo.findAll() ).thenReturn( List.of( exampleUser ) );

        List<AppUserDto> expectedUserDtos = List.of();
        List<AppUserDto> actualUserDtos = userService.getAllUsersDtos();

        assertEquals( expectedUserDtos, actualUserDtos );
        verify( mockUserRepo ).findAll();
    }

    @Test
    @DisplayName("Should return true when user exists")
    void userExistsById_shouldReturnTrueWhenExists() {
        when( mockUserRepo.existsById( exampleUser.getId() ) ).thenReturn( true );

        boolean exists = userService.userExistsById( exampleUser.getId() );

        assertTrue( exists );
        verify( mockUserRepo ).existsById( exampleUser.getId() );
    }

    @Test
    @DisplayName("Should return true when user does not exists")
    void userExistsById_shouldReturnTrueWhenNotExists() {
        String notExistingId = "notExistingId";
        when( mockUserRepo.existsById( notExistingId ) ).thenReturn( false );

        boolean exists = userService.userExistsById( notExistingId );

        assertFalse( exists );
        verify( mockUserRepo ).existsById( notExistingId );
    }

    @Test
    @DisplayName("Should return true when raw user found")
    void getRawUserById_shouldReturnTrueWhenFound() {
        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );

        AppUser actualUser = userService.getRawUserById( exampleUser.getId() );

        assertEquals( exampleUser, actualUser );
        verify( mockUserRepo ).findById( exampleUser.getId() );
    }

    @Test
    @DisplayName("Should return true when all found")
    void getAllRawUsers_shouldReturnTrueWhenFound() {
        when( mockUserRepo.findAll() ).thenReturn( List.of( exampleUser ) );

        List<AppUser> actualUsers = userService.getAllRawUsers();

        assertEquals( List.of( exampleUser ), actualUsers );
        verify( mockUserRepo ).findAll();
    }

    @Test
    @DisplayName("Should return true when user updated")
    void updateUser_shouldReturnTrueWhenUpdated() {

        UserSettings newSettings = UserSettings.builder()
                .userVisible( true )
                .showEmail( false )
                .showAvatar( false )
                .showOrgas( false )
                .build();

        AppUser expectedUpdatedUser = exampleUser.toBuilder()
                .name( "newExampleUserName" )
                .userSettings( newSettings )
                .email( "newExampleUserEmail" )
                .build();

        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .name( "newExampleUserName" )
                .email( "newExampleUserEmail" )
                .userSettings( newSettings )
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.save( expectedUpdatedUser ) ).thenReturn( expectedUpdatedUser );

        AppUser actualUpdatedUser = userService.updateUser( updateData, exampleUser.getId() );

        assertEquals( expectedUpdatedUser, actualUpdatedUser );

        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).save( expectedUpdatedUser );
    }

    @Test
    @DisplayName("Should return true when user updated minimally")
    void updateUser_shouldReturnTrueWhenUpdatedMinimally() {


        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.save( exampleUser ) ).thenReturn( exampleUser );

        AppUser actualUpdatedUser = userService.updateUser( updateData, exampleUser.getId() );

        assertEquals( exampleUser, actualUpdatedUser );

        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).save( exampleUser );
    }

    @Test
    @DisplayName("Should throw when conflict with user settings and organizations")
    void updateUser_shouldThrowWhenConflictWithUserSettings() {
        AppUserUpdateDto updateData = AppUserUpdateDto.builder()
                .userSettings( UserSettings.builder()
                        .userVisible( false )
                        .build() )
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );

        assertThatThrownBy( () -> userService.updateUser( updateData, exampleUser.getId() ) )
                .isInstanceOf( IllegalStateException.class )
                .hasMessage( "Cannot set user as not visible while being part of organizations. Please remove the user from all organizations first." );

        verify( mockUserRepo ).findById( exampleUser.getId() );
    }

    @Test
    @DisplayName("Should return true when user made admin")
    void makeUserAdmin_shouldReturnTrueWhenMadeAdmin() {

        AppUser expectedUpdatedUser = exampleUser.toBuilder()
                .role( Role.ADMIN )
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.save( expectedUpdatedUser ) ).thenReturn( expectedUpdatedUser );

        AppUser actualUpdatedUser = userService.makeUserAdmin( exampleUser.getId() );

        assertEquals( expectedUpdatedUser, actualUpdatedUser );
    }

    @Test
    @DisplayName("Should return true when user made super admin")
    void makeUserSuperAdmin_shouldReturnTrueWhenMadeSuperAdmin() {

        AppUser expectedUpdatedUser = exampleUser.toBuilder()
                .role( Role.SUPER_ADMIN )
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockUserRepo.save( expectedUpdatedUser ) ).thenReturn( expectedUpdatedUser );

        AppUser actualUpdatedUser = userService.makeUserSuperAdmin( exampleUser.getId() );

        assertEquals( expectedUpdatedUser, actualUpdatedUser );
    }


    @Test
    @DisplayName("Should return true when user without organizations deleted")
    void deleteUserById_shouldReturnTrueWhenDeleted() {
        exampleUser = exampleUser.toBuilder()
                .organizations( null )
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );

        doNothing().when( mockUserRepo ).deleteById( exampleUser.getId() );

        assertDoesNotThrow( () -> userService.deleteUserById( exampleUser.getId() ) );

        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).deleteById( exampleUser.getId() );
    }

    @Test
    @DisplayName("Should return true when user and organizations deleted")
    void deleteUserById_shouldReturnTrueWhenDeletedWithOrganizations() {
        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockOrganizationRepo.findById( "exampleOrgaId" ) ).thenReturn( Optional.of( exampleOrganization ) );
        when( mockOrganizationRepo.save( any( Organization.class ) ) )
                .thenReturn( exampleOrganization.toBuilder()
                        .owners( Set.of() )
                        .build() );

        doNothing().when( mockUserRepo ).deleteById( exampleUser.getId() );

        assertDoesNotThrow( () -> userService.deleteUserById( exampleUser.getId() ) );

        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).deleteById( exampleUser.getId() );
        verify( mockOrganizationRepo ).findById( "exampleOrgaId" );
    }

    @Test
    @DisplayName("Should throw when organization not found during user deletion")
    void deleteUserById_shouldThrowWhenOrgaNotFound() {
        String notExistingOrgaId = "notExistingOrgaId";

        exampleUser = exampleUser.toBuilder()
                .organizations( Set.of( notExistingOrgaId ) )
                .build();

        when( mockUserRepo.findById( exampleUser.getId() ) ).thenReturn( Optional.of( exampleUser ) );
        when( mockOrganizationRepo.findById( notExistingOrgaId ) ).thenReturn( Optional.empty() );

        doNothing().when( mockUserRepo ).deleteById( exampleUser.getId() );

        assertThatThrownBy( () -> userService.deleteUserById( exampleUser.getId() ) )
                .isInstanceOf( ResourceNotFoundException.class )
                .hasMessage( "Organization not found with id: " + notExistingOrgaId );

        verify( mockUserRepo ).findById( exampleUser.getId() );
        verify( mockUserRepo ).deleteById( exampleUser.getId() );
        verify( mockOrganizationRepo ).findById( notExistingOrgaId );
    }
}