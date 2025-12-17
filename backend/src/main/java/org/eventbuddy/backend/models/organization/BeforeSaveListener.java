package org.eventbuddy.backend.models.organization;

import com.mongodb.lang.NonNull;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Component
class BeforeSaveListener implements BeforeSaveCallback<Organization> {

    @Override
    @NonNull
    public Organization onBeforeSave( @NonNull Organization entity, @NonNull Document document, @NonNull String collection ) {
        // Generate slug from name if name is present and has been modified
        if ( entity.getName() != null && document.containsKey( "name" ) ) {
            String slug = createSlugFromName( entity.getName() );

            document.put( "slug", slug );

            return entity.toBuilder()
                    .slug( slug )
                    .build();
        }

        return entity;
    }

    private String createSlugFromName( String name ) {
        return name.toLowerCase()
                .trim()
                .replaceAll( "ä", "ae" )
                .replaceAll( "ö", "oe" )
                .replaceAll( "ü", "ue" )
                .replaceAll( "ß", "ss" )
                .replaceAll( "\\s+", "-" )
                .replaceAll( "[^a-z0-9-]", "" )
                .replaceAll( "-+", "-" )
                .replaceAll( "^-|-$", "" );
    }

}
