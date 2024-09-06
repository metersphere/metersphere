package io.metersphere.system.interceptor;

import java.lang.annotation.*;

/**
 * 标记需要处理 BaseCondition 查询条件
 * 由 BaseConditionFilterAspect 处理切面逻辑
 * {@link BaseConditionFilterAspect}
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BaseConditionFilter {

}
