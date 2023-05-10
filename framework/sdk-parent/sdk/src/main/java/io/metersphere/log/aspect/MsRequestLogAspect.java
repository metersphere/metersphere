package io.metersphere.log.aspect;


import io.metersphere.base.domain.OperatingLogWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.log.service.OperatingLogService;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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
import java.util.*;

/**
 * 系统日志：切面处理类
 */
@Aspect
@Component
public class MsRequestLogAspect {
    /**
     * 解析 spel 表达式
     */
    ExpressionParser parser = new SpelExpressionParser();
    /**
     * 将方法参数纳入Spring管理
     */
    StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    @Resource
    private OperatingLogService operatingLogService;

    /**
     * 定义切点 @Pointcut 在注解的位置切入代码
     */
    @Pointcut("@annotation(io.metersphere.log.annotation.MsRequestLog)")
    public void logPointCut() {
    }

    /**
     * 切面 配置通知
     */
    @AfterReturning(value = "logPointCut()", returning = "result")
    public void saveLog(JoinPoint joinPoint, Object result) {
        try {
            if (this.hasLogicalFail(result)) {
                return;
            }
            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //获取参数对象数组
            Object[] args = joinPoint.getArgs();

            //获取操作
            MsRequestLog logAnnotation = method.getAnnotation(MsRequestLog.class);
            if (logAnnotation != null) {

                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String path = request.getServletPath();

                List<DetailColumn> requestParams = new ArrayList<>(3);

                //保存日志
                OperatingLogWithBLOBs operatingLog = new OperatingLogWithBLOBs();

                //保存获取的操作
                operatingLog.setId(UUID.randomUUID().toString());
                // 操作类型
                operatingLog.setOperType(OperLogConstants.REQUEST.name());

                operatingLog.setOperModule(logAnnotation.module());

                //获取方法参数名
                String[] params = discoverer.getParameterNames(method);
                //将参数纳入Spring管理
                EvaluationContext context = new StandardEvaluationContext();
                for (int len = 0; len < params.length; len++) {
                    context.setVariable(params[len], args[len]);
                    requestParams.add(new DetailColumn(params[len], params[len], null, args[len]));
                }

                // 标题
                operatingLog.setOperTitle(path);
                // 项目ID
                operatingLog.setProjectId(expressionParse(context, logAnnotation.project()));

                if (StringUtils.isBlank(operatingLog.getProjectId())) {
                    operatingLog.setProjectId(SessionUtils.getCurrentProjectId());
                }

                operatingLog.setCreateUser(SessionUtils.getUserId());
                operatingLog.setOperUser(SessionUtils.getUserId());

                OperatingLogDetails details = new OperatingLogDetails(operatingLog.getSourceId(),
                        operatingLog.getProjectId(),
                        operatingLog.getOperTitle(),
                        operatingLog.getCreateUser(),
                        requestParams);

                operatingLog.setOperContent(JSON.toJSONString(details));

                //获取请求的类名
                String className = joinPoint.getTarget().getClass().getName();
                //获取请求的方法名
                String methodName = method.getName();
                operatingLog.setOperMethod(className + "." + methodName);
                operatingLog.setOperTime(System.currentTimeMillis());

                if (StringUtils.isNotEmpty(operatingLog.getOperTitle()) && operatingLog.getOperTitle().length() > 6000) {
                    operatingLog.setOperTitle(operatingLog.getOperTitle().substring(0, 5999));
                }

                operatingLog.setOperPath(request.getServletPath());
                operatingLogService.create(operatingLog, operatingLog.getSourceId());
            }
        } catch (Exception e) {
            LogUtil.error("操作日志写入异常：" + joinPoint.getSignature());
        }
    }

    private String expressionParse(EvaluationContext context, String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            Expression expression = parser.parseExpression(value);
            value = expression.getValue(context, String.class);
            return value;
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 方法正常返回，但是执行逻辑是失败的
     * @param result 方法返回值
     * @return boolean
     */
    private boolean hasLogicalFail(Object result) {
        if (result instanceof ExcelResponse) {
            return BooleanUtils.isFalse(((ExcelResponse<?>) result).getSuccess());
        }
        return false;
    }
}
