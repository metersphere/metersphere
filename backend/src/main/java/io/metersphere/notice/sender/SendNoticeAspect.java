package io.metersphere.notice.sender;

import com.alibaba.fastjson.JSON;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.notice.annotation.SendNotice;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;

@Aspect
@Component
public class SendNoticeAspect {
    @Resource
    private AfterReturningNoticeSendService afterReturningNoticeSendService;

    private ExpressionParser parser = new SpelExpressionParser();
    private LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();


    @Pointcut("@annotation(io.metersphere.notice.annotation.SendNotice)")
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

            InvocationHandler invocationHandler = Proxy.getInvocationHandler(sendNotice);
            Field value = invocationHandler.getClass().getDeclaredField("memberValues");
            value.setAccessible(true);

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
                Map<String, Object> memberValues = (Map<String, Object>) value.get(invocationHandler);
                memberValues.put("source", JSON.toJSONString(v, WriteMapNullValue));
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
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
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(sendNotice);
            Field value = invocationHandler.getClass().getDeclaredField("memberValues");
            value.setAccessible(true);

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
                    Map<String, Object> memberValues = (Map<String, Object>) value.get(invocationHandler);
                    memberValues.put("source", JSON.toJSONString(v, WriteMapNullValue));
                }
            }

            EvaluationContext context = new StandardEvaluationContext();
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }
            SessionUser sessionUser = SessionUtils.getUser();
            String currentProjectId = SessionUtils.getCurrentProjectId();
            afterReturningNoticeSendService.sendNotice(sendNotice, retValue, sessionUser, currentProjectId);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }
}
