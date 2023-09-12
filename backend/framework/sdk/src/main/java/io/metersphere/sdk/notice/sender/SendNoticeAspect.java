package io.metersphere.sdk.notice.sender;


import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.sdk.notice.annotation.SendNotice;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Aspect
@Component
public class SendNoticeAspect {
    @Resource
    private AfterReturningNoticeSendService afterReturningNoticeSendService;

    private ExpressionParser parser = new SpelExpressionParser();
    private StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();
    private ThreadLocal<String> source = new ThreadLocal<>();

    @Pointcut("@annotation(io.metersphere.sdk.notice.annotation.SendNotice)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        try {
            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //获取参数对象数组
            Object[] args = joinPoint.getArgs();
            SendNotice sendNotice = method.getAnnotation(SendNotice.class);

            if (StringUtils.isNotEmpty(sendNotice.target())) {
                // 操作内容
                //获取方法参数名
                String[] params = discoverer.getParameterNames(method);
                //将参数纳入Spring管理
                EvaluationContext context = new StandardEvaluationContext();
                for (int len = 0; len < params.length; len++) {
                    context.setVariable(params[len], args[len]);
                }
                context.setVariable("targetClass", CommonBeanFactory.getBean(sendNotice.targetClass()));

                String target = sendNotice.target();
                Expression titleExp = parser.parseExpression(target);
                Object v = titleExp.getValue(context, Object.class);
                source.set(JSON.toJSONString(v));
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
    }

    @AfterReturning(value = "pointcut()", returning = "retValue")
    public void sendNotice(JoinPoint joinPoint, Object retValue) {
        try {
            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //获取参数对象数组
            Object[] args = joinPoint.getArgs();
            //获取方法参数名
            String[] params = discoverer.getParameterNames(method);
            //获取操作
            SendNotice sendNotice = method.getAnnotation(SendNotice.class);
            // 再次从数据库查询一次内容，方便获取最新参数

            if (StringUtils.isNotEmpty(sendNotice.target())) {
                //将参数纳入Spring管理
                EvaluationContext context = new StandardEvaluationContext();
                for (int len = 0; len < params.length; len++) {
                    context.setVariable(params[len], args[len]);
                }
                context.setVariable("targetClass", CommonBeanFactory.getBean(sendNotice.targetClass()));

                String target = sendNotice.target();
                Expression titleExp = parser.parseExpression(target);
                Object v = titleExp.getValue(context, Object.class);
                // 查询结果如果是null或者是{}，不使用这个值
                String jsonObject = JSON.toJSONString(v);
                if (v != null && !StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
                    source.set(JSON.toJSONString(v));
                }
            }

            List<Map> resources = new ArrayList<>();
            String v = source.get();
            if (StringUtils.isNotBlank(v)) {
                // array
                if (StringUtils.startsWith(v, "[")) {
                    resources.addAll(JSON.parseArray(v, Map.class));
                }
                // map
                else {
                    Map<?, ?> value = JSON.parseObject(v, Map.class);
                    resources.add(value);
                }
            } else {
                resources.add(new BeanMap(retValue));
            }

            SessionUser sessionUser = SessionUtils.getUser();
            String currentProjectId = SessionUtils.getCurrentProjectId();
            afterReturningNoticeSendService.sendNotice(sendNotice, resources, sessionUser, currentProjectId);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        } finally {
            source.remove();
        }
    }
}
