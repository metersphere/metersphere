package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.request.TestPlanCopyRequest;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.util.JSON;
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

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLogService {

    private String logModule = OperationLogModule.TEST_PLAN;

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private TestPlanMapper testPlanMapper;

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
                .content(generateTestPlanSimpleContent(module))
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

    public void saveDeleteLog(TestPlan deleteTestPlan, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(deleteTestPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(deleteTestPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(logModule)
                .method(requestMethod)
                .path(requestUrl)
                .sourceId(deleteTestPlan.getId())
                .content(this.generateTestPlanDeleteContent(deleteTestPlan))
                .originalValue(JSON.toJSONBytes(deleteTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveBatchDeleteLog(List<TestPlan> testPlanList, String operator, String requestUrl, String requestMethod) {
        Project project = projectMapper.selectByPrimaryKey(testPlanList.get(0).getProjectId());
        List<LogDTO> list = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(testPlan.getProjectId())
                    .organizationId(project.getOrganizationId())
                    .type(OperationLogType.DELETE.name())
                    .module(logModule)
                    .method(requestMethod)
                    .path(requestUrl)
                    .sourceId(testPlan.getId())
                    .content(this.generateTestPlanDeleteContent(testPlan))
                    .originalValue(JSON.toJSONBytes(testPlan))
                    .createUser(operator)
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    private String generateTestPlanSimpleContent(TestPlan testPlan) {
        StringBuilder content = new StringBuilder();
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            content.append(Translator.get("test_plan.test_plan_group")).append(StringUtils.SPACE).append(testPlan.getName()).append(StringUtils.SPACE);
        } else {
            content.append(Translator.get("test_plan.test_plan")).append(StringUtils.SPACE).append(testPlan.getName()).append(StringUtils.SPACE);
        }
        return content.toString();
    }

    private String generateTestPlanDeleteContent(TestPlan deleteTestPlan) {
        StringBuilder content = new StringBuilder();
        if (StringUtils.equals(deleteTestPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            content.append(Translator.get("log.delete.test_plan_group")).append(":").append(deleteTestPlan.getName()).append(StringUtils.SPACE);
        } else {
            content.append(Translator.get("log.delete.test_plan")).append(":").append(deleteTestPlan.getName()).append(StringUtils.SPACE);
        }
        return content.toString();
    }

    /**
     * 归档日志
     *
     * @param id
     * @return
     */
    public LogDTO archivedLog(String id) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        LogDTO dto = new LogDTO(
                testPlan.getProjectId(),
                null,
                testPlan.getId(),
                null,
                OperationLogType.ARCHIVED.name(),
                logModule,
                testPlan.getName());
        dto.setPath("/test-plan/archived");
        dto.setMethod(HttpMethodConstants.GET.name());
        dto.setOriginalValue(JSON.toJSONBytes(testPlan));
        return dto;
    }

    /**
     * 复制日志
     *
     * @param request
     * @return
     */
    public LogDTO copyLog(TestPlanCopyRequest request) {
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                null,
                null,
                OperationLogType.COPY.name(),
                logModule,
                request.getName());
        dto.setPath("/test-plan/copy");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }


}
