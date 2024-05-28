package io.metersphere.project.service;

import com.alibaba.excel.util.BooleanUtils;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Template;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.metersphere.system.log.constants.OperationLogModule;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectTemplateLogService {

    @Resource
    private ProjectTemplateService projectTemplateService;

    public LogDTO addLog(TemplateUpdateRequest request) {
        LogDTO dto = new LogDTO(
                null,
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
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_TEMPLATE;
            case BUG:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_TEMPLATE;
            default:
                return null;
        }
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = projectTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
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

    public LogDTO setDefaultTemplateLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    StringUtils.join(Translator.get("set_default_template"), ":",
                            template.getInternal() ? projectTemplateService.translateInternalTemplate() : template.getName()));
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                null,
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