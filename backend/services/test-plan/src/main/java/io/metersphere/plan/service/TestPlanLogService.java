package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLogService {

    private String logModule = OperationLogModule.TEST_PLAN;

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void saveAddLog(TestPlan module, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveUpdateLog(TestPlan oldTestPlan, TestPlan newTestPlan, String projectId, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(newTestPlan.getId())
                .content(newTestPlan.getName())
                .originalValue(JSON.toJSONBytes(oldTestPlan))
                .modifiedValue(JSON.toJSONBytes(newTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveDeleteLog(TestPlan deleteTestPlan, List<TestPlan> testPlanItemList, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(deleteTestPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(deleteTestPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(deleteTestPlan.getId())
                .content(this.generateTestPlanDeleteContent(deleteTestPlan, testPlanItemList))
                .originalValue(JSON.toJSONBytes(deleteTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    private String generateTestPlanDeleteContent(TestPlan deleteTestPlan, List<TestPlan> testPlanItemList) {
        StringBuilder content = new StringBuilder();
        if(StringUtils.equals(deleteTestPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)){
            content.append(Translator.get("log.delete.test_plan_group")).append(":").append(deleteTestPlan.getName()).append(StringUtils.SPACE);
        }
        if(CollectionUtils.isNotEmpty(testPlanItemList)){
            List<String> testPlanNameList = testPlanItemList.stream().map(TestPlan::getName).toList();
            String testPlanNamesString = StringUtils.join(testPlanNameList,",");
            content.append(Translator.get("log.delete.test_plan")).append(":").append(testPlanNamesString).append(StringUtils.SPACE);
        }
        return content.toString();
    }

}
