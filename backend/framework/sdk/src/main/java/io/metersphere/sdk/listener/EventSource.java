package io.metersphere.sdk.listener;

public interface EventSource {
    /**
     * @param listener
     */
    void addListener(EventListener<Event> listener);

    /**
     * @param module
     * @param message
     */
    void fireEvent(String module, String message);

}
