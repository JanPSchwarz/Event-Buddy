package org.eventbuddy.backend.scripts;

import net.datafaker.Faker;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class FakeDataInitializer implements CommandLineRunner {

    private static final int NUMBER_OF_FAKE_USES = 10;
    private final UserRepository userRepository;
    private final Faker faker = new Faker();

    public FakeDataInitializer( UserRepository userRepository ) {
        this.userRepository = userRepository;
    }

    @Override
    public void run( String... args ) throws Exception {

        if ( userRepository.count() < NUMBER_OF_FAKE_USES ) {
            System.out.println( "Creating fake users..." );
            for ( int i = 0; i < NUMBER_OF_FAKE_USES; i++ ) {

                UserSettings fakeSettings = UserSettings.builder()
                        .userVisible( true )
                        .showAvatar( true )
                        .showOrgas( true )
                        .build();

                AppUser newUser = new AppUser().toBuilder()
                        .name( faker.name().name() )
                        .email( faker.internet().emailAddress() )
                        .providerId( "github_" + faker.number().digits( 9 ) )
                        .userSettings( fakeSettings )
                        .avatarUrl( faker.avatar().image() )
                        .role( Role.USER )
                        .build();

                userRepository.save( newUser );
            }

            System.out.println( "Fake users created." );
        }

    }


}
