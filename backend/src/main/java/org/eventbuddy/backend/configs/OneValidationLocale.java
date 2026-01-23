package org.eventbuddy.backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

@Configuration
public class OneValidationLocale {
    @Bean
    FixedLocaleResolver localeResolver() {
        return new FixedLocaleResolver( Locale.ENGLISH );
    }
}
