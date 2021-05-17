package io.metersphere.api.jmeter;

import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.commons.utils.CommonBeanFactory;

/**
 * @author song.tianyang
 *  2021/5/13 5:24 下午
 */
public class RunningParam {
    private static ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;

    public static void setParam(String enviromentId, String key, String value){
        checkService();
        apiEnvironmentRunningParamService.addParam(enviromentId,key,value);
    }

    public static void deleteParam(String enviromentId, String key){
    }

    public static void checkService(){
        if(apiEnvironmentRunningParamService == null){
            apiEnvironmentRunningParamService = CommonBeanFactory.getBean(ApiEnvironmentRunningParamService.class);
        }
    }
}
