package org.eventbuddy.backend.repos;

import org.eventbuddy.backend.models.appUser.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<AppUser, String> {

    Optional<AppUser> findByProviderId( String providerId );
}
