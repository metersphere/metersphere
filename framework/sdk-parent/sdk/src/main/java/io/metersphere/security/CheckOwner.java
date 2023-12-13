package io.metersphere.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckOwner {
    String resourceId();

    String resourceType();
}
