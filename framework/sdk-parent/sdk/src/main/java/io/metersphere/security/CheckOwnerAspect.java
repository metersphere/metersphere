package io.metersphere.security;


import io.metersphere.base.mapper.ext.ExtCheckOwnerMapper;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.util.WebUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.util.List;


@Aspect
@Component
public class CheckOwnerAspect {

    private ExpressionParser parser = new SpelExpressionParser();
    private StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    @Resource
    private ExtCheckOwnerMapper extCheckOwnerMapper;

    @Pointcut("@annotation(io.metersphere.security.CheckOwner)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {

        // apikey 过来的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            if (ApiKeyHandler.isApiKeyCall(request) && !SecurityUtils.getSubject().isAuthenticated()) {
                String userId = ApiKeyHandler.getUser(WebUtils.toHttp(request));
                SecurityUtils.getSubject().login(new UsernamePasswordToken(userId, SSOSessionHandler.random));
            }
        }

        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();
        //获取参数对象数组
        Object[] args = joinPoint.getArgs();
        CheckOwner checkOwner = method.getAnnotation(CheckOwner.class);
        long count = SessionUtils.getUser().getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getId(), UserGroupConstants.SUPER_GROUP))
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
        Expression titleExp = parser.parseExpression(resourceId);
        Object v = titleExp.getValue(context, Object.class);
        if (v instanceof String id) {
            if (!extCheckOwnerMapper.checkoutOwner(resourceType, SessionUtils.getUserId(), List.of(id))) {
                MSException.throwException(Translator.get("check_owner_case"));
            }
        }
        if (v instanceof List ids) {
            if (!extCheckOwnerMapper.checkoutOwner(resourceType, SessionUtils.getUserId(), ids)) {
                MSException.throwException(Translator.get("check_owner_case"));
            }
        }
    }

    @After("pointcut()")
    public void after() {
        // apikey 过来的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
            // apikey 退出
            if (ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request)) && SecurityUtils.getSubject().isAuthenticated()) {
                SecurityUtils.getSubject().logout();
            }
        }
    }
}
