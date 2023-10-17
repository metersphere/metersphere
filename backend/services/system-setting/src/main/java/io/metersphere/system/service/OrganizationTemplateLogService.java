package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.request.TemplateUpdateRequest;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Template;
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
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION_TEMPLATE,
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO setDefaultTemplateLog(TemplateUpdateRequest request) {
        Template template = organizationTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    OperationLogModule.SETTING_SYSTEM_ORGANIZATION_TEMPLATE,
                    String.join(Translator.get("set_default_template"), ":", template.getName()));
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
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
                    OperationLogModule.SETTING_SYSTEM_ORGANIZATION_TEMPLATE,
                    template.getName());
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
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION_TEMPLATE,
                template.getName());
        dto.setOriginalValue(JSON.toJSONBytes(template));
        return dto;
    }
}