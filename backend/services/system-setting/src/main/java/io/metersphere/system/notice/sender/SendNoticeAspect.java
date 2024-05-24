package io.metersphere.system.notice.sender;


import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.MapUtils;
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
    private CommonNoticeSendService commonNoticeSendService;

    private ExpressionParser parser = new SpelExpressionParser();
    private StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();
    private ThreadLocal<String> source = new ThreadLocal<>();

    private final static String ID = "id";
    private final static String PROJECT_ID = "projectId";
    private final static String CREATE_USER = "createUser";
    private final static String CREATE_TIME = "createTime";
    private final static String UPDATE_TIME = "updateTime";
    private final static String UPDATE_USER = "updateUser";



    @Pointcut("@annotation(io.metersphere.system.notice.annotation.SendNotice)")
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
            EvaluationContext context = new StandardEvaluationContext();
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }
            // 再次从数据库查询一次内容，方便获取最新参数
            if (StringUtils.isNotEmpty(sendNotice.target())) {
                //将参数纳入Spring管理

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
            String taskType = sendNotice.taskType();
            // taskType
            if (StringUtils.isNotEmpty(sendNotice.taskType())) {
                try {
                    Expression titleExp = parser.parseExpression(taskType);
                    taskType = titleExp.getValue(resources, String.class);

                } catch (Exception e) {
                    LogUtils.info("使用原值");
                }
            }
            String event = sendNotice.event();
            // event
            if (StringUtils.isNotEmpty(sendNotice.event())) {
                try {
                    Expression titleExp = parser.parseExpression(event);
                    event = titleExp.getValue(context, String.class);
                } catch (Exception e) {
                    LogUtils.info("使用原值");
                }
            }

            SessionUser sessionUser = SessionUtils.getUser();
            String currentProjectId = SessionUtils.getCurrentProjectId();
            LogUtils.info("event:" + event);
            String resultStr = JSON.toJSONString(retValue);
            Map object = JSON.parseMap(resultStr);
            if (MapUtils.isNotEmpty(object)) {
                for (Map resource : resources) {
                    if (object.containsKey(ID) && resource.get(ID) == null) {
                        resource.put(ID, object.get(ID));
                    }
                    if (object.containsKey(PROJECT_ID) && resource.get(PROJECT_ID) == null) {
                        resource.put(PROJECT_ID, object.get(PROJECT_ID));
                    }
                    if (object.containsKey(CREATE_USER) && resource.get(CREATE_USER) == null) {
                        resource.put(CREATE_USER, object.get(CREATE_USER));
                    }
                    if (object.containsKey(CREATE_TIME) && resource.get(CREATE_TIME) == null) {
                        resource.put(CREATE_TIME, object.get(CREATE_TIME));
                    }
                    if (object.containsKey(UPDATE_TIME) && resource.get(UPDATE_TIME) == null) {
                        resource.put(UPDATE_TIME, object.get(UPDATE_TIME));
                    }
                    if (object.containsKey(UPDATE_USER) && resource.get(UPDATE_USER) == null) {
                        resource.put(UPDATE_USER, object.get(UPDATE_USER));
                    }
                }
            }
            commonNoticeSendService.sendNotice(taskType, event, resources, sessionUser, currentProjectId);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        } finally {
            source.remove();
        }
    }
}
