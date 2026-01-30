package org.eventbuddy.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpaController {

    // Regex explanation:
    // Match everything that does NOT start with /api (so we don't break backend endpoints)
    // AND does not look like a static file (has a dot extension)
    // Only GET requests are allowed - SPA routing only happens via GET (browser navigation/reloads)
    @GetMapping(value = {
            "/{path:[^\\.]*}",       // Matches /dashboard
            "/**/{path:[^\\.]*}"     // Matches /users/123/edit
    })
    public String forward( @PathVariable(required = false) String path ) {
        // Forward to the static index.html
        // Spring Boot usually maps "static/index.html" to root "/" internally
        // Path variable is bound but not used - required for SonarCloud security compliance
        return "forward:/index.html";
    }
}
