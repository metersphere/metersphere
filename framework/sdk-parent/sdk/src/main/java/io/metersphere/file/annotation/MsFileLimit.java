package io.metersphere.file.annotation;

import java.lang.annotation.*;

/**
 * @author song-cc-rock
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MsFileLimit {

	/**
	 * 文件大小限制 (单位: MB)
	 */
	long maxSize() default 0;
}
