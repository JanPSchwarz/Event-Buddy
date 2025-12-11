package org.eventbuddy.backend.services;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("dev")
public class FakeDataService {

    private final UserRepository userRepo;
    private final Faker faker = new Faker();

    public void createFakeUsers( int numberOfUsers ) {
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

            userRepo.save( newUser );
        }
    }

    public void deleteAllFakeUsers() {
        userRepo.deleteAll();
    }
}
