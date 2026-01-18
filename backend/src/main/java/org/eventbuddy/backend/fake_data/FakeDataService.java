package org.eventbuddy.backend.fake_data;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.eventbuddy.backend.enums.Role;
import org.eventbuddy.backend.exceptions.ResourceNotFoundException;
import org.eventbuddy.backend.models.app_user.AppUser;
import org.eventbuddy.backend.models.app_user.UserSettings;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.models.image.Image;
import org.eventbuddy.backend.models.organization.Contact;
import org.eventbuddy.backend.models.organization.Location;
import org.eventbuddy.backend.models.organization.Organization;
import org.eventbuddy.backend.repos.EventRepository;
import org.eventbuddy.backend.repos.ImageRepository;
import org.eventbuddy.backend.repos.OrganizationRepository;
import org.eventbuddy.backend.repos.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Profile("dev")
public class FakeDataService {

    private final UserRepository userRepo;
    private final OrganizationRepository organizationRepo;
    private final ImageRepository imageRepo;
    private final EventRepository eventRepo;
    private final Faker faker = new Faker();

    private final List<String> currentUserIds = new ArrayList<>();
    private final List<String> currentOrgaIds = new ArrayList<>();

    public void createFakeData( int numberOfUsers ) {
        List<AppUser> createdFakeUsers = createFakeUser( numberOfUsers );

        createFakeOrganizations( 10 );

        createFakeEvents( 30 );

        List<AppUser> updatedFakeUsers = new ArrayList<>();
        for ( AppUser user : createdFakeUsers ) {

            int orgaCount = faker.number().numberBetween( 0, 3 );

            Set<String> orgaIds = currentOrgaIds.stream()
                    .skip( faker.number().numberBetween( 0, currentOrgaIds.size() - orgaCount ) )
                    .limit( orgaCount )
                    .collect( java.util.stream.Collectors.toSet() );

            AppUser updatedUser = !orgaIds.isEmpty() ? user.toBuilder()
                    .organizations( orgaIds )
                    .userSettings( user.getUserSettings().toBuilder().userVisible( true ).build() )
                    .build() : user;

            updatedFakeUsers.add( updatedUser );
        }

        userRepo.saveAll( updatedFakeUsers );
    }

    public void deleteAllFakeData() {
        userRepo.deleteAll();
        organizationRepo.deleteAll();
        imageRepo.deleteAll();
        eventRepo.deleteAll();
    }

    private void createFakeEvents( int numberOfEvents ) {

        for ( int i = 0; i < numberOfEvents; i++ ) {

            Organization randomOrga = organizationRepo.findById(
                    currentOrgaIds.get( faker.number().numberBetween( 0, currentOrgaIds.size() ) )
            ).orElseThrow( () ->
                    new ResourceNotFoundException( "orga not found while creating fake events" )
            );

            Location fakeLocation = provideFakeLocation();

            Image randomImage = Image.builder()
                    .imageData( provideFakeImageBase64Binary() )
                    .contentType( "image/jpeg" )
                    .build();

            Image savedImage = imageRepo.save( randomImage );

            int maxCapacity = faker.number().numberBetween( 0, 500 );
            int freeTicketCapacity = faker.number().numberBetween( 0, maxCapacity );

            boolean hasValidCapacity = maxCapacity > 0 && freeTicketCapacity > 0;

            boolean isUnder20PercentLeft = hasValidCapacity && ( ( double ) freeTicketCapacity / maxCapacity ) < 0.2;

            boolean isSoldOut = hasValidCapacity && ( freeTicketCapacity == 0 );

            Event newEvent = Event.builder()
                    .eventOrganization( randomOrga )
                    .title( faker.funnyName().name() )
                    .description( faker.lorem().sentence( 100 ) )
                    .eventDateTime( faker.timeAndDate().future() )
                    .price( faker.bool().bool() ? faker.number().randomDouble( 2, 5, 100 ) : 0.0 )
                    .maxTicketCapacity( maxCapacity > 0 ? maxCapacity : null )
                    .freeTicketCapacity( freeTicketCapacity > 0 ? freeTicketCapacity : null )
                    .maxPerBooking( faker.number().numberBetween( 0, 8 ) )
                    .ticketAlarm( isUnder20PercentLeft )
                    .isSoldOut( isSoldOut )
                    .imageId( savedImage.getImageId() )
                    .location( fakeLocation )
                    .build();

            eventRepo.save( newEvent );
        }

    }


    private List<AppUser> createFakeUser( int numberOfUsers ) {
        List<AppUser> appUsersList = new ArrayList<>();
        for ( int i = 0; i < numberOfUsers; i++ ) {

            UserSettings fakeSettings = UserSettings.builder()
                    .userVisible( true )
                    .showAvatar( true )
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

            Image randomImage = Image.builder()
                    .imageData( provideFakeImageBase64Binary() )
                    .contentType( "image/jpeg" )
                    .build();

            Image savedImage = imageRepo.save( randomImage );

            Location fakeLocation = provideFakeLocation();

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
                    .imageId( savedImage.getImageId() )
                    .build();

            Organization createdOrganization = organizationRepo.save( newOrganization );

            currentOrgaIds.add( createdOrganization.getId() );
        }
    }

    private Binary provideFakeImageBase64Binary() {
        String base64Image = faker.image().base64JPG();
        String cleanBase64 = base64Image.replaceFirst( "^data:image/[^;]+;base64,", "" );
        byte[] imageBytes = Base64.getDecoder().decode( cleanBase64 );

        return new Binary( BsonBinarySubType.BINARY, imageBytes );
    }

    private Location provideFakeLocation() {
        return Location.builder()
                .locationName( faker.bool().bool() ? faker.name().fullName() + " Hall" : null )
                .address( faker.address().streetAddress() )
                .city( faker.address().city() )
                .zipCode( faker.number().digits( 6 ) )
                .country( faker.address().country() )
                .build();
    }
}
