package io.metersphere.api.engine;

import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineFactory {
    private static Class<? extends ApiEngine> apiEngine = null;

    static {
        Set<Class<? extends ApiEngine>> subTypes = new Reflections("io.metersphere.xpack.engine.api").getSubTypesOf(ApiEngine.class);
        if (CollectionUtils.isNotEmpty(subTypes)) {
            apiEngine = subTypes.stream().findFirst().get();
        }
    }


    public static ApiEngine createApiEngine(TaskRequestDTO request)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        LogUtils.info("创建K8s client");
        return ConstructorUtils.invokeConstructor(apiEngine, request);
    }
}
