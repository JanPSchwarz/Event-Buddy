package org.eventbuddy.backend.configs;

import org.eventbuddy.backend.fake_data.FakeDataService;
import org.eventbuddy.backend.models.event.Event;
import org.eventbuddy.backend.repos.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    @Mock
    private FakeDataService fakeDataService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private Scheduler scheduler;

    @Test
    void cleanAndRefreshData_shouldDeleteAndCreateFakeData() {
        // When
        scheduler.cleanAndRefreshData();

        // Then
        verify( fakeDataService ).deleteAllFakeData();
        verify( fakeDataService ).createFakeData( 10 );
    }

    @Test
    void cleanAndRefreshData_shouldHandleExceptionGracefully() {
        // Given
        doThrow( new RuntimeException( "Test error" ) ).when( fakeDataService ).createFakeData( anyInt() );

        // When
        scheduler.cleanAndRefreshData();

        // Then
        verify( fakeDataService ).deleteAllFakeData();
        verify( fakeDataService ).createFakeData( 10 );
    }

    @Test
    void deleteOldEvents_shouldDeletePastEvents() {
        // Given
        Event pastEvent = mock( Event.class );
        Event futureEvent = mock( Event.class );

        when( pastEvent.getEventDateTime() ).thenReturn( Instant.now().minusSeconds( 3600 ) );
        when( pastEvent.getTitle() ).thenReturn( "Past Event" );
        when( futureEvent.getEventDateTime() ).thenReturn( Instant.now().plusSeconds( 3600 ) );

        when( eventRepository.findAll() ).thenReturn( Arrays.asList( pastEvent, futureEvent ) );

        // When
        scheduler.deleteOldEvents();

        // Then
        verify( eventRepository ).delete( pastEvent );
        verify( eventRepository, never() ).delete( futureEvent );
    }

    @Test
    void deleteOldEvents_shouldNotDeleteAnythingWhenNoOldEvents() {
        // Given
        when( eventRepository.findAll() ).thenReturn( Collections.emptyList() );

        // When
        scheduler.deleteOldEvents();

        // Then
        verify( eventRepository, never() ).delete( any() );
    }
}
