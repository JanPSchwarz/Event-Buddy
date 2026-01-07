package org.eventbuddy.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

// Converter to prevent Jackson from interfering with multipart/form-data requests
// https://stackoverflow.com/questions/16230291/requestpart-with-mixed-multipart-request-spring-mvc-3-2

@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    /**
     * Converter for support http request with header Content-Type: multipart/form-data
     */
    public MultipartJackson2HttpMessageConverter( ObjectMapper objectMapper ) {
        super( objectMapper, MediaType.APPLICATION_OCTET_STREAM );
    }

    @Override
    public boolean canWrite( Class<?> clazz, MediaType mediaType ) {
        return false;
    }

    @Override
    public boolean canWrite( Type type, Class<?> clazz, MediaType mediaType ) {
        return false;
    }

    @Override
    protected boolean canWrite( MediaType mediaType ) {
        return false;
    }
}
