package org.eventbuddy.backend.mockUser;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(value = "admin", roles = { "SUPER_ADMIN" })
public @interface WithCustomMockAdmin {
}
