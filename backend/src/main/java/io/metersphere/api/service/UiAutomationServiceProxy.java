package io.metersphere.api.service;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.RunUiScenarioRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.function.Function;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiAutomationServiceProxy {

    public Object run(RunScenarioRequest request) {
        return invoke((clazz) -> {
            try {
                return clazz.getDeclaredMethod("run", RunUiScenarioRequest.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            }
        }, request);
    }

    private Object invoke(Function<Class, Method> getDeclaredMethod, Object... args) {
        return CommonBeanFactory.invoke("uiAutomationService", getDeclaredMethod, args);
    }

}
