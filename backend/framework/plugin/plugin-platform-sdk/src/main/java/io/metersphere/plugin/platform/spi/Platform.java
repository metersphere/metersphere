package io.metersphere.plugin.platform.spi;

import io.metersphere.plugin.platform.dto.PlatformCustomFieldItemDTO;
import io.metersphere.plugin.platform.dto.PluginOptionsRequest;
import io.metersphere.plugin.platform.dto.SelectOption;
import org.pf4j.ExtensionPoint;

import java.util.List;

/**
 * 平台对接相关业务接口
 * @author jianxing.chen
 */
public interface Platform extends ExtensionPoint {

    /**
     * 校验服务集成配置
     * 服务集成点击校验时调用
     */
    void validateIntegrationConfig();

    /**
     * 校验项目配置
     * 项目设置成点击校验项目 key 时调用
     */
    void validateProjectConfig(String projectConfig);

    /**
     * 插件是否支持第三方模板
     * @return
     */
    boolean isThirdPartTemplateSupport();

    /**
     * 获取第三方平台缺陷的自定义字段
     * 需要 PluginMetaInfo 的 isThirdPartTemplateSupport 返回 true
     * @return
     */
    List<PlatformCustomFieldItemDTO> getThirdPartCustomField(String projectConfig);

    /**
     * 获取第三方联级下拉options
     * @param optionsRequest
     * @return
     */
    List<SelectOption> getPluginOptions(PluginOptionsRequest optionsRequest);
}
