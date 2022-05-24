package io.metersphere.api.service;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.function.Function;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiScenarioSerialServiceProxy {

    public Object serial(ApiExecutionQueue executionQueue, ApiExecutionQueueDetail queue) {
       return invoke((clazz) -> {
           try {
               return clazz.getDeclaredMethod("serial", ApiExecutionQueue.class, ApiExecutionQueueDetail.class);
           } catch (NoSuchMethodException e) {
               e.printStackTrace();
               return null;
           }
       }, executionQueue, queue);
    }

    private Object invoke(Function<Class, Method> getDeclaredMethod, Object... args) {
        return CommonBeanFactory.invoke("uiScenarioSerialService", getDeclaredMethod, args);
    }

}
