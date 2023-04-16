package io.metersphere.plugin.platform.api;

import io.metersphere.plugin.platform.dto.*;

import java.util.List;

/**
 * 平台对接相关业务
 * @author jianxing.chen
 */
public interface Platform {

    /**
     * 获取平台相关需求
     * 功能用例关联需求时调用
     * @param projectConfig 项目设置表单值
     * @return 需求列表
     */
    List<DemandDTO> getDemands(String projectConfig);

    /**
     * 创建缺陷并封装 MS 返回
     * 创建缺陷时调用
     * @param issuesRequest issueRequest
     * @return MS 缺陷
     */
    MsIssueDTO addIssue(PlatformIssuesUpdateRequest issuesRequest);

    /**
     * 项目设置中选项，从平台获取下拉框选项
     * frontend.json 中选项值配置了 optionMethod ，项目设置时调用
     * @return 返回下拉列表
     *  该接口后续版本将废弃，替换为 getFormOptions
     */
    @Deprecated
    List<SelectOption> getProjectOptions(GetOptionRequest request);

    /**
     * 项目设置和缺陷表单中，调用接口获取下拉框选项
     * 配置文件的表单中选项值配置了 optionMethod ，则调用获取表单的选项值
     * @return 返回下拉列表
     */
    List<SelectOption> getFormOptions(GetOptionRequest request);

    /**
     * 更新缺陷
     * 编辑缺陷时调用
     * @param request
     * @return MS 缺陷
     */
    MsIssueDTO updateIssue(PlatformIssuesUpdateRequest request);

    /**
     * 删除缺陷平台缺陷
     * 删除缺陷时调用
     * @param id 平台的缺陷 ID
     */
    void deleteIssue(String id);

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
     * 校验用户配置配置
     * 用户信息，校验第三方信息时调用
     */
    void validateUserConfig(String userConfig);

    /**
     * 支持附件上传
     * 编辑缺陷上传附件是会调用判断是否支持附件上传
     * 如果支持会调用 syncIssuesAttachment 上传缺陷到第三方平台
     */
    boolean isAttachmentUploadSupport();

    /**
     * 同步缺陷最新变更
     * 开源用户点击同步缺陷时调用
     */
    SyncIssuesResult syncIssues(SyncIssuesRequest request);

    /**
     * 同步项目下所有的缺陷
     * 企业版用户会调用同步缺陷
     */
    void syncAllIssues(SyncAllIssuesRequest request);

    /**
     * 获取附件内容
     * 同步缺陷中，同步附件时会调用
     * @param fileKey 文件关键字
     */
    byte[] getAttachmentContent(String fileKey);

    /**
     * 获取第三方平台缺陷的自定义字段
     * 需要 PluginMetaInfo 的 isThirdPartTemplateSupport 返回 true
     * @return
     */
    List<PlatformCustomFieldItemDTO> getThirdPartCustomField(String projectConfig);

    /**
     * Get请求的代理
     * 目前使用场景：富文本框中如果有图片是存储在第三方平台，MS 通过 url 访问
     * 这时如果第三方平台需要登入才能访问到静态资源，可以通过将富文本框图片内容构造如下格式访问
     * ![name](/resource/md/get/path?platform=Jira?project_id=&workspace_id=&path=)
     * @param path
     * @return
     */
    Object proxyForGet(String path, Class responseEntityClazz);

    /**
     * 同步 MS 缺陷附件到第三方平台
     * isAttachmentUploadSupport 返回为 true 时，同步和创建缺陷时会调用
     */
    void syncIssuesAttachment(SyncIssuesAttachmentRequest request);

    /**
     * 获取第三方平台的状态列表
     * 缺陷列表和编辑缺陷时会调用
     * @return
     */
    List<PlatformStatusDTO> getStatusList(String projectConfig);

    /**
     * 获取第三方平台的状态转移列表
     * 即编辑缺陷时的可选状态
     * 默认会调用 getStatusList，可重写覆盖
     * @param issueId
     * @return
     */
    List<PlatformStatusDTO> getTransitions(String projectConfig, String issueId);

    /**
     * 用例关联需求时调用
     * 可在第三方平台添加用例和需求的关联关系
     * @param request
     */
    void handleDemandUpdate(DemandUpdateRequest request);

    /**
     * 用例批量关联需求时调用
     * 可在第三方平台添加用例和需求的关联关系
     * @param request
     */
    void handleDemandUpdateBatch(DemandUpdateRequest request);
}
