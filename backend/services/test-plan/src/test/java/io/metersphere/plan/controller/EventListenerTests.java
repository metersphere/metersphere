package io.metersphere.plan.controller;

import io.metersphere.plan.listener.ExecEventListener;
import io.metersphere.sdk.listener.Event;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class EventListenerTests {

    @Test
    @Order(0)
    public void eventSourceTest() throws Exception {
        new ExecEventListener().onEvent(new Event("TEST", "Event after removing the listener test."));
    }
}
