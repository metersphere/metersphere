package io.metersphere.api.service;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.RunUiScenarioRequest;
import io.metersphere.dto.UiScenarioDTO;
import io.metersphere.dto.UiScenarioRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiAutomationServiceProxy {

    private Object invoke(Function<Class, Method> getDeclaredMethod, Object... args) {
        return CommonBeanFactory.invoke("uiAutomationService", getDeclaredMethod, args);
    }

    public List run(RunScenarioRequest request) {
        return (List) invoke((clazz) -> {
            try {
                return clazz.getDeclaredMethod("run", RunUiScenarioRequest.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            }
        }, request);
    }

    public List<UiScenarioDTO> list(UiScenarioRequest request) {
        return (List<UiScenarioDTO>) invoke((clazz) -> {
            try {
                return clazz.getDeclaredMethod("list", UiScenarioRequest.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            }
        }, request);
    }
}
