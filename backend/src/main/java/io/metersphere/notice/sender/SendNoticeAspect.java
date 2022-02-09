package io.metersphere.notice.sender;

import com.alibaba.fastjson.JSON;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.beanutils.BeanMap;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;

@Aspect
@Component
public class SendNoticeAspect {
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private SystemParameterService systemParameterService;

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
                if (v != null && !StringUtils.equals("{}", jsonObject)) {
                    Map<String, Object> memberValues = (Map<String, Object>) value.get(invocationHandler);
                    memberValues.put("source", JSON.toJSONString(v, WriteMapNullValue));
                }
            }

            EvaluationContext context = new StandardEvaluationContext();
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], args[len]);
            }

            handleNotice(sendNotice, retValue);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private void handleNotice(SendNotice sendNotice, Object retValue) {
        //
        List<Map> resources = new ArrayList<>();
        String source = sendNotice.source();
        if (StringUtils.isNotBlank(source)) {
            // array
            if (StringUtils.startsWith(source, "[")) {
                resources.addAll(JSON.parseArray(source, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(source, Map.class);
                resources.add(value);
            }
        } else {
            resources.add(new BeanMap(retValue));
        }
        // 有批量操作发送多次
        for (Map resource : resources) {
            Map<String, Object> paramMap = getParamMap(resource);
            String context = getContext(sendNotice, paramMap);

            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(SessionUtils.getUserId())
                    .context(context)
                    .subject(sendNotice.subject())
                    .mailTemplate(sendNotice.mailTemplate())
                    .paramMap(paramMap)
                    .event(sendNotice.event())
                    .status((String) paramMap.get("status"))
                    .excludeSelf(true)
                    .build();
            noticeSendService.send(sendNotice.taskType(), noticeModel);
        }
    }

    private Map<String, Object> getParamMap(Map resource) {
        Map<String, Object> paramMap = new HashMap<>();
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("operator", SessionUtils.getUser().getName());
        paramMap.put("planShareUrl", ""); // 占位符
        paramMap.putAll(resource);
        return paramMap;
    }

    private String getContext(SendNotice sendNotice, Map<String, Object> paramMap) {
        String operation = "";
        switch (sendNotice.event()) {
            case NoticeConstants.Event.CREATE:
            case NoticeConstants.Event.CASE_CREATE:
                operation = "创建了";
                break;
            case NoticeConstants.Event.UPDATE:
            case NoticeConstants.Event.CASE_UPDATE:
                operation = "更新了";
                break;
            case NoticeConstants.Event.DELETE:
            case NoticeConstants.Event.CASE_DELETE:
                operation = "删除了";
                break;
            case NoticeConstants.Event.COMMENT:
                operation = "评论了";
                break;
            case NoticeConstants.Event.COMPLETE:
                operation = "完成了";
                break;
            case NoticeConstants.Event.CLOSE_SCHEDULE:
                operation = "关闭了定时任务";
                break;
            default:
                break;
        }
        String subject = sendNotice.subject();
        String resource = StringUtils.removeEnd(subject, "通知");

        String name = "";
        if (paramMap.containsKey("name")) {
            name = ": ${name}";
        }
        if (paramMap.containsKey("title")) {
            name = ": ${title}";
        }
        return "${operator}" + operation + resource + name;
    }
}
