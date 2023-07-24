package io.metersphere.sdk.listener;

/**
 * 监听所有执行结果
 *
 * @param <T>
 */
public interface EventListener<T> {
    void onEvent(T event);
}