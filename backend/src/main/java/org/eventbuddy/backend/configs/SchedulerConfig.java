package org.eventbuddy.backend.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("!integrationTest") // Enable scheduling for all profiles except integration tests
public class SchedulerConfig {
}
