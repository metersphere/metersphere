package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiReportBatchRequest;
import io.metersphere.api.dto.definition.ApiReportPageRequest;
import io.metersphere.api.dto.report.ApiScenarioReportListDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportDetailDTO;
import io.metersphere.api.dto.scenario.ApiScenarioReportStepDTO;
import io.metersphere.api.mapper.*;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
        SubListUtils.dealForSubList(ids, 2000, subList -> {
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
        //将scenarioReportSteps按照parentId进行分组 值为list 然后根据sort进行排序
        Map<String, List<ApiScenarioReportStepDTO>> scenarioReportStepMap = scenarioReportSteps.stream().collect(Collectors.groupingBy(ApiScenarioReportStepDTO::getParentId));
        // TODO 查询修改
        List<ApiScenarioReportStepDTO> steps = scenarioReportStepMap.get("NONE");
        steps.sort(Comparator.comparingLong(ApiScenarioReportStepDTO::getSort));
        getStepTree(steps, scenarioReportStepMap);
        scenarioReportDTO.setStepTotal(steps.size());
        scenarioReportDTO.setChildren(steps);
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
        scenarioReportDTO.setEnvironmentName(environmentMapper.selectByPrimaryKey(scenarioReport.getEnvironmentId()).getName());
        return scenarioReportDTO;
    }

    private static void getStepTree(List<ApiScenarioReportStepDTO> steps, Map<String, List<ApiScenarioReportStepDTO>> scenarioReportStepMap) {
        steps.forEach(step -> {
            List<ApiScenarioReportStepDTO> children = scenarioReportStepMap.get(step.getStepId());
            if (CollectionUtils.isNotEmpty(children)) {
                children.sort(Comparator.comparingLong(ApiScenarioReportStepDTO::getSort));
                step.setChildren(children);
                getStepTree(children, scenarioReportStepMap);
            }
        });
    }

    public List<ApiScenarioReportDetailDTO> getDetail(String stepId, String reportId) {
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
            throw new MSException(Translator.get("api_case_report_not_exist"));
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
