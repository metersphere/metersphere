package io.metersphere.plugin.platform.spi;

import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.plugin.platform.dto.SyncBugResult;
import io.metersphere.plugin.platform.dto.reponse.DemandRelatePageResponse;
import io.metersphere.plugin.platform.dto.reponse.PlatformBugUpdateDTO;
import io.metersphere.plugin.platform.dto.reponse.PlatformCustomFieldItemDTO;
import io.metersphere.plugin.platform.dto.request.*;
import io.metersphere.plugin.platform.utils.PluginPager;
import org.pf4j.ExtensionPoint;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

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
     * 校验用户配置
     * 个人中心-第三方平台点击时调用
     */
    void validateUserConfig(String userConfig);

    /**
     * 校验项目配置
     * 项目设置成点击校验项目 key 时调用
     * @param projectConfig 项目配置
     */
    void validateProjectConfig(String projectConfig);

    /**
     * 平台是否支持第三方默认模板
     *
     * @return True时会在MS平台展示第三方的默认模板
     */
    boolean isSupportDefaultTemplate();

    /**
     * 获取第三方平台模板的自定义字段(isSupportDefaultTemplate为true时才会调用)
     *
     * @param projectConfig  项目配置信息
     * @return 平台自定义字段集合
     */
    List<PlatformCustomFieldItemDTO> getDefaultTemplateCustomField(String projectConfig);

    /**
     * 获取第三方联级下拉options
     * @param optionsRequest 插件请求参数
     * @return 选项集合
     */
    List<SelectOption> getPluginOptions(PluginOptionsRequest optionsRequest);

    /**
     * 获取第三方平台表单下拉选项
     * @param optionsRequest 选项请求参数
     * @return 选项集合
     */
    List<SelectOption> getFormOptions(GetOptionRequest optionsRequest);

    /**
     * 获取第三方平台缺陷状态选项
     * @param projectConfig 项目配置信息
     * @param issueKey 缺陷ID
     * @return 缺陷平台状态
     */
    List<SelectOption> getStatusTransitions(String projectConfig, String issueKey);

    /**
     * 获取第三方平台关联需求列表
     * @param request 需求分页查询参数
     * @return 需求分页数据
     */
    PluginPager<DemandRelatePageResponse> pageDemand(DemandPageRequest request);

    /**
     * 新增平台缺陷
     *
     * @param request 平台缺陷参数
     * @return 平台缺陷
     */
    PlatformBugUpdateDTO addBug(PlatformBugUpdateRequest request);

    /**
     * 修改平台缺陷
     *
     * @param request 平台缺陷参数
     * @return 平台缺陷
     */
    PlatformBugUpdateDTO updateBug(PlatformBugUpdateRequest request);

    /**
     * 删除平台缺陷
     *
     * @param platformBugId 平台缺陷ID
     */
    void deleteBug(String platformBugId);

    /**
     * 平台是否支持附件API
     *
     * @return 是否支持附件同步
     */
    boolean isSupportAttachment();

    /**
     * 同步MS附件至第三方平台(isSupportAttachment为true时执行同步附件的逻辑)
     *
     * @param request 同步附件参数
     */
    void syncAttachmentToPlatform(SyncAttachmentToPlatformRequest request);

    /**
     * 同步存量缺陷
     *
     * @param request 同步缺陷参数
     * @return 同步缺陷结果
     */
    SyncBugResult syncBugs(SyncBugRequest request);

    /**
     * 同步全量缺陷
     *
     * @param request 同步缺陷参数
     */
    void syncAllBugs(SyncAllBugRequest request);

    /**
     * 获取附件输入流，并做相应处理
     * 同步缺陷中，同步附件时会调用
     * @param fileKey 文件关键字
     * @param inputStreamHandler 获取响应的输入流后，做对应处理
     */
    void getAttachmentContent(String fileKey, Consumer<InputStream> inputStreamHandler);
}
