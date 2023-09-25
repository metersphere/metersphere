package io.metersphere.project.service;

import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.request.TemplateUpdateRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Template;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
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
                OperationLogModule.PROJECT_TEMPLATE,
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
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
                    OperationLogModule.PROJECT_TEMPLATE,
                    template.getName());
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO setDefaultTemplateLog(TemplateUpdateRequest request) {
        Template template = projectTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
                    null,
                    template.getId(),
                    null,
                    String.join(Translator.get("set_default_template"), ":", OperationLogType.UPDATE.name()),
                    OperationLogModule.PROJECT_TEMPLATE,
                    template.getName());
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
                OperationLogModule.PROJECT_TEMPLATE,
                template.getName());
        dto.setOriginalValue(JSON.toJSONBytes(template));
        return dto;
    }
}