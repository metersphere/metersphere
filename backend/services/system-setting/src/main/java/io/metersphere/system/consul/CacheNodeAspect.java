package io.metersphere.system.consul;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheNodeAspect {
    /**
     * 定义切点 @Pointcut 在注解的位置切入代码
     */
    @Pointcut("@annotation(io.metersphere.system.consul.CacheNode)")
    public void cacheNodes() {
    }

    @After("cacheNodes()")
    @Async
    public void after() {
       // microService.getForResultHolder(MicroServiceName.PERFORMANCE_TEST, "/performance/update/cache");
    }

}
