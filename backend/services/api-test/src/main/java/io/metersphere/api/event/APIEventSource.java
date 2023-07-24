package io.metersphere.api.event;

import io.metersphere.sdk.listener.Event;
import io.metersphere.sdk.listener.EventListener;
import io.metersphere.sdk.listener.EventSource;
import org.springframework.stereotype.Component;

@Component
public class APIEventSource implements EventSource {
    private EventListener<Event> listener;

    @Override
    public void addListener(EventListener<Event> listener) {
        this.listener = listener;
    }

    @Override
    public void fireEvent(String module, String message) {
        Event event = new Event("API", message);
        listener.onEvent(event);
    }
}
