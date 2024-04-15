package io.metersphere.system.security;


import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.mapper.ExtCheckOwnerMapper;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;


@Aspect
@Component
public class CheckOrgOwnerAspect {

    private ExpressionParser parser = new SpelExpressionParser();
    private StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    @Resource
    private ExtCheckOwnerMapper extCheckOwnerMapper;

    @Pointcut("@annotation(io.metersphere.system.security.CheckOrgOwner)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();
        //获取参数对象数组
        Object[] args = joinPoint.getArgs();
        CheckOrgOwner checkOwner = method.getAnnotation(CheckOrgOwner.class);
        long count = SessionUtils.getUser().getUserRoles()
                .stream()
                .filter(g -> StringUtils.equalsIgnoreCase(g.getId(), InternalUserRole.ADMIN.getValue()))
                .count();
        if (count > 0) {
            return;
        }
        // 操作内容
        //获取方法参数名
        String[] params = discoverer.getParameterNames(method);
        //将参数纳入Spring管理
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], args[len]);
        }

        String resourceId = checkOwner.resourceId();
        String resourceType = checkOwner.resourceType();
        String resourceCol = checkOwner.resourceCol();
        Expression titleExp = parser.parseExpression(resourceId);
        Object v = titleExp.getValue(context, Object.class);
        handleOrganizationResource(v, resourceType, resourceCol);
    }

    private void handleOrganizationResource(Object v, String resourceType, String resourceCol) {
        if (v instanceof String id) {
            if (!extCheckOwnerMapper.checkoutOrganizationOwnerByScope(resourceType, resourceCol, SessionUtils.getUserId(), List.of(id))) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
        if (v instanceof List<?> ids) {
            // noinspection unchecked
            if (!extCheckOwnerMapper.checkoutOrganizationOwnerByScope(resourceType, resourceCol, SessionUtils.getUserId(), (List<String>) ids)) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
    }

}
