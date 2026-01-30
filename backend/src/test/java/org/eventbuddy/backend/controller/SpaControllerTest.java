package org.eventbuddy.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpaController.class)
@WithMockUser
class SpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void forward_shouldForwardToIndexHtml_whenRootPath() throws Exception {
        // Root path is handled by Spring Boot's WelcomePageHandlerMapping
        // which forwards to index.html (without leading slash)
        mockMvc.perform( get( "/" ) )
                .andExpect( status().isOk() )
                .andExpect( forwardedUrl( "index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenSingleLevelPath() throws Exception {
        mockMvc.perform( get( "/events" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenNestedPath() throws Exception {
        mockMvc.perform( get( "/event/123" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenDeeplyNestedPath() throws Exception {
        mockMvc.perform( get( "/event/123/orga/456" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenPathWithHyphens() throws Exception {
        mockMvc.perform( get( "/my-orga/123" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }

    @Test
    void forward_shouldForwardToIndexHtml_whenPathWithUnderscores() throws Exception {
        mockMvc.perform( get( "/my_orga/123" ) )
                .andExpect( status().isOk() )
                .andExpect( view().name( "forward:/index.html" ) );
    }
    
}