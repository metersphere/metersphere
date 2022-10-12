package io.metersphere.api.exec.engine;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.engine.Engine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineFactory {
    public static Engine createApiEngine(JmeterRunRequestDTO runRequest) {
        LogUtil.info("创建K8s client");
        return new KubernetesTestEngine(runRequest);
    }
}
