package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.ResourceLogInsertModule;
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
import org.springframework.validation.annotation.Validated;

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

    public void saveAddLog(TestPlan module, @Validated ResourceLogInsertModule logInsertModule) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(logModule)
                .method(logInsertModule.getRequestMethod())
                .path(logInsertModule.getRequestUrl())
                .sourceId(module.getId())
                .content(generateContent(module.getName(), logInsertModule.getResourceType(), "add"))
                .createUser(logInsertModule.getOperator())
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveDeleteLog(TestPlan module, @Validated ResourceLogInsertModule logInsertModule) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(logModule)
                .method(logInsertModule.getRequestMethod())
                .path(logInsertModule.getRequestUrl())
                .sourceId(module.getId())
                .content(generateContent(module.getName(), logInsertModule.getResourceType(), "remove"))
                .createUser(logInsertModule.getOperator())
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveSortLog(TestPlan testPlan, String resourceId, @Validated ResourceLogInsertModule logInsertModule) {
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(testPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(logInsertModule.getRequestMethod())
                .path(logInsertModule.getRequestUrl())
                .sourceId(resourceId)
                .content(generateContent(testPlan.getName(), logInsertModule.getResourceType(), "move"))
                .createUser(logInsertModule.getOperator())
                .build().getLogDTO();
        operationLogService.add(dto);
    }


    public void saveAssociateLog(TestPlan module, @Validated ResourceLogInsertModule logInsertModule) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ASSOCIATE.name())
                .module(logModule)
                .method(logInsertModule.getRequestMethod())
                .path(logInsertModule.getRequestUrl())
                .sourceId(module.getId())
                .content(generateContent(module.getName(), logInsertModule.getResourceType(), "associate"))
                .createUser(logInsertModule.getOperator())
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
