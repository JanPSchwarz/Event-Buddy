package org.eventbuddy.backend.repos;

import org.eventbuddy.backend.models.organization.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

    Optional<Organization> findByName( String name );

    Optional<Organization> findBySlug( String organizationSlug );
}
