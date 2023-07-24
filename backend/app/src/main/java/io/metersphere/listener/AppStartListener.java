package io.metersphere.listener;

import io.metersphere.api.event.APIEventSource;
import io.metersphere.plan.listener.ExecEventListener;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartListener implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtils.info("================= 应用启动 =================");
        // 注册所有监听源
        LogUtils.info("初始化接口事件源");
        APIEventSource apiEventSource = CommonBeanFactory.getBean(APIEventSource.class);
        LogUtils.info("初始化性能测试事件源");
        //LoadEventSource loadEventSource = CommonBeanFactory.getBean(LoadEventSource.class);
        //todo: 注册其他事件源


        // 创建监听器对象并注册到多个事件源
        ExecEventListener listener = CommonBeanFactory.getBean(ExecEventListener.class);
        apiEventSource.addListener(listener);

        //todo: 注册其他监听器
        //loadEventSource.addListener(listener);

        // 触发事件
        apiEventSource.fireEvent("API", "Event after removing the listener test.");
        //loadEventSource.fireEvent("LOAD","Event after removing the listener.");
    }
}
