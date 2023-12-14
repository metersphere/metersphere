package io.metersphere.functional.event;

import io.metersphere.sdk.listener.Event;
import io.metersphere.sdk.listener.EventListener;
import io.metersphere.sdk.listener.EventSource;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CaseEventSource implements EventSource {
    private EventListener<Event> listener;

    @Override
    public void addListener(EventListener<Event> listener) {
        this.listener = listener;
    }

    @Override
    public void fireEvent(String module, String message) {
        Event event = new Event("CASE", message);
        listener.onEvent(event);
    }

    @Override
    public void fireEvent(String module, String message, Map<String, Object> paramMap) {
        Event event = new Event("CASE", message, paramMap);
        listener.onEvent(event);
        LogUtils.info("带有参数的监听事件");
    }
}
