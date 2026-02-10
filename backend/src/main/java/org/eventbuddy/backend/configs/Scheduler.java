package org.eventbuddy.backend.configs;

import org.eventbuddy.backend.fake_data.FakeDataService;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.repos.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class Scheduler {

    private static final Logger logger = LoggerFactory.getLogger( Scheduler.class );
    private final FakeDataService fakeDataService;
    private final EventRepository eventRepository;

    public Scheduler( FakeDataService fakeDataService, EventRepository eventRepository ) {
        this.eventRepository = eventRepository;
        this.fakeDataService = fakeDataService;
    }

    // Create fresh data after booting up (render.com shuts down app to sleep after 15 mins)
    @Scheduled(initialDelay = 1L)
    public void cleanAndRefreshData() {

        logger.info( "Scheduler started after booting" );
        fakeDataService.deleteAllFakeData();
        Instant start = Instant.now();
        try {
            fakeDataService.createFakeData( 10 );
            logger.info( "Fake Data created without issues" );
        } catch ( Exception e ) {
            logger.error( "Error while creating fake data: {}", e.getMessage() );
        }
        Instant end = Instant.now();
        logger.info( "Scheduler finished after booting. Time taken: {} seconds", ( end.toEpochMilli() - start.toEpochMilli() ) / 1000.0 );
    }

    // Assuming the service is running continuously, clean up data at midnight every day
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldEvents() {
        List<Event> allEvents = eventRepository.findAll();
        int count = 0;

        Instant now = Instant.now();
        for ( Event event : allEvents ) {
            if ( event.getEventDateTime().isBefore( now ) ) {
                eventRepository.delete( event );
                count++;
                logger.info( "Deleted past event: {}", event.getTitle() );
            }
        }
        logger.info( "Scheduler finished deleting old events. Total deleted: {}", count );
    }
}
