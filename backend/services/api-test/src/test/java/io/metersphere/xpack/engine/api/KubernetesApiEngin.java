package io.metersphere.xpack.engine.api;

import io.metersphere.api.engine.ApiEngine;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.util.LogUtils;

public class KubernetesApiEngin implements ApiEngine {

    // 初始化API调用
    public KubernetesApiEngin(TaskRequest request) {
        LogUtils.info("init k8s client");
    }


    @Override
    public void start() {
        LogUtils.info("k8s执行START");
    }

    @Override
    public void stop() {
        LogUtils.info("K8S执行STOP");
    }
}
