package io.metersphere.api.jmeter;

import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.commons.utils.CommonBeanFactory;

import java.util.List;

/**
 * 执行结束后的处理(单例类）
 *
 * @author song.tianyang
 * @Date 2021/10/29 11:22 上午
 */
public class ExecutedHandleSingleton {
    private static volatile ApiEnvironmentRunningParamService apiEnvironmentRunningParamService = CommonBeanFactory.getBean(ApiEnvironmentRunningParamService.class);
    private ExecutedHandleSingleton() {
    }

    public  static void parseEnvironment(List<String> evnStrList) {
        for (String evnStr: evnStrList) {
            apiEnvironmentRunningParamService.parseEvn(evnStr);
        }
    }
}
