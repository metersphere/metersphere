package io.metersphere.plan.listener;

import io.metersphere.sdk.listener.Event;
import io.metersphere.sdk.listener.EventListener;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.stereotype.Component;

@Component
public class ExecEventListener implements EventListener<Event> {
    @Override
    public void onEvent(Event event) {
        // todo: 测试计划事件处理逻辑
        LogUtils.info("ExecEventListener: " + event.module() + "：" + event.message());
    }
}
