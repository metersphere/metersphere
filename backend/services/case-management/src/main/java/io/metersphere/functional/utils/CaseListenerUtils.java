package io.metersphere.functional.utils;

import io.metersphere.functional.event.CaseEventSource;
import io.metersphere.functional.listener.CaseEventListener;
import io.metersphere.sdk.util.CommonBeanFactory;

import java.util.Map;

public class CaseListenerUtils {

    public static void addListener(Map<String, Object> param, String message) {
        CaseEventSource caseEventSource = CommonBeanFactory.getBean(CaseEventSource.class);
        CaseEventListener caseEventListener = CommonBeanFactory.getBean(CaseEventListener.class);
        if (caseEventSource != null) {
            caseEventSource.addListener(event -> {
                assert caseEventListener != null;
                caseEventListener.onEvent(event);
            });
            caseEventSource.fireEvent("CASE_MANAGEMENT", message, param);
        }
    }

}
