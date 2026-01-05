package org.eventbuddy.backend.mockUser;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithCustomMockUser {

    String id() default "123";

    String registrationId() default "github";

    String role() default "USER";
}
