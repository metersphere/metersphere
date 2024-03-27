package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.report.ApiScenarioReportListDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDetailDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportStepDTO;
import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.api.mapper.*;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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
    private ApiScenarioReportLogService apiScenarioReportLogService;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiScenarioReport(List<ApiScenarioReport> reports, List<ApiScenarioRecord> records) {
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
        example.createCriteria().andIdEqualTo(id).andDeletedEqualTo(false);
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
        if (CollectionUtils.isEmpty(scenarioReportSteps)) {
            throw new MSException(Translator.get("api_scenario_report_not_exist"));
        }
        if (BooleanUtils.isFalse(scenarioReport.getIntegrated())) {
            ApiScenarioBlob apiScenarioBlob = extApiScenarioReportMapper.getScenarioBlob(id);
            if (apiScenarioBlob != null) {
                ScenarioConfig scenarioConfig = JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class);
                scenarioReportDTO.setWaitingTime(scenarioConfig.getOtherConfig().getStepWaitTime());
            }
        }
        //将scenarioReportSteps按照parentId进行分组 值为list 然后根据sort进行排序
        Map<String, List<ApiScenarioReportStepDTO>> scenarioReportStepMap = scenarioReportSteps.stream().collect(Collectors.groupingBy(ApiScenarioReportStepDTO::getParentId));
        // TODO 查询修改
        List<ApiScenarioReportStepDTO> steps = scenarioReportStepMap.get("NONE");
        steps.sort(Comparator.comparingLong(ApiScenarioReportStepDTO::getSort));
        getStepTree(steps, scenarioReportStepMap);
        scenarioReportDTO.setStepTotal(steps.size());
        scenarioReportDTO.setRequestTotal(scenarioReportDTO.getErrorCount() + scenarioReportDTO.getPendingCount() + scenarioReportDTO.getSuccessCount() + scenarioReportDTO.getFakeErrorCount());
        scenarioReportDTO.setChildren(steps);

        scenarioReportDTO.setStepErrorCount(steps.stream().filter(step -> StringUtils.equals(ApiReportStatus.ERROR.name(), step.getStatus())).count());
        scenarioReportDTO.setStepSuccessCount(steps.stream().filter(step -> StringUtils.equals(ApiReportStatus.SUCCESS.name(), step.getStatus())).count());
        scenarioReportDTO.setStepPendingCount(steps.stream().filter(step -> StringUtils.equals(ApiReportStatus.PENDING.name(), step.getStatus())).count());
        scenarioReportDTO.setStepFakeErrorCount(steps.stream().filter(step -> StringUtils.equals(ApiReportStatus.FAKE_ERROR.name(), step.getStatus())).count());
        //控制台信息 console
        ApiScenarioReportLogExample example = new ApiScenarioReportLogExample();
        example.createCriteria().andReportIdEqualTo(id);
        List<ApiScenarioReportLog> apiScenarioReportLogs = apiScenarioReportLogMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(apiScenarioReportLogs)) {
            scenarioReportDTO.setConsole(new String(apiScenarioReportLogs.getFirst().getConsole()));
        }
        //查询资源池名称
        scenarioReportDTO.setPoolName(testResourcePoolMapper.selectByPrimaryKey(scenarioReport.getPoolId()).getName());
        //查询环境名称
        String environmentName = Translator.get("api_report_default_env");
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

    private static void getStepTree(List<ApiScenarioReportStepDTO> steps, Map<String, List<ApiScenarioReportStepDTO>> scenarioReportStepMap) {
        if (CollectionUtils.isNotEmpty(steps)) {
            List<String> stepTypes = Arrays.asList(ApiScenarioStepType.IF_CONTROLLER.name(),
                    ApiScenarioStepType.LOOP_CONTROLLER.name(),
                    ApiScenarioStepType.ONCE_ONLY_CONTROLLER.name(),
                    ApiScenarioStepType.CONSTANT_TIMER.name());
            for (ApiScenarioReportStepDTO step : steps) {
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
                    //children 先过滤满足控制器的数据，然后再获取id
                    List<String> controllerIds = children.stream().filter(child -> stepTypes.contains(child.getStepType())).map(ApiScenarioReportStepDTO::getStepId).toList();
                    //看map中有没有这些id  如果没有 需要返回几个没有
                    List<String> noControllerIds = controllerIds.stream().filter(controllerId -> !scenarioReportStepMap.containsKey(controllerId)).toList();
                    //获取所有的子请求的状态
                    List<String> requestStatus = children.stream().map(ApiScenarioReportStepDTO::getStatus).toList();
                    //过滤出来SUCCESS的状态
                    List<String> successStatus = requestStatus.stream().filter(status -> StringUtils.equals(ApiReportStatus.SUCCESS.name(),status)).toList();
                    //只要包含ERROR 就是ERROR
                    if (requestStatus.contains(ApiReportStatus.ERROR.name())) {
                        step.setStatus(ApiReportStatus.ERROR.name());
                    } else if (requestStatus.contains(ApiReportStatus.FAKE_ERROR.name())) {
                        step.setStatus(ApiReportStatus.FAKE_ERROR.name());
                    } else if (successStatus.size() == children.size()- noControllerIds.size()) {
                        step.setStatus(ApiReportStatus.SUCCESS.name());
                    } else {
                        step.setStatus(ApiReportStatus.PENDING.name());
                    }
                } else if (stepTypes.contains(step.getStepType())) {
                    step.setStatus(ApiReportStatus.PENDING.name());
                    if (StringUtils.equals(ApiScenarioStepType.CONSTANT_TIMER.name(), step.getStepType())) {
                        step.setStatus(ApiReportStatus.SUCCESS.name());
                    }
                }
            }
        }
    }

    public List<ApiScenarioReportDetailDTO> getDetail(String reportId, String stepId) {
        List<ApiScenarioReportDetail> apiReportDetails = checkResourceStep(stepId, reportId);
        List<ApiScenarioReportDetailDTO> results = new ArrayList<>();
        apiReportDetails.forEach(apiReportDetail -> {
            ApiScenarioReportDetailDTO apiReportDetailDTO = new ApiScenarioReportDetailDTO();
            BeanUtils.copyBean(apiReportDetailDTO, apiReportDetail);
            apiReportDetailDTO.setContent(ApiDataUtils.parseObject(new String(apiReportDetail.getContent()), RequestResult.class));
            results.add(apiReportDetailDTO);
        });
        return results;
    }

    private List<ApiScenarioReportDetail> checkResourceStep(String stepId, String reportId) {
        ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
        detailExample.createCriteria().andStepIdEqualTo(stepId).andReportIdEqualTo(reportId);
        List<ApiScenarioReportDetail> apiReportDetails = apiScenarioReportDetailMapper.selectByExampleWithBLOBs(detailExample);
        if (CollectionUtils.isEmpty(apiReportDetails)) {
            return new ArrayList<>();
        }
        return apiReportDetails;
    }

    /**
     * 更新执行中的场景报告
     * @param reportId
     */
    public void updateReportStatus(String reportId, String status) {
        ApiScenarioReport scenarioReport = new ApiScenarioReport();
        scenarioReport.setId(reportId);
        scenarioReport.setStatus(status);
        scenarioReport.setUpdateTime(System.currentTimeMillis());
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport);
    }
}
