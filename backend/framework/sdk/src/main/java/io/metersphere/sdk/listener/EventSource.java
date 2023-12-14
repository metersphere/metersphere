package io.metersphere.sdk.listener;

import java.util.Map;

public interface EventSource {
    /**
     * 注册监听
     */
    void addListener(EventListener<Event> listener);

    /**
     * 触发事件
     */
    void fireEvent(String module, String message);

    /**
     * 触发事件，带有参数
     */
    void fireEvent(String module, String message, Map<String, Object> paramMap);
}
