package io.metersphere.plugin.platform.spi;

import io.metersphere.plugin.platform.dto.PlatformBugUpdateRequest;
import io.metersphere.plugin.platform.dto.PlatformCustomFieldItemDTO;
import io.metersphere.plugin.platform.dto.SyncAttachmentToPlatformRequest;
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
     * @param projectConfig 项目配置
     */
    void validateProjectConfig(String projectConfig);

    /**
     * 平台是否支持第三方模板
     *
     * @return 是否支持第三方模板
     */
    boolean isThirdPartTemplateSupport();

    /**
     * 获取第三方平台缺陷的自定义字段
     * 需要 PluginMetaInfo 的 isThirdPartTemplateSupport 返回 true
     *
     * @param projectConfig  项目配置信息
     * @return 平台自定义字段集合
     */
    List<PlatformCustomFieldItemDTO> getThirdPartCustomField(String projectConfig);

    /**
     * 新增平台缺陷
     *
     * @param request 平台缺陷参数
     * @return 平台缺陷ID
     */
    String addBug(PlatformBugUpdateRequest request);

    /**
     * 修改平台缺陷
     *
     * @param request 平台缺陷参数
     */
    void updateBug(PlatformBugUpdateRequest request);

    /**
     * 删除平台缺陷
     *
     * @param platformBugId 平台缺陷ID
     */
    void deleteBug(String platformBugId);

    /**
     * 平台是否支持附件同步
     *
     * @return 是否支持附件同步
     */
    boolean isAttachmentUploadSupport();

    /**
     * 同步MS附件至第三方平台(isAttachmentUploadSupport返回true时执行同步附件的逻辑)
     *
     * @param request 同步附件参数
     */
    void syncAttachmentToPlatform(SyncAttachmentToPlatformRequest request);

    /**
     * 同步部分缺陷
     */
    void syncPartIssues();

    /**
     * 同步全量缺陷
     */
    void syncAllIssues();
}
