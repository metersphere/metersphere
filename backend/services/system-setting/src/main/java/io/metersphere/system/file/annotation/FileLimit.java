package io.metersphere.system.file.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileLimit {

	/**
	 * 文件大小限制 (单位: MB)
	 */
	long maxSize() default 0;
}
