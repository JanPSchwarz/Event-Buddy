package org.eventbuddy.backend.exceptions;

import org.eventbuddy.backend.models.error.ErrorMessage;
import org.eventbuddy.backend.utils.IdService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${upload.max-file-size}")
    String maxFileSize;

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

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException( IllegalArgumentException ex ) {
        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body(
                createErrorMessage( ex.getMessage(), HttpStatus.BAD_REQUEST.value() )
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorMessage> handleUnauthorizedException( UnauthorizedException ex ) {
        return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).body(
                createErrorMessage( ex.getMessage(), HttpStatus.UNAUTHORIZED.value() )
        );
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handleIOException( IOException ex ) {
        return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).body(
                createErrorMessage( "An unexpected error occurred while processing the file", HttpStatus.INTERNAL_SERVER_ERROR.value() )
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<ErrorMessage> handleMaxUploadSizeExceededException( MaxUploadSizeExceededException ex ) {
        return ResponseEntity.status( HttpStatus.PAYLOAD_TOO_LARGE ).body(
                createErrorMessage( "Uploaded file exceeds the maximum allowed size: " + maxFileSize, HttpStatus.PAYLOAD_TOO_LARGE.value() )
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

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorMessage> handleIllegalStateException( IllegalStateException ex ) {
        return ResponseEntity.status( HttpStatus.CONFLICT ).body(
                createErrorMessage( ex.getMessage(), HttpStatus.CONFLICT.value() )
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorMessage> handleAccessDeniedException( AuthorizationDeniedException ex ) {
        return ResponseEntity.status( HttpStatus.UNAUTHORIZED ).body(
                createErrorMessage( "You are not logged in or not allowed to perform this Action.", HttpStatus.FORBIDDEN.value() )
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


