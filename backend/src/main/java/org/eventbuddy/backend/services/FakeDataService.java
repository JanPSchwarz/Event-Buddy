package org.eventbuddy.backend.services;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.organization.Organization;
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
    private final Faker faker = new Faker();

    private final List<String> currentUserIds = new ArrayList<>();

    public void createFakeData( int numberOfUsers ) {
        createFakeUser( numberOfUsers );
        createFakeOrganizations( 5 );
    }

    public void deleteAllFakeData() {
        deleteFakeUsers();
    }

    private void createFakeUser( int numberOfUsers ) {
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

            currentUserIds.add( createdUser.getId() );
        }
    }

    private void createFakeOrganizations( int numberOfOrgas ) {
        for ( int i = 0; i < numberOfOrgas; i++ ) {
            Organization newOrganization = Organization.builder()
                    .name( faker.company().name() )
                    .description( faker.lorem().sentence( 10 ) )
                    .website( faker.internet().url() )
                    .owners( Set.of( currentUserIds.get( faker.number().numberBetween( 0, currentUserIds.size() ) ) ) )
                    .build();

            organizationRepo.save( newOrganization );
        }

    }

    private void deleteFakeUsers() {
        userRepo.deleteAll();
        organizationRepo.deleteAll();
    }
}
