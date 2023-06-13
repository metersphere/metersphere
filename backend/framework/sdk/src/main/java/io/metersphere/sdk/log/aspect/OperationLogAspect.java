package io.metersphere.sdk.log.aspect;

import io.metersphere.sdk.log.annotation.RequestLog;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.domain.OperationLog;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 系统日志：切面处理类
 */
@Aspect
@Component
public class OperationLogAspect {
    /**
     * 解析spell表达式
     */
    ExpressionParser parser = new SpelExpressionParser();
    /**
     * 将方法参数纳入Spring管理
     */
    private final StandardReflectionParameterNameDiscoverer
            discoverer = new StandardReflectionParameterNameDiscoverer();

    private final String ID = "id";
    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private OperationLogService operationLogService;

    private ThreadLocal<List<OperationLog>> beforeValue = new ThreadLocal<>();

    private ThreadLocal<String> treadLocalDetails = new ThreadLocal<>();

    private ThreadLocal<String> treadLocalSourceId = new ThreadLocal<>();

    private ThreadLocal<String> localUser = new ThreadLocal<>();

    private final String[] methodNames = new String[]{"delete", "update", "add"};

    /**
     * 定义切点 @Pointcut 在注解的位置切入代码
     */
    @Pointcut("@annotation(io.metersphere.sdk.log.annotation.RequestLog)")
    public void logPointCut() {
    }

    @Before("logPointCut()")
    public void before(JoinPoint joinPoint) {
        try {
            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            RequestLog msLog = method.getAnnotation(RequestLog.class);
            if (msLog != null && msLog.isBefore()) {
                //获取参数对象数组
                Object[] args = joinPoint.getArgs();
                //获取方法参数名
                String[] params = discoverer.getParameterNames(method);
                //将参数纳入Spring管理
                EvaluationContext context = new StandardEvaluationContext();

                for (int len = 0; len < params.length; len++) {
                    context.setVariable(params[len], args[len]);
                }
                boolean isNext = false;
                for (Class clazz : msLog.msClass()) {
                    if (clazz.getName().endsWith("SessionUtils")) {
                        localUser.set(SessionUtils.getUserId());
                        continue;
                    }
                    context.setVariable("msClass", applicationContext.getBean(clazz));
                    isNext = true;
                }
                if (!isNext) {
                    return;
                }
                // 初始化details内容
                initDetails(msLog, context);
                // 初始化资源id
                initResourceId(msLog, context);
            }
        } catch (Exception e) {
            LogUtils.error("操作日志写入异常：" + joinPoint.getSignature());
        }
    }

    public boolean isMatch(String keyword) {
        return Arrays.stream(methodNames)
                .anyMatch(input -> input.contains(keyword));
    }

    private void initDetails(RequestLog msLog, EvaluationContext context) {
        try {
            // 批量内容处理
            if (StringUtils.isNotBlank(msLog.details()) && msLog.details().startsWith("#msClass")) {
                if (msLog.isBatch()) {
                    Expression expression = parser.parseExpression(msLog.details());
                    List<OperationLog> beforeContent = expression.getValue(context, List.class);
                    beforeValue.set(beforeContent);
                } else {
                    Expression detailsEx = parser.parseExpression(msLog.details());
                    String details = detailsEx.getValue(context, String.class);
                    this.treadLocalDetails.set(details);
                }
            } else if (StringUtils.isNotBlank(msLog.details()) && msLog.details().startsWith("#")) {
                Expression titleExp = parser.parseExpression(msLog.details());
                String details = titleExp.getValue(context, String.class);
                this.treadLocalDetails.set(details);
            } else {
                this.treadLocalDetails.set(msLog.details());
            }
        } catch (Exception e) {
            LogUtils.error("未获取到details内容", e);
            this.treadLocalDetails.set(msLog.details());
        }
    }

    private void initResourceId(RequestLog msLog, EvaluationContext context) {
        try {
            // 批量内容处理
            if (StringUtils.isNotBlank(msLog.sourceId()) && msLog.sourceId().startsWith("#msClass")) {
                Expression detailsEx = parser.parseExpression(msLog.sourceId());
                String sourceId = detailsEx.getValue(context, String.class);
                treadLocalSourceId.set(sourceId);
            } else if (StringUtils.isNotBlank(msLog.sourceId()) && msLog.sourceId().startsWith("#")) {
                Expression titleExp = parser.parseExpression(msLog.sourceId());
                String sourceId = titleExp.getValue(context, String.class);
                treadLocalSourceId.set(sourceId);
            } else {
                treadLocalSourceId.set(msLog.sourceId());
            }
        } catch (Exception e) {
            LogUtils.error("未获取到资源id", e);
            treadLocalSourceId.set(msLog.sourceId());
        }
    }

    private String getProjectId(RequestLog msLog, EvaluationContext context) {
        try {
            if (StringUtils.isNotBlank(msLog.projectId()) && msLog.projectId().startsWith("#")) {
                Expression titleExp = parser.parseExpression(msLog.projectId());
                return titleExp.getValue(context, String.class);
            } else {
                return msLog.projectId();
            }
        } catch (Exception e) {
            return msLog.projectId();
        }
    }

    private void add(OperationLog operationLog, RequestLog msLog, JoinPoint joinPoint, Object result) {
        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();
        //获取参数对象数组
        Object[] args = joinPoint.getArgs();
        // 操作类型
        operationLog.setType(msLog.type().name());
        // 创建用户
        if (StringUtils.isNotBlank(msLog.createUser())) {
            operationLog.setCreateUser(msLog.createUser());
        }
        // 项目ID
        operationLog.setProjectId(msLog.projectId());
        operationLog.setModule(msLog.module());
        //获取方法参数名
        String[] params = discoverer.getParameterNames(method);
        //将参数纳入Spring管理
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], args[len]);
        }
        for (Class clazz : msLog.msClass()) {
            context.setVariable("msClass", applicationContext.getBean(clazz));
        }
        // 批量编辑操作
        if (msLog.isBatch()) {
            operationLogService.batchAdd(beforeValue.get());
            return;
        }
        // 项目ID表达式
        operationLog.setProjectId(getProjectId(msLog, context));

        if (!msLog.isBefore()) {
            // 初始化资源id
            initResourceId(msLog, context);
            // 初始化内容详情
            initDetails(msLog, context);
        }
        // 内容详情
        operationLog.setDetails(treadLocalDetails.get());
        // 资源id
        operationLog.setSourceId(treadLocalSourceId.get());

        if (StringUtils.isBlank(operationLog.getCreateUser())) {
            operationLog.setCreateUser(localUser.get());
        }
        // 从返回内容中获取资源id
        if (StringUtils.isEmpty(operationLog.getSourceId()) && !msLog.isBatch()) {
            operationLog.setSourceId(getId(result));
        }
        operationLogService.add(operationLog);
    }

    public String getId(Object result) {
        try {
            if (result != null) {
                String resultStr = JSON.toJSONString(result);
                Map object = JSON.parseMap(resultStr);
                if (object != null && object.containsKey(ID)) {
                    Object nameValue = object.get(ID);
                    if (ObjectUtils.isNotEmpty(nameValue)) {
                        return nameValue.toString();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error("未获取到响应Id");
        }
        return null;
    }

    /**
     * 切面 配置通知
     */
    @AfterReturning(value = "logPointCut()", returning = "result")
    public void saveLog(JoinPoint joinPoint, Object result) {
        try {
            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //保存日志
            OperationLog operationLog = new OperationLog();
            //保存获取的操作
            operationLog.setId(UUID.randomUUID().toString());
            String className = joinPoint.getTarget().getClass().getName();
            operationLog.setMethod(StringUtils.join(className, ".", method.getName()));
            operationLog.setCreateTime(System.currentTimeMillis());
            operationLog.setCreateUser(SessionUtils.getUserId());
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            operationLog.setPath(request.getServletPath());
            // 获取操作
            RequestLog msLog = method.getAnnotation(RequestLog.class);
            if (msLog != null) {
                add(operationLog, msLog, joinPoint, result);
            }
            // 兼容遗漏注解的内容
            else if (isMatch(method.getName())) {
                operationLogService.add(operationLog);
            }
        } catch (Exception e) {
            LogUtils.error("操作日志写入异常：", e);
        } finally {
            localUser.remove();
            beforeValue.remove();
            treadLocalDetails.remove();
        }
    }

}
