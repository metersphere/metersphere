package io.metersphere.system.service;

import com.alibaba.excel.util.BooleanUtils;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Template;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationTemplateLogService {

    @Resource
    private OrganizationTemplateService organizationTemplateService;

    public LogDTO addLog(TemplateUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                getOperationLogModule(request.getScene()),
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        switch (templateScene) {
            case API:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_TEMPLATE;
            case BUG:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_TEMPLATE;
            default:
                return null;
        }
    }


    public LogDTO disableOrganizationTemplateLog(String organizationId, String scene) {
        return new LogDTO(
                OperationLogConstants.ORGANIZATION,
                organizationId,
                scene,
                null,
                OperationLogType.UPDATE.name(),
                getDisableOrganizationTemplateModule(scene),
                Translator.get("project_template_enable"));
    }

    /**
     * 获取启用项目模板的操作对象
     * @param scene
     * @return
     */
    public String getDisableOrganizationTemplateModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        switch (templateScene) {
            case API:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API;
            case FUNCTIONAL:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL;
            case UI:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI;
            case BUG:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG;
            case TEST_PLAN:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN;
            default:
                return null;
        }
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = organizationTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    BooleanUtils.isTrue(template.getInternal()) ? Translator.get("template.default") : template.getName());
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Template template = organizationTemplateService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                template.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(template.getScene()),
                template.getName());
        dto.setOriginalValue(JSON.toJSONBytes(template));
        return dto;
    }
}