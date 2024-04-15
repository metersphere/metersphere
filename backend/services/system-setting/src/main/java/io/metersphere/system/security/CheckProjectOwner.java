package io.metersphere.system.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckProjectOwner {

    String resourceId();

    String resourceType();

    String resourceCol() default "project_id";
}
