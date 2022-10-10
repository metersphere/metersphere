package io.metersphere.api.exec.engine;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.engine.Engine;
import org.apache.commons.beanutils.ConstructorUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class EngineFactory {

    public static Class<? extends KubernetesTestEngine> getKubernetesTestEngineClass() {
        Class kubernetesTestEngineClass;
        try {
            // 使用反射工具包这里在特定环境（66）会卡住
            kubernetesTestEngineClass = Class.forName("io.metersphere.xpack.engine.KubernetesTestEngineImpl");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return kubernetesTestEngineClass;
    }

    public static Engine createApiEngine(JmeterRunRequestDTO runRequest) {
        try {
            return ConstructorUtils.invokeConstructor(getKubernetesTestEngineClass(), runRequest);
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e.getMessage());
        }
        return null;
    }

}
