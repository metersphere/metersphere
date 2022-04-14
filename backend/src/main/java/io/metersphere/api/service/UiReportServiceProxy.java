package io.metersphere.api.service;

import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.RequestResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiReportServiceProxy {

    public Object saveUiResult(String reportId, List<RequestResult> queue) {
       return invoke((clazz) -> {
           try {
               return clazz.getDeclaredMethod("saveUiResult", String.class, List.class);
           } catch (NoSuchMethodException e) {
               e.printStackTrace();
               return null;
           }
       }, reportId, queue);
    }

    private Object invoke(Function<Class, Method> getDeclaredMethod, Object... args) {
        Object uiAutomationService = CommonBeanFactory.getBean("uiReportService");
        try {
            Class<?> clazz = uiAutomationService.getClass();
            Method postProcessUiReport = getDeclaredMethod.apply(clazz);
            return postProcessUiReport.invoke(uiAutomationService, args);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

}
