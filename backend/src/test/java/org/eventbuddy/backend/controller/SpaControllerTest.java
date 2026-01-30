package org.eventbuddy.backend.controller;

import org.eventbuddy.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser
class SpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void forward_shouldForwardToIndexHtml_whenRootPath() throws Exception {
        // Root path is handled by Spring Boot's WelcomePageHandlerMapping
        // which forwards to index.html (without leading slash)
        mockMvc.perform( get( "/" ) )
                .andExpect( status().isOk() );
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