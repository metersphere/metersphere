package io.metersphere.system.log.annotation;

import io.metersphere.system.log.constants.OperationLogType;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * 操作日志注解类
 * @author mr.zhao
 */
public @interface Log {

    /**
     * 操作类型
     *
     * @return
     */
    OperationLogType type() default OperationLogType.SELECT;

    /***
     * 操作函数
     */
    String expression();

    /**
     * 传入执行类
     *
     * @return
     */
    Class[] msClass() default {};
}
