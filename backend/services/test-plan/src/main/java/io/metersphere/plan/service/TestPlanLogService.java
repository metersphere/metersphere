package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.request.TestPlanBatchEditRequest;
import io.metersphere.plan.dto.request.TestPlanCopyRequest;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public LogDTO scheduleLog(String id) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.TEST_PLAN)
                .sourceId(testPlan.getId())
                .content(Translator.get("test_plan_schedule") + ":" + testPlan.getName())
                .build().getLogDTO();
        return dto;
    }

    /**
     * 新增计划日志
     *
     * @param module        模块
     * @param operator      操作人
     * @param requestUrl    请求路径
     * @param requestMethod 请求方法
     */
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

    /**
     * 修改计划日志
     *
     * @param oldTestPlan   旧计划
     * @param newTestPlan   新计划
     * @param projectId     项目ID
     * @param operator      操作人
     * @param requestUrl    请求URL
     * @param requestMethod 请求方法
     */
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
                .content(generateTestPlanSimpleContent(newTestPlan, Translator.get("update")))
                .originalValue(JSON.toJSONBytes(oldTestPlan))
                .modifiedValue(JSON.toJSONBytes(newTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    /**
     * 删除计划日志
     *
     * @param deleteTestPlan 删除计划
     * @param operator       操作人
     * @param requestUrl     请求URL
     * @param requestMethod  请求方法
     */
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
                .content(generateTestPlanSimpleContent(deleteTestPlan, Translator.get("delete")))
                .originalValue(JSON.toJSONBytes(deleteTestPlan))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    /**
     * 归档日志
     *
     * @param id ID
     * @return 日志对象
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
     * @param request 请求参数
     * @return 日志对象
     */
    public LogDTO copyLog(TestPlanCopyRequest request) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getId());
        testPlan.setName(request.getName());
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                null,
                null,
                null,
                OperationLogType.COPY.name(),
                logModule,
                generateTestPlanSimpleContent(testPlan, Translator.get("copy")));
        dto.setPath("/test-plan/copy");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    /**
     * 保存批量日志
     *
     * @param plans         计划
     * @param operator      操作人
     * @param requestUrl    请求URL
     * @param requestMethod 请求方法
     * @param requestType   请求类型
     * @param typeKey       类型Key
     */
    public void saveBatchLog(List<TestPlan> plans, String operator, String requestUrl, String requestMethod, String requestType, String typeKey) {
        Project project = projectMapper.selectByPrimaryKey(plans.get(0).getProjectId());
        List<LogDTO> list = new ArrayList<>();
        for (TestPlan plan : plans) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(plan.getProjectId())
                    .organizationId(project.getOrganizationId())
                    .type(requestType)
                    .module(logModule)
                    .method(requestMethod)
                    .path(requestUrl)
                    .sourceId(plan.getId())
                    .content(generateTestPlanSimpleContent(plan, Translator.get(typeKey)))
                    .originalValue(JSON.toJSONBytes(plan))
                    .createUser(operator)
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    /**
     * 生成计划操作日志内容
     *
     * @param testPlan 测试计划
     * @param type     类型
     * @return 日志内容
     */
    private String generateTestPlanSimpleContent(TestPlan testPlan, String type) {
        StringBuilder content = new StringBuilder();
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            content.append(Translator.getWithArgs("test_plan_group.batch.log", type)).append(StringUtils.SPACE).append(testPlan.getName()).append(StringUtils.SPACE);
        } else {
            content.append(Translator.getWithArgs("test_plan.batch.log", type)).append(StringUtils.SPACE).append(testPlan.getName()).append(StringUtils.SPACE);
        }
        return content.toString();
    }


    /**
     * 批量编辑
     *
     * @param request
     * @return
     */
    public List<LogDTO> batchEditLog(TestPlanBatchEditRequest request) {
        // 测试计划不支持全选所有页
        List<String> ids = request.getSelectIds();
        List<LogDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andIdIn(ids);
            List<TestPlan> testPlans = testPlanMapper.selectByExample(example);
            Map<String, TestPlan> collect = testPlans.stream().collect(Collectors.toMap(TestPlan::getId, testPlan -> testPlan));
            ids.forEach(id -> {
                TestPlan testPlan = collect.get(id);
                LogDTO dto = new LogDTO(
                        testPlan.getProjectId(),
                        null,
                        testPlan.getId(),
                        null,
                        OperationLogType.UPDATE.name(),
                        OperationLogModule.TEST_PLAN,
                        testPlan.getName());
                dto.setPath("/test-plan/batch-edit");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(testPlan));
                dtoList.add(dto);
            });
        }
        return dtoList;
    }

    public void saveMoveLog(TestPlan testPlan, String moveId, LogInsertModule logInsertModule) {

        Project project = projectMapper.selectByPrimaryKey(testPlan.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(testPlan.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(logModule)
                .method(logInsertModule.getRequestMethod())
                .path(logInsertModule.getRequestUrl())
                .sourceId(moveId)
                .content(Translator.get("log.test_plan.move.test_plan") + ":" + testPlan.getName() + StringUtils.SPACE)
                .createUser(logInsertModule.getOperator())
                .build().getLogDTO();
        operationLogService.add(dto);
    }

}
