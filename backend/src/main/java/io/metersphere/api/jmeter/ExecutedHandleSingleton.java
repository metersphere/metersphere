package io.metersphere.api.jmeter;

import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行结束后的处理(单例类）
 *
 * @author song.tianyang
 * @Date 2021/10/29 11:22 上午
 */
public class ExecutedHandleSingleton {
    private static volatile ApiEnvironmentRunningParamService apiEnvironmentRunningParamService = CommonBeanFactory.getBean(ApiEnvironmentRunningParamService.class);
    static  Logger testPlanLog = LoggerFactory.getLogger("testPlanExecuteLog");
    private ExecutedHandleSingleton() {
    }

    public synchronized static void parseEnvironment(String evnStr) {
        try {
         Thread.sleep(1000);
        }catch (Exception e){
        }
        apiEnvironmentRunningParamService.parseEvn(evnStr);
    }
}
