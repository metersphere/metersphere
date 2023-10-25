package io.metersphere.system.notice.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SendNotice {

    String taskType();

    /**
     * Event
     */
    String event() default "";

    /**
     * 消息主题
     */
    String subject() default "";

    /**
     * 获取实际值
     */
    String target() default "";

    /**
     * 资源目标
     */
    Class<?> targetClass() default Object.class;

    /**
     * 消息内容
     */
    String context() default "";

}
