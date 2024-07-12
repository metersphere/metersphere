package io.metersphere.system.config;

import io.metersphere.plugin.sdk.spi.QuotaPlugin;
import io.metersphere.system.service.PluginLoadService;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class QuotaInterceptor {
    @Resource
    private PluginLoadService pluginLoadService;
    // 插件ID
    private final String QUOTA = "cloud-quota-plugin";

    /*@Around("execution(* io.metersphere..*(..)) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)|| " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping))")*/
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        // 验证配额规则
        PluginWrapper pluginWrapper = pluginLoadService.getMsPluginManager().getPlugin(QUOTA);
        if (pluginWrapper != null && pluginWrapper.getPlugin() instanceof QuotaPlugin quotaPlugin) {
            quotaPlugin.interceptor(pjp);
        }
        return pjp.proceed();
    }
}
