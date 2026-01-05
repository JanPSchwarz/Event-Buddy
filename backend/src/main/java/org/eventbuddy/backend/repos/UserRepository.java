package org.eventbuddy.backend.repos;

import org.eventbuddy.backend.models.app_user.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends MongoRepository<AppUser, String> {

    Optional<AppUser> findByProviderId( String providerId );

    Optional<List<AppUser>> findAllById( Set<String> stringIds );
}
