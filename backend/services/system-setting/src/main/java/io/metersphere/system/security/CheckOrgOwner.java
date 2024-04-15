package io.metersphere.system.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckOrgOwner {

    String resourceId();

    String resourceType();

    String resourceCol() default "organization_id";
}
