package org.eventbuddy.backend.exceptions;

import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.utils.IdService;
import org.springframework.dao.DuplicateKeyException;
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

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorMessage> handleResourceNotFoundException( ResourceNotFoundException ex ) {
        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body(
                createErrorMessage( ex.getMessage(), HttpStatus.NOT_FOUND.value() )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadableException( HttpMessageNotReadableException ex ) {
        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body(
                createErrorMessage( "Request Body must not be empty", HttpStatus.BAD_REQUEST.value() )
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleValidationException( MethodArgumentNotValidException ex ) {
        StringBuilder messageBuilder = new StringBuilder();

        ex.getBindingResult().getFieldErrors().stream()
                .sorted( Comparator.comparing( FieldError::getField ) ).forEach( error -> messageBuilder.append( error.getDefaultMessage() ).append( ". " ) );

        ErrorMessage errorMessage = createErrorMessage( messageBuilder.toString().trim(), HttpStatus.BAD_REQUEST.value() );

        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( errorMessage );
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorMessage> handleUnauthorizedException( UnauthorizedException ex ) {
        return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).body(
                createErrorMessage( ex.getMessage(), HttpStatus.UNAUTHORIZED.value() )
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorMessage> handleDuplicateKeyException( DuplicateKeyException ex ) {

        String message = "A resource with this value already exists";
        String errorMsg = ex.getMessage();

        // Try to extract field name from MongoDB error message
        // Message format: "... dup key: { fieldName: \"value\" }"
        if ( errorMsg != null && errorMsg.contains( "dup key:" ) ) {
            try {
                int dupKeyIndex = errorMsg.indexOf( "dup key:" );
                int startBrace = errorMsg.indexOf( "{", dupKeyIndex );
                int endBrace = errorMsg.indexOf( "}", startBrace );

                if ( startBrace != -1 && endBrace != -1 ) {
                    String keyPart = errorMsg.substring( startBrace + 1, endBrace ).trim();
                    String fieldName = keyPart.split( ":" )[0].trim();
                    message = String.format( "A resource with this %s already exists", fieldName );
                }
            } catch ( Exception e ) {
                // If parsing fails, use generic message
            }
        }

        return ResponseEntity.status( HttpStatus.CONFLICT ).body(
                createErrorMessage( message, HttpStatus.CONFLICT.value() )
        );
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handleRuntimeException( RuntimeException ex ) {
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body(
                createErrorMessage( "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value() )
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handleException( Exception ex ) {
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body(
                createErrorMessage( "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value() )
        );
    }
}


