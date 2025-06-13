package io.metersphere.ai.engine.advisor;

import io.metersphere.sdk.util.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 这是一个日志拦截器，用于拦截请求并记录日志。
 * 实现了StreamAdvisor 接口，可以在请求处理流程前后插入自定义逻辑。
 */
public class LoggingStreamAdvisor implements StreamAdvisor {

    /**
     * 处理流式请求并记录日志。
     *
     * @param advisedRequest 请求对象
     * @param chain          拦截器链
     * @return 返回处理后的响应流
     */
    @Override
    public @NotNull Flux<ChatClientResponse> adviseStream(@NotNull ChatClientRequest advisedRequest, StreamAdvisorChain chain) {
        // 记录请求日志
        LogUtils.info("Request: " + advisedRequest);

        // 继续执行下一个拦截器并返回响应
        return chain.nextStream(advisedRequest);
    }

    /**
     * 获取当前拦截器的执行顺序。
     * 返回值越小，表示拦截器优先级越高。
     *
     * @return 执行顺序
     */
    @Override
    public int getOrder() {
        return 0; // 此拦截器的优先级为0
    }

    /**
     * 获取当前拦截器的名称。
     *
     * @return 当前拦截器的类名
     */
    @Override
    public @NotNull String getName() {
        return getClass().getSimpleName();
    }
}
