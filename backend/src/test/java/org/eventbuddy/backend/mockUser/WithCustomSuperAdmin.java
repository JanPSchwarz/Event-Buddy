package org.eventbuddy.backend.mockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithCustomMockUser(role = "SUPER_ADMIN")
public @interface WithCustomSuperAdmin {
}
