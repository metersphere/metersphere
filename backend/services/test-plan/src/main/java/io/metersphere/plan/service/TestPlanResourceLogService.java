package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanResourceLogService {

    private String logModule = OperationLogModule.TEST_PLAN;

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    /**
     * @param testPlanName 测试计划名称   取自TestPlanResourceConstants
     * @param resourceType 资源类型
     * @param operatorType add/remove/move/update
     * @return
     */
    private String generateContent(String testPlanName, String resourceType, String operatorType) {
        StringBuilder content = new StringBuilder();
        content.append(testPlanName + Translator.get("log.test_plan." + operatorType)).append(":").append(Translator.get("log.test_plan." + resourceType)).append(StringUtils.SPACE);
        return content.toString();
    }

    public void saveAddLog(TestPlan module, String resourceType, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(module.getId())
                .content(generateContent(module.getName(), resourceType, "add"))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveDeleteLog(TestPlan module, String resourceType, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(module.getId())
                .content(generateContent(module.getName(), resourceType, "remove"))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveSortLog(TestPlan testPlan, String resourceId, String resourceType, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(testPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(resourceId)
                .content(generateContent(testPlan.getName(), resourceType, "move"))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
