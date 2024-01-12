package io.metersphere.functional.excel.annotation;

import java.lang.annotation.*;

/**
 * @author wx
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface NotRequired {
}
