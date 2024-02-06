package io.metersphere.plugin.platform.spi;

import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.plugin.platform.dto.request.PlatformRequest;
import io.metersphere.plugin.platform.dto.request.PluginOptionsRequest;
import io.metersphere.plugin.sdk.util.MSPluginException;
import io.metersphere.plugin.sdk.util.PluginUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractPlatform implements Platform {
    protected PlatformRequest request;

    public AbstractPlatform(PlatformRequest request) {
        this.request = request;
    }

    public <T> T getIntegrationConfig(Class<T> clazz) {
        return getIntegrationConfig(request.getIntegrationConfig(), clazz);
    }

    public <T> T getIntegrationConfig(String integrationConfig, Class<T> clazz) {
        if (StringUtils.isBlank(integrationConfig)) {
            throw new MSPluginException("服务集成配置为空");
        }
        return PluginUtils.parseObject(integrationConfig, clazz);
    }

    public String getPluginId() {
        return request.getPluginId();
    }


    @Override
    public List<SelectOption> getPluginOptions(PluginOptionsRequest request)  {
        try {
            // 这里反射调用 getIssueTypes 等方法，获取下拉框选项
            Method method = this.getClass().getMethod(request.getOptionMethod(), request.getClass());
            @SuppressWarnings("unchecked")
            List<SelectOption> result = (List<SelectOption>) method.invoke(this, request);
            return result;
        } catch (InvocationTargetException e) {
            throw new MSPluginException(e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new MSPluginException(e);
        }
    }
}
