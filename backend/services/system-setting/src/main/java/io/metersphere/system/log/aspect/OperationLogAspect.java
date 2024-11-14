package io.metersphere.system.log.aspect;

import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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

    private final static String ID = "id";
    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private OperationLogService operationLogService;
    // 批量变更前后内容
    private final ThreadLocal<List<LogDTO>> beforeValues = new ThreadLocal<>();

    private final ThreadLocal<String> localUser = new ThreadLocal<>();

    private final ThreadLocal<String> localOrganizationId = new ThreadLocal<>();

    private final ThreadLocal<String> localProjectId = new ThreadLocal<>();

    // 此方法随时补充类型，需要在内容变更前执行的类型都可以加入
    private final OperationLogType[] beforeMethodNames = new OperationLogType[]{OperationLogType.UPDATE, OperationLogType.DELETE, OperationLogType.COPY
            , OperationLogType.RECOVER, OperationLogType.DISASSOCIATE,OperationLogType.ASSOCIATE, OperationLogType.ARCHIVED};
    // 需要后置执行合并内容的
    private final OperationLogType[] postMethodNames = new OperationLogType[]{OperationLogType.ADD, OperationLogType.UPDATE, OperationLogType.RERUN};

    /**
     * 定义切点 @Pointcut 在注解的位置切入代码
     */
    @Pointcut("@annotation(io.metersphere.system.log.annotation.Log)")
    public void logPointCut() {
    }

    @AfterThrowing(pointcut = "logPointCut()", throwing = "ex")
    public void handleException(Exception ex) {
        localUser.remove();
        beforeValues.remove();
        localOrganizationId.remove();
        localProjectId.remove();
        LogUtils.error(ex);
    }

    @Before("logPointCut()")
    public void before(JoinPoint joinPoint) {
        try {
            localUser.set(SessionUtils.getUserId());
            localOrganizationId.set(SessionUtils.getCurrentOrganizationId());
            localProjectId.set(SessionUtils.getCurrentProjectId());

            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            Log msLog = method.getAnnotation(Log.class);
            if (msLog != null && isMatch(msLog.type())) {
                //获取参数对象数组
                Object[] args = joinPoint.getArgs();
                //获取方法参数名
                String[] params = discoverer.getParameterNames(method);
                //将参数纳入Spring管理
                EvaluationContext context = new StandardEvaluationContext();

                for (int len = 0; len < Objects.requireNonNull(params).length; len++) {
                    context.setVariable(params[len], args[len]);
                }
                boolean isNext = false;
                for (Class<?> clazz : msLog.msClass()) {
                    context.setVariable("msClass", applicationContext.getBean(clazz));
                    isNext = true;
                }
                if (!isNext) {
                    return;
                }
                // 初始化details内容
                initBeforeDetails(msLog, context);
            }
        } catch (Exception e) {
            LogUtils.error("操作日志写入异常：" + joinPoint.getSignature());
        }
    }

    public boolean isMatch(OperationLogType keyword) {
        return Arrays.stream(beforeMethodNames)
                .anyMatch(input -> input.contains(keyword));
    }

    private void initBeforeDetails(Log msLog, EvaluationContext context) {
        try {
            // 批量内容处理
            Expression expression = parser.parseExpression(msLog.expression());
            Object obj = expression.getValue(context);
            if (obj == null) {
                return;
            }
            if (obj instanceof List<?>) {
                beforeValues.set((List<LogDTO>) obj);
            } else {
                List<LogDTO> LogDTOs = new ArrayList<>();
                LogDTOs.add((LogDTO) obj);
                beforeValues.set(LogDTOs);
            }

        } catch (Exception e) {
            LogUtils.error("未获取到details内容", e);
        }
    }

    public void mergeLists(List<LogDTO> beforeLogs, List<LogDTO> postLogs) {
        if (CollectionUtils.isEmpty(beforeLogs) && CollectionUtils.isNotEmpty(postLogs)) {
            beforeValues.set(postLogs);
            return;
        }
        if (CollectionUtils.isEmpty(beforeLogs) && CollectionUtils.isEmpty(postLogs)) {
            return;
        }
        Map<String, LogDTO> postDto = postLogs.stream().collect(Collectors.toMap(LogDTO::getSourceId, item -> item));
        beforeLogs.forEach(item -> {
            LogDTO post = postDto.get(item.getSourceId());
            if (post != null) {
                item.setModifiedValue(post.getOriginalValue());
            }
        });
    }

    private void initPostDetails(Log msLog, EvaluationContext context) {
        try {
            if (StringUtils.isBlank(msLog.expression())) {
                return;
            }
            // 批量内容处理
            Expression expression = parser.parseExpression(msLog.expression());
            Object obj = expression.getValue(context);
            if (obj == null) {
                return;
            }

            if (obj instanceof List<?>) {
                mergeLists(beforeValues.get(), (List<LogDTO>) obj);
            } else if (obj instanceof LogDTO log) {
                if (CollectionUtils.isNotEmpty(beforeValues.get())) {
                    beforeValues.get().getFirst().setModifiedValue(log.getOriginalValue());
                } else {
                    beforeValues.set(new ArrayList<>() {{
                        this.add(log);
                    }});
                }
            }

        } catch (Exception e) {
            LogUtils.error("未获取到details内容", e);
        }
    }


    public String getId(Object result) {
        try {
            if (result != null) {
                String resultStr = JSON.toJSONString(result);
                Map object = JSON.parseMap(resultStr);
                if (MapUtils.isNotEmpty(object) && object.containsKey(ID)) {
                    Object nameValue = object.get(ID);
                    if (ObjectUtils.isNotEmpty(nameValue)) {
                        return nameValue.toString();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error("未获取到响应资源Id");
        }
        return null;
    }

    private void save(Object result) {
        List<LogDTO> logDTOList = beforeValues.get();
        if (CollectionUtils.isEmpty(logDTOList)) {
            return;
        }
        logDTOList.forEach(logDTO -> {
            logDTO.setSourceId(StringUtils.defaultIfBlank(logDTO.getSourceId(), getId(result)));
            logDTO.setCreateUser(StringUtils.defaultIfBlank(logDTO.getCreateUser(), localUser.get()));
            logDTO.setOrganizationId(StringUtils.defaultIfBlank(logDTO.getOrganizationId(), localOrganizationId.get()));
            logDTO.setProjectId(StringUtils.defaultIfBlank(logDTO.getProjectId(), localProjectId.get()));
            logDTO.setMethod(getMethod());
            logDTO.setPath(getPath());
        });

        // 单条存储
        if (logDTOList.size() == 1) {
            operationLogService.add(logDTOList.getFirst());
        } else {
            operationLogService.batchAdd(logDTOList);
        }
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
            // 获取操作
            Log msLog = method.getAnnotation(Log.class);
            if (msLog != null) {
                //获取参数对象数组
                Object[] args = joinPoint.getArgs();
                //获取方法参数名
                String[] params = discoverer.getParameterNames(method);
                //将参数纳入Spring管理
                EvaluationContext context = new StandardEvaluationContext();
                for (int len = 0; len < Objects.requireNonNull(params).length; len++) {
                    context.setVariable(params[len], args[len]);
                }
                for (Class<?> clazz : msLog.msClass()) {
                    context.setVariable("msClass", applicationContext.getBean(clazz));
                }
                // 需要后置再次执行的方法
                if (Arrays.stream(postMethodNames).anyMatch(input -> input.contains(msLog.type()))) {
                    initPostDetails(msLog, context);
                }
                // 存储日志结果
                save(result);
            }
        } catch (Exception e) {
            LogUtils.error("操作日志写入异常：{}", e);
        } finally {
            localUser.remove();
            beforeValues.remove();
        }
    }


    public static String getPath() {
        HttpServletRequest httpRequest = getHttpRequest();
        String path = StringUtils.EMPTY;
        if (httpRequest != null) {
            path = httpRequest.getRequestURI();
            if (StringUtils.isNotBlank(httpRequest.getQueryString())) {
                path += "?" + httpRequest.getQueryString();
            }
        }
        return path.length() > 255 ? path.substring(0, 255) : path;
    }

    public static String getMethod() {
        HttpServletRequest httpRequest = getHttpRequest();
        return httpRequest == null ? StringUtils.EMPTY : httpRequest.getMethod();
    }

    private static HttpServletRequest getHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }
}
