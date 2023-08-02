package io.metersphere.sdk.listener;

public interface EventSource {
    /**
     * 注册监听
     */
    void addListener(EventListener<Event> listener);

    /**
     * 触发事件
     */
    void fireEvent(String module, String message);
}
