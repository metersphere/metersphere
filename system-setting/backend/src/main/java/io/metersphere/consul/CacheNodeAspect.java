package io.metersphere.consul;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.MicroService;
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
    private MicroService microService;
    /**
     * 定义切点 @Pointcut 在注解的位置切入代码
     */
    @Pointcut("@annotation(io.metersphere.consul.CacheNode)")
    public void cacheNodes() {
    }

    @After("cacheNodes()")
    @Async
    public void after() {
        microService.getForResultHolder(MicroServiceName.PERFORMANCE_TEST, "/performance/update/cache");
    }

}
