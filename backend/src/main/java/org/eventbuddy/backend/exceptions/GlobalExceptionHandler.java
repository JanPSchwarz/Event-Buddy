package org.eventbuddy.backend.exceptions;

import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.utils.IdService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Comparator;

@RestControllerAdvice
public class GlobalExceptionHandler {

    IdService idService = new IdService();

    private ErrorMessage createErrorMessage( String message, int status ) {
        return new ErrorMessage( Instant.now().toString(), message, idService.generateErrorId(), status );
    }

    /**
     * Handles ResourceNotFoundException and returns a 404 NOT_FOUND status.
     * Triggered when a requested resource cannot be found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorMessage> handleResourceNotFoundException( ResourceNotFoundException ex ) {
        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body(
                createErrorMessage( ex.getMessage(), HttpStatus.NOT_FOUND.value() )
        );
    }

    /**
     * Handles HttpMessageNotReadableException and returns a 400 BAD_REQUEST status.
     * Triggered when the request body is empty or cannot be parsed.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadableException( HttpMessageNotReadableException ex ) {
        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body(
                createErrorMessage( "Request Body must not be empty", HttpStatus.BAD_REQUEST.value() )
        );
    }

    /**
     * Handles MethodArgumentNotValidException and returns a 400 BAD_REQUEST status.
     * Triggered when validation of request parameters fails.
     * The error message contains all validation errors sorted by field name.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleValidationException( MethodArgumentNotValidException ex ) {
        StringBuilder messageBuilder = new StringBuilder();

        ex.getBindingResult().getFieldErrors().stream()
                .sorted( Comparator.comparing( FieldError::getField ) ).forEach( error -> messageBuilder.append( error.getField() ).append( ": " ).append( error.getDefaultMessage() ).append( "; " ) );

        ErrorMessage errorMessage = createErrorMessage( messageBuilder.toString(), HttpStatus.BAD_REQUEST.value() );

        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( errorMessage );
    }

    /**
     * Handles UnauthorizedException and returns a 401 UNAUTHORIZED status.
     * Triggered when a user is not authorized to perform an action.
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorMessage> handleUnauthorizedException( UnauthorizedException ex ) {
        return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).body(
                createErrorMessage( ex.getMessage(), HttpStatus.UNAUTHORIZED.value() )
        );
    }

    /**
     * Handles RuntimeException and returns a 500 INTERNAL_SERVER_ERROR status.
     * FALLBACK: Catches unexpected runtime errors and returns a generic error message.
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handleRuntimeException( RuntimeException ex ) {
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body(
                createErrorMessage( "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value() )
        );
    }

    /**
     * Handles all uncaught exceptions and returns a 500 INTERNAL_SERVER_ERROR status.
     * FALLBACK: Catches unexpected errors and returns a generic error message.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handleException( Exception ex ) {
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body(
                createErrorMessage( "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value() )
        );
    }
}


