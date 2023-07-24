package io.metersphere.api.controller;

import io.metersphere.api.event.APIEventSource;
import io.metersphere.sdk.listener.Event;
import io.metersphere.sdk.listener.EventListener;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class EventSourceTests {

    @Test
    @Order(0)
    public void eventSourceTest() throws Exception {
        // 注册所有监听源
        LogUtils.info("初始化接口事件源");
        APIEventSource apiEventSource = CommonBeanFactory.getBean(APIEventSource.class);
        apiEventSource.addListener(new EventListener<Event>() {
            @Override
            public void onEvent(Event event) {
                LogUtils.info("ExecEventListener: " + event.getModule() + "：" + event.getMessage());
            }
        });
        // 触发事件
        apiEventSource.fireEvent("API", "Event after removing the listener test.");
    }
}
