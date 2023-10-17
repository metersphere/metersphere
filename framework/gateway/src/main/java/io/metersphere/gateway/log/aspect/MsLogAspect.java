package io.metersphere.gateway.log.aspect;


import io.metersphere.base.domain.OperatingLogWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.gateway.log.annotation.MsAuditLog;
import io.metersphere.log.service.OperatingLogService;
import io.metersphere.request.LoginRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
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

import jakarta.annotation.Resource;
import org.springframework.web.server.WebSession;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 系统日志：切面处理类
 */
@Aspect
@Component
public class MsLogAspect {
    /**
     * 解析spel表达式
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
    @Pointcut("@annotation(io.metersphere.gateway.log.annotation.MsAuditLog)")
    public void logPointCut() {
    }


    /**
     * 切面 配置通知
     */
    @AfterReturning("logPointCut()")
    public void saveLog(JoinPoint joinPoint) {
        try {

            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //获取参数对象数组
            Object[] args = joinPoint.getArgs();

            //获取操作
            MsAuditLog msLog = method.getAnnotation(MsAuditLog.class);
            if (msLog != null) {
                //保存日志
                OperatingLogWithBLOBs msOperLog = new OperatingLogWithBLOBs();

                //保存获取的操作
                msOperLog.setId(UUID.randomUUID().toString());
                // 操作类型
                msOperLog.setOperType(msLog.type().name());
                // 项目ID
                msOperLog.setProjectId(msLog.project());

                String module = msLog.module();
                msOperLog.setOperModule(StringUtils.isNotEmpty(module) ? module : msLog.module());
                //获取方法参数名
                String[] params = discoverer.getParameterNames(method);
                //将参数纳入Spring管理
                EvaluationContext context = new StandardEvaluationContext();
                for (int len = 0; len < params.length; len++) {
                    context.setVariable(params[len], args[len]);
                }

                // 标题
                if (StringUtils.isNotEmpty(msLog.title())) {
                    String title = msLog.title();
                    try {
                        Expression titleExp = parser.parseExpression(title);
                        title = titleExp.getValue(context, String.class);
                        msOperLog.setOperTitle(title);
                    } catch (Exception e) {
                        msOperLog.setOperTitle(title);
                    }
                }

                //获取请求的类名
                String className = joinPoint.getTarget().getClass().getName();
                //获取请求的方法名
                String methodName = method.getName();
                msOperLog.setOperMethod(className + "." + methodName);

                msOperLog.setOperTime(System.currentTimeMillis());

                for (Object arg : args) {
                    if (arg instanceof WebSession session) {
                        Object user = session.getAttribute("user");
                        if (user != null) {
                            String userId = (String) MethodUtils.invokeExactMethod(user, "getId");
                            msOperLog.setOperUser(userId);
                            msOperLog.setCreateUser(userId);
                        }
                        break;
                    }
                }

                if (StringUtils.isNotEmpty(msOperLog.getOperTitle()) && msOperLog.getOperTitle().length() > 6000) {
                    msOperLog.setOperTitle(msOperLog.getOperTitle().substring(0, 5999));
                }
//                msOperLog.setOperPath(path);
                operatingLogService.create(msOperLog, msOperLog.getSourceId());
            }
        } catch (Exception e) {
            LogUtil.error("操作日志写入异常：" + joinPoint.getSignature());
        }
    }


}
