package io.metersphere.ai.engine.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一个管理并提供访问聊天客户端模型的类。
 * 该类扫描所有实现了 {@link AIChatClient} 的 Bean，并使用 {@link AIRegister} 注解中的键
 * 将它们注册到一个映射中，用于根据类型获取特定的聊天模型。
 * <p>
 * A class that manages and provides access to chat client models.
 * This class scans all beans of type {@link AIChatClient} and registers them in a map
 * using the key from the {@link AIRegister} annotation to fetch specific chat models by type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIRegister {
    String value();
}
