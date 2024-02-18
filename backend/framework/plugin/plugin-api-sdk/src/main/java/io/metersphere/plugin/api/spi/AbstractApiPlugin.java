package io.metersphere.plugin.api.spi;

import io.metersphere.plugin.api.dto.ApiPluginOptionsRequest;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.plugin.sdk.spi.AbstractMsPlugin;
import io.metersphere.plugin.sdk.util.MSPluginException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 接口插件抽象类
 */
public abstract class AbstractApiPlugin extends AbstractMsPlugin {

    public boolean isXpack() {
        return false;
    }

    /**
     * 获取插件选项
     *
     * @param request 请求参数
     * @return 选项列表
     */
    public List<ApiPluginSelectOption> getPluginOptions(ApiPluginOptionsRequest request) {
        try {
            // 这里反射调用插件方法，获取下拉框选项
            Method method = this.getClass().getMethod(request.getOptionMethod(), request.getClass());
            @SuppressWarnings("unchecked")
            List<ApiPluginSelectOption> result = (List<ApiPluginSelectOption>) method.invoke(this, request);
            return result;
        } catch (InvocationTargetException e) {
            throw new MSPluginException(e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new MSPluginException(e);
        }
    }
}
