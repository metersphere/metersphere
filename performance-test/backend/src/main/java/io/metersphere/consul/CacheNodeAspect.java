package io.metersphere.consul;

import io.metersphere.commons.utils.LogUtil;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component
public class CacheNodeAspect {
    @Resource
    private ConsulService consulService;

    /**
     * 定义切点 @Pointcut 在注解的位置切入代码
     */
    @Pointcut("@annotation(io.metersphere.consul.CacheNode)")
    public void cacheNodes() {
    }

    @After("cacheNodes()")
    @Async
    public void after() {
        try {
            consulService.updateCache();
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

}
