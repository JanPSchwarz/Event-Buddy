package org.eventbuddy.backend.fakeData;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.organization.Contact;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.repos.ImageRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Profile("dev")
public class FakeDataService {

    private final UserRepository userRepo;
    private final OrganizationRepository organizationRepo;
    private final ImageRepository imageRepo;
    private final Faker faker = new Faker();

    private final List<String> currentUserIds = new ArrayList<>();
    private final List<String> currentOrgaIds = new ArrayList<>();

    public void createFakeData( int numberOfUsers ) {
        List<AppUser> createdFakeUsers = createFakeUser( numberOfUsers );

        createFakeOrganizations( 5 );

        List<AppUser> updatedFakeUsers = new ArrayList<>();
        for ( AppUser user : createdFakeUsers ) {

            int orgaCount = faker.number().numberBetween( 0, 3 );

            Set<String> orgaIds = currentOrgaIds.stream()
                    .skip( faker.number().numberBetween( 0, currentOrgaIds.size() - orgaCount ) )
                    .limit( orgaCount )
                    .collect( java.util.stream.Collectors.toSet() );

            AppUser updatedUser = !orgaIds.isEmpty() ? user.toBuilder()
                    .organizations( orgaIds )
                    .build() : user;

            updatedFakeUsers.add( updatedUser );
        }

        userRepo.saveAll( updatedFakeUsers );
    }

    public void deleteAllFakeData() {
        deleteFakeUsers();
    }

    private List<AppUser> createFakeUser( int numberOfUsers ) {
        List<AppUser> appUsersList = new ArrayList<>();
        for ( int i = 0; i < numberOfUsers; i++ ) {

            UserSettings fakeSettings = UserSettings.builder()
                    .userVisible( faker.bool().bool() )
                    .showAvatar( faker.bool().bool() )
                    .showOrgas( faker.bool().bool() )
                    .showEmail( faker.bool().bool() )
                    .build();

            AppUser newUser = new AppUser().toBuilder()
                    .name( faker.name().name() )
                    .email( faker.internet().emailAddress() )
                    .providerId( "github_" + faker.number().digits( 9 ) )
                    .userSettings( fakeSettings )
                    .avatarUrl( faker.avatar().image() )
                    .role( Role.USER )
                    .build();

            AppUser createdUser = userRepo.save( newUser );

            appUsersList.add( createdUser );
            currentUserIds.add( createdUser.getId() );
        }

        return appUsersList;
    }

    private void createFakeOrganizations( int numberOfOrgas ) {
        for ( int i = 0; i < numberOfOrgas; i++ ) {

            Location fakeLocation = Location.builder()
                    .address( faker.address().streetAddress() )
                    .city( faker.address().city() )
                    .zipCode( faker.number().digits( 6 ) )
                    .country( faker.address().country() )
                    .latitude( Double.parseDouble( faker.address().latitude() ) )
                    .longitude( Double.parseDouble( faker.address().longitude() ) )
                    .build();

            Contact fakeContact = Contact.builder()
                    .email( faker.internet().emailAddress() )
                    .phoneNumber( faker.phoneNumber().cellPhoneInternational() )
                    .build();

            Organization newOrganization = Organization.builder()
                    .name( faker.company().name() )
                    .description( faker.lorem().sentence( 10 ) )
                    .website( faker.internet().url() )
                    .owners( Set.of( currentUserIds.get( faker.number().numberBetween( 0, currentUserIds.size() ) ) ) )
                    .contact( fakeContact )
                    .location( fakeLocation )
                    .build();

            Organization createdOrganization = organizationRepo.save( newOrganization );

            currentOrgaIds.add( createdOrganization.getId() );
        }

    }

    private void deleteFakeUsers() {
        userRepo.deleteAll();
        organizationRepo.deleteAll();
        imageRepo.deleteAll();
    }
}
