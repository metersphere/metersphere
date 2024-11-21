package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.report.ApiScenarioReportListDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDetailDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportStepDTO;
import io.metersphere.api.dto.scenario.ExecTaskDetailDTO;
import io.metersphere.api.mapper.*;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.taskhub.ExecTaskItemDetailDTO;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import io.metersphere.system.mapper.ExtExecTaskMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;
    @Resource
    private ApiScenarioReportLogService apiScenarioReportLogService;
    @Resource
    private ExtApiScenarioReportDetailBlobMapper extApiScenarioReportDetailBlobMapper;
    @Resource
    private ApiScenarioReportLogMapper apiScenarioReportLogMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiScenarioReportNoticeService apiScenarioReportNoticeService;
    private static final String SPLITTER = "_";
    private static final int MAX = 50000;
    private static final int BATCH_SIZE = 1000;
    @Resource
    private ExtExecTaskMapper extExecTaskMapper;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ApiScenarioRecordMapper apiScenarioRecordMapper;

    public void insertApiScenarioReport(ApiScenarioReport report) {
        apiScenarioReportMapper.insertSelective(report);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiScenarioReport(ApiScenarioReport report, ApiReportRelateTask taskRelation) {
        apiScenarioReportMapper.insertSelective(report);
        apiReportRelateTaskMapper.insertSelective(taskRelation);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiScenarioReport(List<ApiScenarioReport> reports, List<ApiScenarioRecord> records) {
        this.insertApiScenarioReport(reports, records, null);
    }

    public void insertApiScenarioReportDetail(ApiScenarioRecord record, ApiReportRelateTask taskRelation) {
        apiScenarioRecordMapper.insertSelective(record);
        apiReportRelateTaskMapper.insertSelective(taskRelation);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiScenarioReport(List<ApiScenarioReport> reports, List<ApiScenarioRecord> records, List<ApiReportRelateTask> taskRelations) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        if (CollectionUtils.isNotEmpty(reports)) {
            ApiScenarioReportMapper reportMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
            SubListUtils.dealForSubList(reports, 1000, subList -> {
                subList.forEach(reportMapper::insertSelective);
            });
        }
        if (CollectionUtils.isNotEmpty(records)) {
            ApiScenarioRecordMapper detailMapper = sqlSession.getMapper(ApiScenarioRecordMapper.class);
            SubListUtils.dealForSubList(records, 1000, subList -> {
                subList.forEach(detailMapper::insertSelective);
            });
        }
        if (CollectionUtils.isNotEmpty(taskRelations)) {
            ApiReportRelateTaskMapper taskRelationMapper = sqlSession.getMapper(ApiReportRelateTaskMapper.class);
            SubListUtils.dealForSubList(taskRelations, 1000, subList -> {
                subList.forEach(taskRelationMapper::insertSelective);
            });
        }
        sqlSession.flushStatements();
        if (sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiScenarioReportStep(List<ApiScenarioReportStep> reportSteps) {
        if (CollectionUtils.isNotEmpty(reportSteps)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiScenarioReportStepMapper stepMapper = sqlSession.getMapper(ApiScenarioReportStepMapper.class);
            SubListUtils.dealForSubList(reportSteps, 1000, subList -> {
                subList.forEach(stepMapper::insertSelective);
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public List<ApiScenarioReportListDTO> getPage(ApiReportPageRequest request) {
        List<ApiScenarioReport> list = extApiScenarioReportMapper.list(request);
        List<ApiScenarioReportListDTO> result = new ArrayList<>();
        //取所有的userid
        Set<String> userSet = list.stream()
                .flatMap(scenarioReport -> Stream.of(scenarioReport.getUpdateUser(), scenarioReport.getDeleteUser(), scenarioReport.getCreateUser()))
                .collect(Collectors.toSet());
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
        list.forEach(scenarioReport -> {
            ApiScenarioReportListDTO scenarioReportListDTO = new ApiScenarioReportListDTO();
            BeanUtils.copyBean(scenarioReportListDTO, scenarioReport);
            scenarioReportListDTO.setCreateUserName(userMap.get(scenarioReport.getCreateUser()));
            scenarioReportListDTO.setUpdateUserName(userMap.get(scenarioReport.getUpdateUser()));
            result.add(scenarioReportListDTO);
        });
        return result;
    }

    public void rename(String id, String name, String userId) {
        ApiScenarioReport apiScenarioReport = checkResource(id);
        apiScenarioReport.setName(name);
        apiScenarioReport.setUpdateTime(System.currentTimeMillis());
        apiScenarioReport.setUpdateUser(userId);
        apiScenarioReportMapper.updateByPrimaryKeySelective(apiScenarioReport);
    }

    public void delete(String id, String userId) {
        ApiScenarioReport scenarioReport = checkResource(id);
        scenarioReport.setDeleted(true);
        scenarioReport.setDeleteTime(System.currentTimeMillis());
        scenarioReport.setDeleteUser(userId);
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport);
    }

    private ApiScenarioReport checkResource(String id) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andIdEqualTo(id);
        List<ApiScenarioReport> scenarioReport = apiScenarioReportMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(scenarioReport)) {
            throw new RuntimeException(Translator.get("api_scenario_report_not_exist"));
        }
        return scenarioReport.getFirst();
    }

    public void batchDelete(ApiReportBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        User user = userMapper.selectByPrimaryKey(userId);
        SubListUtils.dealForSubList(ids, 500, subList -> {
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(subList);
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setDeleted(true);
            scenarioReport.setDeleteTime(System.currentTimeMillis());
            scenarioReport.setDeleteUser(userId);
            apiScenarioReportMapper.updateByExampleSelective(scenarioReport, example);
            //TODO 记录日志
            apiScenarioReportLogService.batchDeleteLog(subList, userId, request.getProjectId());
            apiScenarioReportNoticeService.batchSendNotice(subList, user, request.getProjectId(), NoticeConstants.Event.DELETE);
        });
    }

    public List<String> doSelectIds(ApiReportBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extApiScenarioReportMapper.getIds(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            return request.getSelectIds();
        }
    }

    public ApiScenarioReportDTO get(String id) {
        ApiScenarioReportDTO scenarioReportDTO = new ApiScenarioReportDTO();
        ApiScenarioReport scenarioReport = checkResource(id);
        BeanUtils.copyBean(scenarioReportDTO, scenarioReport);
        //需要查询出所有的步骤
        List<ApiScenarioReportStepDTO> scenarioReportSteps = extApiScenarioReportMapper.selectStepByReportId(id);

        List<ApiScenarioReportStepDTO> deatilList = extApiScenarioReportMapper.selectStepDetailByReportId(id);
        //根据stepId进行分组
        Map<String, List<ApiScenarioReportStepDTO>> detailMap = deatilList.stream().collect(Collectors.groupingBy(ApiScenarioReportStepDTO::getStepId));
        //只处理请求的
        detailRequest(scenarioReportSteps, detailMap);

        //将scenarioReportSteps按照parentId进行分组 值为list 然后根据sort进行排序
        Map<String, List<ApiScenarioReportStepDTO>> scenarioReportStepMap = scenarioReportSteps.stream().collect(Collectors.groupingBy(ApiScenarioReportStepDTO::getParentId));

        List<ApiScenarioReportStepDTO> steps = Optional.ofNullable(scenarioReportStepMap.get("NONE")).orElse(new ArrayList<>(0));
        steps.sort(Comparator.comparingLong(ApiScenarioReportStepDTO::getSort));

        getStepTree(steps, scenarioReportStepMap);

        scenarioReportDTO.setStepTotal(steps.size());
        scenarioReportDTO.setRequestTotal(getRequestTotal(scenarioReportDTO));
        scenarioReportDTO.setChildren(steps);

        scenarioReportDTO.setStepErrorCount(steps.stream().filter(step -> StringUtils.equals(ResultStatus.ERROR.name(), step.getStatus())).count());
        scenarioReportDTO.setStepSuccessCount(steps.stream().filter(step -> StringUtils.equals(ResultStatus.SUCCESS.name(), step.getStatus())).count());
        scenarioReportDTO.setStepPendingCount(steps.stream().filter(step -> StringUtils.equals(ExecStatus.PENDING.name(), step.getStatus()) || StringUtils.isBlank(step.getStatus())).count());
        scenarioReportDTO.setStepFakeErrorCount(steps.stream().filter(step -> StringUtils.equals(ResultStatus.FAKE_ERROR.name(), step.getStatus())).count());
        //控制台信息 console
        ApiScenarioReportLogExample example = new ApiScenarioReportLogExample();
        example.createCriteria().andReportIdEqualTo(id);
        List<ApiScenarioReportLog> apiScenarioReportLogs = apiScenarioReportLogMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(apiScenarioReportLogs)) {
            //获取所有的console,生成集合
            List<String> consoleList = apiScenarioReportLogs.stream().map(c -> new String(c.getConsole())).toList();
            scenarioReportDTO.setConsole(String.join("\n", consoleList));
        }
        //查询资源池名称
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(scenarioReport.getPoolId());
        scenarioReportDTO.setPoolName(testResourcePool != null ? testResourcePool.getName() : null);
        //查询环境名称
        String environmentName = null;
        if (StringUtils.isNotBlank(scenarioReport.getEnvironmentId())) {
            Environment environment = environmentMapper.selectByPrimaryKey(scenarioReport.getEnvironmentId());
            if (environment != null) {
                environmentName = environment.getName();
            }
            EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(scenarioReport.getEnvironmentId());
            if (environmentGroup != null) {
                environmentName = environmentGroup.getName();
            }
        }
        scenarioReportDTO.setEnvironmentName(environmentName);
        scenarioReportDTO.setCreatUserName(userMapper.selectByPrimaryKey(scenarioReport.getCreateUser()).getName());
        return scenarioReportDTO;
    }

    private static void detailRequest(List<ApiScenarioReportStepDTO> scenarioReportSteps, Map<String, List<ApiScenarioReportStepDTO>> detailMap) {
        List<String> stepTypes = Arrays.asList(ApiScenarioStepType.API_CASE.name(),
                ApiScenarioStepType.API.name(),
                ApiScenarioStepType.CUSTOM_REQUEST.name(),
                ApiScenarioStepType.SCRIPT.name());
        scenarioReportSteps.parallelStream().forEach(step -> {
            if (StringUtils.equals(ApiScenarioStepType.CONSTANT_TIMER.name(), step.getStepType())) {
                if (CollectionUtils.isNotEmpty(detailMap.get(step.getStepId()))) {
                    step.setStatus(ResultStatus.SUCCESS.name());
                } else {
                    step.setStatus(ExecStatus.PENDING.name());
                }
            }
            if (stepTypes.contains(step.getStepType())) {
                List<ApiScenarioReportStepDTO> details = detailMap.get(step.getStepId());
                if (CollectionUtils.isNotEmpty(details) && details.size() > 1) {
                    details.sort(Comparator.comparingLong(ApiScenarioReportStepDTO::getLoopIndex));
                    if (details.size() > 1) {
                        //需要重新处理sort
                        for (int i = 0; i < details.size(); i++) {
                            ApiScenarioReportStepDTO detail = details.get(i);
                            detail.setSort((long) i + 1);
                            detail.setStepId(step.getStepId() + SPLITTER + detail.getSort());
                            detail.setStepType(step.getStepType());
                            detail.setName(detail.getRequestName());
                        }

                        step.setRequestTime(details.stream().mapToLong(ApiScenarioReportStepDTO::getRequestTime).sum());
                        step.setResponseSize(details.stream().mapToLong(ApiScenarioReportStepDTO::getResponseSize).sum());
                        List<String> requestStatus = details.stream().map(ApiScenarioReportStepDTO::getStatus).toList();
                        List<String> successStatus = requestStatus.stream().filter(status -> StringUtils.equals(ResultStatus.SUCCESS.name(), status)).toList();
                        if (requestStatus.contains(ResultStatus.ERROR.name())) {
                            step.setStatus(ResultStatus.ERROR.name());
                        } else if (requestStatus.contains(ResultStatus.FAKE_ERROR.name())) {
                            step.setStatus(ResultStatus.FAKE_ERROR.name());
                        } else if (successStatus.size() == details.size()) {
                            step.setStatus(ResultStatus.SUCCESS.name());
                        } else {
                            step.setStatus(ExecStatus.PENDING.name());
                        }

                        // 重试的话，取最后一次的状态
                        ApiScenarioReportStepDTO lastDetail = details.getLast();
                        if (lastDetail.getName().contains("MsRetry_")) {
                            step.setStatus(lastDetail.getStatus());
                        }
                    }
                    step.setChildren(details);
                } else if (CollectionUtils.isNotEmpty(details)) {
                    step.setName(details.getFirst().getRequestName());
                    step.setReportId(details.getFirst().getReportId());
                    step.setRequestTime(details.getFirst().getRequestTime());
                    step.setResponseSize(details.getFirst().getResponseSize());
                    step.setStatus(details.getFirst().getStatus());
                    step.setCode(details.getFirst().getCode());
                    step.setFakeCode(details.getFirst().getFakeCode());
                    step.setScriptIdentifier(details.getFirst().getScriptIdentifier());
                }
            }
        });
    }

    public long getRequestTotal(ApiScenarioReport report) {
        return report.getErrorCount() + report.getPendingCount() + report.getSuccessCount() + report.getFakeErrorCount();
    }

    private static void getStepTree(List<ApiScenarioReportStepDTO> steps, Map<String, List<ApiScenarioReportStepDTO>> scenarioReportStepMap) {
        if (CollectionUtils.isNotEmpty(steps)) {
            List<String> stepTypes = Arrays.asList(ApiScenarioStepType.IF_CONTROLLER.name(),
                    ApiScenarioStepType.LOOP_CONTROLLER.name(),
                    ApiScenarioStepType.ONCE_ONLY_CONTROLLER.name());
            steps.parallelStream().forEach(step -> {
                List<ApiScenarioReportStepDTO> children = scenarioReportStepMap.get(step.getStepId());
                if (CollectionUtils.isNotEmpty(children)) {
                    children.sort(Comparator.comparingLong(ApiScenarioReportStepDTO::getSort));
                    step.setChildren(children);
                    getStepTree(children, scenarioReportStepMap);
                    //如果是父级的报告，需要计算请求时间  请求时间是所有子级的请求时间之和 还需要计算请求的大小  还有请求的数量 以及请求成功的状态
                    step.setRequestTime(step.getChildren().stream().mapToLong(child -> child.getRequestTime() != null ? child.getRequestTime() : 0).sum());
                    step.setResponseSize(step.getChildren().stream().mapToLong(child -> child.getResponseSize() != null ? child.getResponseSize() : 0).sum());
                    //请求的状态， 如果是 LOOP_CONTROLLER IF_CONTROLLER ONCE_ONLY_CONTROLLER  则需要判断子级的状态 但是如果下面没有子集不需要判断状态
                    //需要把这些数据拿出来 如果没有子请求说明是最后一级的请求 不需要计算入状态
                    //获取所有的子请求的状态
                    List<String> requestStatus = children.stream().map(ApiScenarioReportStepDTO::getStatus).toList();
                    //获取为执行的状态
                    List<String> pendingStatus = requestStatus.stream().filter(status -> StringUtils.equals(ExecStatus.PENDING.name(), status) || StringUtils.isBlank(status)).toList();
                    //过滤出来SUCCESS的状态
                    List<String> successStatus = requestStatus.stream().filter(status -> StringUtils.equals(ResultStatus.SUCCESS.name(), status)).toList();
                    //只要包含ERROR 就是ERROR
                    if (requestStatus.contains(ResultStatus.ERROR.name())) {
                        step.setStatus(ResultStatus.ERROR.name());
                    } else if (requestStatus.contains(ResultStatus.FAKE_ERROR.name())) {
                        step.setStatus(ResultStatus.FAKE_ERROR.name());
                    } else if (successStatus.size() + pendingStatus.size() == children.size() && !successStatus.isEmpty()) {
                        step.setStatus(ResultStatus.SUCCESS.name());
                    }
                } else if (stepTypes.contains(step.getStepType())) {
                    step.setStatus(ExecStatus.PENDING.name());
                }
            });
        }
    }

    public List<ApiScenarioReportDetailDTO> getDetail(String reportId, String stepId) {
        //如果是循环控制器下的步骤id 会带着第几条  需要分割处理
        String index = null;
        if (StringUtils.isNotBlank(stepId) && StringUtils.contains(stepId, SPLITTER)) {
            index = StringUtils.substringAfter(stepId, SPLITTER);
            stepId = StringUtils.substringBefore(stepId, SPLITTER);
        }
        List<ApiScenarioReportDetailDTO> apiReportDetails = checkResourceStep(stepId, reportId);
        apiReportDetails.sort(Comparator.comparingLong(ApiScenarioReportDetailDTO::getSort));

        if (StringUtils.isNotBlank(index)) {
            ApiScenarioReportDetailDTO apiScenarioReportDetail = apiReportDetails.get(Integer.parseInt(index) - 1);
            apiReportDetails = Collections.singletonList(apiScenarioReportDetail);
        }
        return apiReportDetails;
    }

    private List<ApiScenarioReportDetailDTO> checkResourceStep(String stepId, String reportId) {
        List<ApiScenarioReportDetailDTO> apiReportDetails = extApiScenarioReportDetailBlobMapper.selectByExampleWithBLOBs(stepId, reportId);
        if (CollectionUtils.isEmpty(apiReportDetails)) {
            return new ArrayList<>();
        }
        return apiReportDetails;
    }

    public List<ApiScenarioReport> getApiScenarioReportByIds(List<String> reportIds) {
        if (CollectionUtils.isEmpty(reportIds)) {
            return List.of();
        }
        ApiScenarioReportExample reportExample = new ApiScenarioReportExample();
        reportExample.createCriteria().andIdIn(reportIds);
        return apiScenarioReportMapper.selectByExample(reportExample);
    }

    public void exportLog(String reportId, String userId, String projectId) {
        ApiScenarioReport apiScenarioReport = apiScenarioReportMapper.selectByPrimaryKey(reportId);
        Optional.ofNullable(apiScenarioReport).ifPresent(report -> apiScenarioReportLogService.exportLog(List.of(report), userId, projectId, "/api/report/scenario/export/" + reportId));
    }

    public void batchExportLog(ApiReportBatchRequest request, String userId, String projectId) {
        List<String> ids = doSelectIds(request);
        if(CollectionUtils.isNotEmpty(ids)){
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(ids);
            List<ApiScenarioReport> reports = apiScenarioReportMapper.selectByExample(example);
            apiScenarioReportLogService.exportLog(reports, userId, projectId, "/api/report/scenario/batch-export");
        }
    }

    public ExecTaskDetailDTO viewScenarioItemReport(String id) {
        List<ExecTaskItemDetailDTO> taskList = extExecTaskMapper.selectTypeByItemId(id);
        ExecTaskDetailDTO apiTaskReportDTO = new ExecTaskDetailDTO();

        if (CollectionUtils.isNotEmpty(taskList)) {
            ExecTaskItemDetailDTO task = taskList.getFirst();
            //设置 顶部数据
            BeanUtils.copyBean(apiTaskReportDTO, task);
            //计划组处理来源
            if (StringUtils.isNotBlank(apiTaskReportDTO.getTaskOrigin())) {
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(apiTaskReportDTO.getTaskOrigin());
                Optional.ofNullable(testPlan).ifPresent(item -> apiTaskReportDTO.setTaskOriginName(testPlan.getName()));
            }
            if (task.getIntegrated()) {
                //场景集合报告
                ApiScenarioReportDTO scenarioReportDetail = getScenarioReportDetail(id, task.getId());
                BeanUtils.copyBean(apiTaskReportDTO, scenarioReportDetail);
            } else {
                //场景非集合报告
                ApiScenarioReportDTO scenarioReportDetail = scenarioReportDetail(id);
                BeanUtils.copyBean(apiTaskReportDTO, scenarioReportDetail);
            }
            apiTaskReportDTO.setStartTime(task.getStartTime());
            apiTaskReportDTO.setEndTime(task.getEndTime());
        }
        return apiTaskReportDTO;
    }

    private ApiScenarioReportDTO getScenarioReportDetail(String taskItemId, String taskId) {
        ExecTaskItem taskItem = execTaskItemMapper.selectByPrimaryKey(taskItemId);
        ApiScenarioReportDTO apiScenarioReportDTO = scenarioReportDetail(taskId);
        if (CollectionUtils.isNotEmpty(apiScenarioReportDTO.getChildren())) {
            List<ApiScenarioReportStepDTO> list = apiScenarioReportDTO.getChildren().stream()
                    .filter(step -> StringUtils.equals(step.getStepId(), taskItem.getResourceId())).toList();
            apiScenarioReportDTO.setChildren(list);
        }
        return apiScenarioReportDTO;
    }

    private ApiScenarioReportDTO scenarioReportDetail(String id) {
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdEqualTo(id);
        List<ApiReportRelateTask> apiReportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiReportRelateTasks)) {
            //报告id
            String reportId = apiReportRelateTasks.getFirst().getReportId();
            return get(reportId);
        }
        return new ApiScenarioReportDTO();
    }
}
