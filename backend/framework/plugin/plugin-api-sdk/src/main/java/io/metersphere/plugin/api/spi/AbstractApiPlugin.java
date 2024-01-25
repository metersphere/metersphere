package io.metersphere.plugin.api.spi;

import io.metersphere.plugin.api.dto.ApiPluginOptionsRequest;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.plugin.sdk.spi.AbstractMsPlugin;
import io.metersphere.plugin.sdk.util.MSPluginException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 接口插件抽象类
 */
public abstract class AbstractApiPlugin extends AbstractMsPlugin {

    public List<ApiPluginSelectOption> getPluginOptions(ApiPluginOptionsRequest request)  {
        String method = request.getOptionMethod();
        try {
            // 这里反射调用插件方法，获取下拉框选项
            return (List<ApiPluginSelectOption>) this.getClass().getMethod(method, request.getClass()).invoke(this, request);
        } catch (InvocationTargetException e) {
            throw new MSPluginException(e.getTargetException());
        }  catch (Exception e) {
            throw new MSPluginException(e);
        }
    }
}
