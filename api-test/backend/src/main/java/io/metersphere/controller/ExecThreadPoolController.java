package io.metersphere.controller;

import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;


@RestController
@RequestMapping("/exec/thread/pool")
public class ExecThreadPoolController {

    @GetMapping("/set-core-size/{size}")
    public void setExecThreadPoolCoreSize(@PathVariable int size) {
        Objects.requireNonNull(CommonBeanFactory.getBean(ExecThreadPoolExecutor.class)).setCorePoolSize(size);
    }
}
