package org.eventbuddy.backend.enums;

import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

@Getter
public enum ImageType {
    JPEG( "image/jpeg" ),
    PNG( "image/png" ),
    HEIC( "image/heic" ),
    WEBP( "image/webp" ),
    SVG( "image/svg+xml" );

    private final String contentType;

    ImageType( String contentType ) {
        this.contentType = contentType;
    }

    public static boolean isSupported( String contentType ) {
        return Stream.of( values() )
                .anyMatch( type -> type.getContentType().equals( contentType ) );
    }

    public static List<String> getAllFileTypes() {
        return Stream.of( values() )
                .map( ImageType::getFileType )
                .toList();
    }

    public String getFileType() {
        return this.contentType.split( "/" )[1].toLowerCase();
    }
}
