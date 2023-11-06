package io.metersphere.api.engine;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.util.LogUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineFactory {
    public static ApiEngine createApiEngine(TaskRequest request) {
        LogUtils.info("创建K8s client");
        return new KubernetesApiEngine(request);
    }
}
