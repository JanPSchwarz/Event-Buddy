package org.eventbuddy.backend.repos;

import org.eventbuddy.backend.models.booking.Booking;
import org.eventbuddy.backend.models.event.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingRepository extends MongoRepository<Booking, String> {
    void deleteAllByEvent( Event event );
}
