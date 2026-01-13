package org.eventbuddy.backend.repos;

import org.eventbuddy.backend.models.booking.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingRepository extends MongoRepository<Booking, String> {
}
