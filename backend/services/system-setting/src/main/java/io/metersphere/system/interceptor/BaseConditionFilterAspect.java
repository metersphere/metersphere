package io.metersphere.system.interceptor;
import io.metersphere.sdk.dto.BaseCondition;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.constants.InternalUserView;
import io.metersphere.system.dto.UserViewDTO;
import io.metersphere.system.dto.sdk.*;
import io.metersphere.system.utils.SessionUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  17:31
 * 拦截高级搜索等查询
 * 处理高级搜索等通用查询条件
 * 1. 处理视图查询条件
 * 2. 预先过滤不合法的查询条件
 * 3. 拆分系统字段和自定义字段
 * 4. 处理成员选项中的 CURRENT_USER
 */
@Aspect
@Component
public class BaseConditionFilterAspect {
    @Pointcut("@annotation(io.metersphere.system.interceptor.BaseConditionFilter)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof BaseCondition baseCondition) {
                parseBaseCondition(baseCondition);
            } else {
                try {
                    // 批量操作
                    Method getCondition = arg.getClass().getMethod("getCondition");
                    BaseCondition baseCondition = (BaseCondition) getCondition.invoke(arg);
                    parseBaseCondition(baseCondition);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    private void parseBaseCondition(BaseCondition baseCondition) {
        CombineSearch combineSearch = baseCondition.getCombineSearch();
        if (combineSearch == null) {
            return;
        }
        List<CombineCondition> validConditions = getValidConditions(combineSearch.getConditions());
        replaceCurrentUser(validConditions);
        List<CombineCondition> systemFieldConditions = validConditions.stream().filter(item -> !BooleanUtils.isTrue(item.getCustomField())).toList();
        List<CombineCondition> customFieldConditions = validConditions.stream().filter(item -> BooleanUtils.isTrue(item.getCustomField())).toList();

        // 拆分系统字段和自定义字段
        DBCombineSearch dbCombineSearch = BeanUtils.copyBean(new DBCombineSearch(), combineSearch);
        dbCombineSearch.setSystemFieldConditions(systemFieldConditions);
        dbCombineSearch.setCustomFiledConditions(customFieldConditions);
        baseCondition.setCombineSearch(dbCombineSearch);

        // 处理视图查询条件
        String viewId = baseCondition.getViewId();
        dbCombineSearch.setUserViewConditions(List.of());
        for (InternalUserView internalUserView : InternalUserView.values()) {
            UserViewDTO userView = internalUserView.getUserView();
            if (StringUtils.equals(userView.getId(), viewId)) {
                replaceCurrentUser(userView.getConditions());
                dbCombineSearch.setUserViewConditions(userView.getConditions());
            }
        }
    }

    /**
     * 处理成员选项中的 CURRENT_USER
     * 替换当前用户的用户ID
     * @param validConditions
     */
    private void replaceCurrentUser(List<CombineCondition> validConditions) {
        for (CombineCondition validCondition : validConditions) {
            Object value = validCondition.getValue();
            if (value instanceof List arrayValues) {
                for (int i = 0; i < arrayValues.size(); i++) {
                    Object arrayValue = arrayValues.get(i);
                    if (arrayValue != null && StringUtils.equals(arrayValue.toString(), InternalUserView.CURRENT_USER)) {
                        // 替换当前用户的用户ID
                        arrayValues.set(i, SessionUtils.getUserId());
                    }
                }
            }
        }
    }

    public List<CombineCondition> getValidConditions(List<CombineCondition> conditions) {
        if (CollectionUtils.isEmpty(conditions)) {
            return List.of();
        }
        return conditions.stream().filter(CombineCondition::valid).toList();
    }
}
