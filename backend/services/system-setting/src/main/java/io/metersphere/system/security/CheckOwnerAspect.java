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
public class CheckOwnerAspect {

    private ExpressionParser parser = new SpelExpressionParser();
    private StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();
    // 组织归属的资源
    private static final List<String> orgResources = List.of("organization_parameter", "plugin_organization", "project", "service_integration");
    @Resource
    private ExtCheckOwnerMapper extCheckOwnerMapper;

    @Pointcut("@annotation(io.metersphere.system.security.CheckOwner)")
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
        CheckOwner checkOwner = method.getAnnotation(CheckOwner.class);
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
        String relationType = checkOwner.relationType();
        Expression titleExp = parser.parseExpression(resourceId);
        Object v = titleExp.getValue(context, Object.class);
        // 归属组织的资源
        if (orgResources.contains(resourceType)) {
            handleOrganizationResource(v, resourceType);
        }
        // 组织自身
        else if (StringUtils.equals(resourceType, "organization")) {
            handleOrganization(v);
        }
        // 中间表
        else if (StringUtils.isNotBlank(relationType)) {
            handleProjectResource(v, resourceType, relationType);
        }
        // 归属项目的资源
        else {
            handleProjectResource(v, resourceType);
        }
    }

    private void handleOrganization(Object v) {
        if (v instanceof String id) {
            if (!extCheckOwnerMapper.checkoutOrganization(SessionUtils.getUserId(), List.of(id))) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
        if (v instanceof List ids) {
            if (!extCheckOwnerMapper.checkoutOrganization(SessionUtils.getUserId(), ids)) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
    }

    private void handleProjectResource(Object v, String resourceType) {
        if (v instanceof String id) {
            if (!extCheckOwnerMapper.checkoutOwner(resourceType, SessionUtils.getUserId(), List.of(id))) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
        if (v instanceof List ids) {
            if (!extCheckOwnerMapper.checkoutOwner(resourceType, SessionUtils.getUserId(), ids)) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
    }

    private void handleProjectResource(Object v, String resourceType, String relationType) {
        if (v instanceof String id) {
            if (!extCheckOwnerMapper.checkoutRelationOwner(resourceType, relationType, SessionUtils.getUserId(), List.of(id))) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
        if (v instanceof List ids) {
            if (!extCheckOwnerMapper.checkoutRelationOwner(resourceType, relationType, SessionUtils.getUserId(), ids)) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
    }


    private void handleOrganizationResource(Object v, String resourceType) {
        if (v instanceof String id) {
            if (!extCheckOwnerMapper.checkoutOrganizationOwner(resourceType, SessionUtils.getUserId(), List.of(id))) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
        if (v instanceof List ids) {
            if (!extCheckOwnerMapper.checkoutOrganizationOwner(resourceType, SessionUtils.getUserId(), ids)) {
                throw new MSException(Translator.get("check_owner_case"));
            }
        }
    }
}
