package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.report.ApiReportListDTO;
import io.metersphere.api.mapper.*;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.exception.MSException;
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
public class ApiReportService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ApiReportDetailMapper apiReportDetailMapper;
    @Resource
    private ApiReportLogService apiReportLogService;
    @Resource
    private ApiReportLogMapper apiReportLogMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private ApiReportNoticeService apiReportNoticeService;
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;
    @Resource
    private ApiReportStepMapper apiReportStepMapper;
    @Resource
    private ExtExecTaskMapper extExecTaskMapper;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ApiTestCaseRecordMapper apiTestCaseRecordMapper;

    public void insertApiReport(ApiReport report) {
        apiReportMapper.insertSelective(report);
    }

    public void insertApiReportDetail(ApiReportStep apiReportStep, ApiTestCaseRecord apiTestCaseRecord, ApiReportRelateTask apiReportRelateTask) {
        apiReportStepMapper.insertSelective(apiReportStep);
        apiTestCaseRecordMapper.insertSelective(apiTestCaseRecord);
        apiReportRelateTaskMapper.insertSelective(apiReportRelateTask);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReport(ApiReport report, ApiReportRelateTask taskRelation) {
        apiReportMapper.insertSelective(report);
        apiReportRelateTaskMapper.insertSelective(taskRelation);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReport(List<ApiReport> reports, List<ApiTestCaseRecord> records) {
        this.insertApiReport(reports, records, null);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReport(List<ApiReport> reports, List<ApiTestCaseRecord> records, List<ApiReportRelateTask> taskRelations) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        if (CollectionUtils.isNotEmpty(reports)) {
            ApiReportMapper reportMapper = sqlSession.getMapper(ApiReportMapper.class);
            SubListUtils.dealForSubList(reports, 1000, subList -> {
                subList.forEach(reportMapper::insertSelective);
            });
        }
        if (CollectionUtils.isNotEmpty(records)) {
            ApiTestCaseRecordMapper detailMapper = sqlSession.getMapper(ApiTestCaseRecordMapper.class);
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
    public void insertApiReportStep(List<ApiReportStep> reportSteps) {
        if (CollectionUtils.isNotEmpty(reportSteps)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiReportStepMapper stepMapper = sqlSession.getMapper(ApiReportStepMapper.class);
            SubListUtils.dealForSubList(reportSteps, 1000, subList -> {
                subList.forEach(stepMapper::insertSelective);
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public List<ApiReportListDTO> getPage(ApiReportPageRequest request) {
        List<ApiReport> list = extApiReportMapper.list(request);
        List<ApiReportListDTO> results = new ArrayList<>();
        //取所有的userid
        Set<String> userSet = list.stream()
                .flatMap(apiReport -> Stream.of(apiReport.getUpdateUser(), apiReport.getDeleteUser(), apiReport.getCreateUser()))
                .collect(Collectors.toSet());
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
        list.forEach(apiReport -> {
            ApiReportListDTO apiReportDTO = new ApiReportListDTO();
            BeanUtils.copyBean(apiReportDTO, apiReport);
            apiReportDTO.setCreateUserName(userMap.get(apiReport.getCreateUser()));
            apiReportDTO.setUpdateUserName(userMap.get(apiReport.getUpdateUser()));
            results.add(apiReportDTO);
        });
        return results;
    }

    public void rename(String id, String name, String userId) {
        ApiReport apiReport = checkResource(id);
        apiReport.setName(name);
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReport.setUpdateUser(userId);
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }

    public void delete(String id, String userId) {
        ApiReport apiReport = checkResource(id);
        apiReport.setDeleted(true);
        apiReport.setDeleteTime(System.currentTimeMillis());
        apiReport.setDeleteUser(userId);
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }

    private ApiReport checkResource(String id) {
        ApiReportExample example = new ApiReportExample();
        example.createCriteria().andIdEqualTo(id);
        List<ApiReport> apiReport = apiReportMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiReport)) {
            throw new MSException(Translator.get("api_case_report_not_exist"));
        }
        return apiReport.getFirst();
    }

    public void batchDelete(ApiReportBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        User user = userMapper.selectByPrimaryKey(userId);
        SubListUtils.dealForSubList(ids, 500, subList -> {
            ApiReportExample example = new ApiReportExample();
            example.createCriteria().andIdIn(subList);
            ApiReport apiReport = new ApiReport();
            apiReport.setDeleted(true);
            apiReport.setDeleteTime(System.currentTimeMillis());
            apiReport.setDeleteUser(userId);
            apiReportMapper.updateByExampleSelective(apiReport, example);
            //TODO 记录日志
            apiReportLogService.batchDeleteLog(subList, userId, request.getProjectId());
            apiReportNoticeService.batchSendNotice(subList, user, request.getProjectId(), NoticeConstants.Event.DELETE);
        });
    }

    public List<String> doSelectIds(ApiReportBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extApiReportMapper.getIds(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            return request.getSelectIds();
        }
    }

    public ApiReportDTO get(String id) {
        ApiReportDTO apiReportDTO = new ApiReportDTO();
        ApiReport apiReport = checkResource(id);
        BeanUtils.copyBean(apiReportDTO, apiReport);
        //查询console
        apiReportDTO.setTotal(apiReportDTO.getErrorCount() + apiReportDTO.getSuccessCount() + apiReportDTO.getPendingCount() + apiReportDTO.getFakeErrorCount());
        ApiReportLogExample consoleExample = new ApiReportLogExample();
        consoleExample.createCriteria().andReportIdEqualTo(id);
        List<ApiReportLog> apiReportLogs = apiReportLogMapper.selectByExampleWithBLOBs(consoleExample);
        if (CollectionUtils.isNotEmpty(apiReportLogs)) {
            //获取所有的console,生成集合
            List<String> consoleList = apiReportLogs.stream().map(c -> new String(c.getConsole())).toList();
            apiReportDTO.setConsole(String.join("\n", consoleList));
        }
        //查询资源池名称
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(apiReportDTO.getPoolId());
        apiReportDTO.setPoolName(testResourcePool != null ? testResourcePool.getName() : null);
        //查询环境名称
        String environmentName = null;
        if (StringUtils.isNoneBlank(apiReportDTO.getEnvironmentId())) {
            Environment environment = environmentMapper.selectByPrimaryKey(apiReportDTO.getEnvironmentId());
            if (environment != null) {
                environmentName = environment.getName();
            }
            EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(apiReportDTO.getEnvironmentId());
            if (environmentGroup != null) {
                environmentName = environmentGroup.getName();
            }

        }
        apiReportDTO.setEnvironmentName(environmentName);
        apiReportDTO.setCreatUserName(userMapper.selectByPrimaryKey(apiReportDTO.getCreateUser()).getName());
        //需要查询出所有的步骤
        List<ApiReportStepDTO> apiReportSteps = extApiReportMapper.selectStepsByReportId(id);
        if (CollectionUtils.isEmpty(apiReportSteps)) {
            throw new MSException(Translator.get("api_case_report_not_exist"));
        }
        apiReportSteps.sort(Comparator.comparingLong(ApiReportStepDTO::getSort));
        apiReportDTO.setChildren(apiReportSteps);
        apiReportDTO.setTotal((long) apiReportSteps.size());
        apiReportDTO.setPendingCount(apiReportSteps.stream().filter(step -> StringUtils.equals(ExecStatus.PENDING.name(), step.getStatus()) || StringUtils.isBlank(step.getStatus())).count());
        return apiReportDTO;
    }

    public List<ApiReportDetailDTO> getDetail(String reportId, String stepId) {
        List<ApiReportDetail> apiReportDetails = checkResourceStep(stepId, reportId);
        List<ApiReportDetailDTO> results = new ArrayList<>();
        apiReportDetails.forEach(apiReportDetail -> {
            ApiReportDetailDTO apiReportDetailDTO = new ApiReportDetailDTO();
            BeanUtils.copyBean(apiReportDetailDTO, apiReportDetail);
            apiReportDetailDTO.setContent(apiReportDetail.getContent() != null ? ApiDataUtils.parseObject(new String(apiReportDetail.getContent()), RequestResult.class) : null);
            results.add(apiReportDetailDTO);
        });
        return results;
    }

    private List<ApiReportDetail> checkResourceStep(String stepId, String reportId) {
        ApiReportDetailExample apiReportDetailExample = new ApiReportDetailExample();
        apiReportDetailExample.createCriteria().andStepIdEqualTo(stepId).andReportIdEqualTo(reportId);
        List<ApiReportDetail> apiReportDetails = apiReportDetailMapper.selectByExampleWithBLOBs(apiReportDetailExample);
        if (CollectionUtils.isEmpty(apiReportDetails)) {
            return new ArrayList<>();
        }
        return apiReportDetails;
    }

    /**
     * 更新执行中的用例报告
     *
     * @param reportId
     */
    public void updateReportRunningStatus(String reportId) {
        ApiReport apiReport = new ApiReport();
        apiReport.setId(reportId);
        apiReport.setExecStatus(ExecStatus.RUNNING.name());
        apiReport.setStartTime(System.currentTimeMillis());
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }


    /**
     * 更新执行中的用例报告
     *
     * @param reportId
     */
    public void updateReportStatus(String reportId, String status) {
        ApiReport apiReport = new ApiReport();
        apiReport.setId(reportId);
        apiReport.setExecStatus(status);
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }

    public void exportLog(String reportId, String userId, String projectId) {
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey(reportId);
        Optional.ofNullable(apiReport).ifPresent(report -> apiReportLogService.exportLog(List.of(report), userId, projectId, "/api/report/case/export/" + reportId));
    }

    public void batchExportLog(ApiReportBatchRequest request, String userId, String projectId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiReportExample example = new ApiReportExample();
            example.createCriteria().andIdIn(ids);
            List<ApiReport> reports = apiReportMapper.selectByExample(example);
            apiReportLogService.exportLog(reports, userId, projectId, "/api/report/case/batch-export");
        }
    }

    public ApiTaskReportDTO viewCaseTaskItemReport(String id) {
        List<ExecTaskItemDetailDTO> taskList = extExecTaskMapper.selectTypeByItemId(id);
        ApiTaskReportDTO apiTaskReportDTO = new ApiTaskReportDTO();

        if (CollectionUtils.isNotEmpty(taskList)) {
            ExecTaskItemDetailDTO task = taskList.getFirst();
            //设置 顶部数据
            BeanUtils.copyBean(apiTaskReportDTO, task);
            //计划组处理来源
            if (StringUtils.isNotBlank(apiTaskReportDTO.getTaskOrigin())) {
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(apiTaskReportDTO.getTaskOrigin());
                Optional.ofNullable(testPlan).ifPresent(item -> apiTaskReportDTO.setTaskOriginName(testPlan.getName()));
            }
            //资源池名称
            if (StringUtils.isNotBlank(apiTaskReportDTO.getResourcePoolId())) {
                TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(apiTaskReportDTO.getResourcePoolId());
                Optional.ofNullable(testResourcePool).ifPresent(item -> apiTaskReportDTO.setResourcePoolName(testResourcePool.getName()));
            }

            if (task.getIntegrated()) {
                //集合报告
                apiTaskReportDTO.setApiReportDetailDTOList(getIntegratedItemDetail(id, task.getId(), apiTaskReportDTO));
            } else {
                //非集合报告
                apiTaskReportDTO.setApiReportDetailDTOList(reportDetail(id, apiTaskReportDTO));
            }
        }
        return apiTaskReportDTO;
    }

    private List<ApiReportDetailDTO> getIntegratedItemDetail(String taskItemId, String taskId, ApiTaskReportDTO apiTaskReportDTO) {
        ExecTaskItem taskItem = execTaskItemMapper.selectByPrimaryKey(taskItemId);
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdEqualTo(taskId);
        List<ApiReportRelateTask> apiReportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiReportRelateTasks)) {
            return new ArrayList<>();
        }
        String reportId = apiReportRelateTasks.getFirst().getReportId();
        setEnvironment(reportId, apiTaskReportDTO);
        return getDetail(reportId, taskItem.getResourceId());
    }

    private List<ApiReportDetailDTO> reportDetail(String id, ApiTaskReportDTO apiTaskReportDTO) {
        List<ApiReportDetailDTO> list = new ArrayList<>();
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdEqualTo(id);
        List<ApiReportRelateTask> apiReportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiReportRelateTasks)) {
            //报告id
            String reportId = apiReportRelateTasks.getFirst().getReportId();
            //获取步骤id
            String stepId = getStepId(reportId);
            setEnvironment(reportId, apiTaskReportDTO);

            list = getDetail(reportId, stepId);
        }
        return list;
    }

    private void setEnvironment(String reportId, ApiTaskReportDTO apiTaskReportDTO) {
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey(reportId);
        if (StringUtils.isNotBlank(apiReport.getEnvironmentId())) {
            Environment environment = environmentMapper.selectByPrimaryKey(apiReport.getEnvironmentId());
            apiTaskReportDTO.setEnvironmentId(environment.getName());
            apiTaskReportDTO.setEnvironmentName(environment.getName());
        }
    }

    private String getStepId(String reportId) {
        ApiReportStepExample example = new ApiReportStepExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<ApiReportStep> apiReportSteps = apiReportStepMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiReportSteps)) {
            return apiReportSteps.getFirst().getStepId();
        }
        return null;
    }
}
