package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.report.ApiReportListDTO;
import io.metersphere.api.mapper.*;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
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
    private ApiTestCaseRecordMapper apiTestCaseRecordMapper;
    @Resource
    private ApiReportLogMapper apiReportLogMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private EnvironmentMapper environmentMapper;


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReport(List<ApiReport> reports, List<ApiTestCaseRecord> records) {
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
        example.createCriteria().andIdEqualTo(id).andDeletedEqualTo(false);
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
        SubListUtils.dealForSubList(ids, 2000, subList -> {
            ApiReportExample example = new ApiReportExample();
            example.createCriteria().andIdIn(subList);
            ApiReport apiReport = new ApiReport();
            apiReport.setDeleted(true);
            apiReport.setDeleteTime(System.currentTimeMillis());
            apiReport.setDeleteUser(userId);
            apiReportMapper.updateByExampleSelective(apiReport, example);
            //TODO 记录日志
            apiReportLogService.batchDeleteLog(subList, userId, request.getProjectId());
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
        ApiReportLogExample consoleExample = new ApiReportLogExample();
        consoleExample.createCriteria().andReportIdEqualTo(id);
        List<ApiReportLog> apiReportLogs = apiReportLogMapper.selectByExampleWithBLOBs(consoleExample);
        if (CollectionUtils.isNotEmpty(apiReportLogs)) {
            apiReportDTO.setConsole(new String(apiReportLogs.getFirst().getConsole()));
        }
        //查询资源池名称
        apiReportDTO.setPoolName(testResourcePoolMapper.selectByPrimaryKey(apiReportDTO.getPoolId()).getName());
        //查询环境名称
        apiReportDTO.setEnvironmentName(StringUtils.isNoneBlank(apiReportDTO.getEnvironmentId()) ? environmentMapper.selectByPrimaryKey(apiReportDTO.getEnvironmentId()).getName() : null);
        apiReportDTO.setCreatUserName(userMapper.selectByPrimaryKey(apiReportDTO.getCreateUser()).getName());
        //需要查询出所有的步骤
        if (BooleanUtils.isTrue(apiReport.getIntegrated())) {
            List<ApiReportStepDTO> apiReportSteps = extApiReportMapper.selectStepsByReportId(id);
            if (CollectionUtils.isEmpty(apiReportSteps)) {
                throw new MSException(Translator.get("api_case_report_not_exist"));
            }
            apiReportSteps.sort(Comparator.comparingLong(ApiReportStepDTO::getSort));
            apiReportDTO.setChildren(apiReportSteps);
            return apiReportDTO;
        }
        ApiTestCaseRecordExample example = new ApiTestCaseRecordExample();
        example.createCriteria().andApiReportIdEqualTo(id);
        List<ApiTestCaseRecord> apiTestCaseRecords = apiTestCaseRecordMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiTestCaseRecords)) {
            throw new MSException(Translator.get("api_case_report_not_exist"));
        }
        ApiReportStepDTO apiReportStepDTO = new ApiReportStepDTO();
        BeanUtils.copyBean(apiReportStepDTO, apiReportDTO);
        apiReportStepDTO.setStepId(apiTestCaseRecords.getFirst().getApiTestCaseId());
        apiReportStepDTO.setStepType(ApiExecuteResourceType.API_CASE.name());
        List<ApiReportStepDTO> apiReportSteps = new ArrayList<>();
        apiReportSteps.add(apiReportStepDTO);
        apiReportDTO.setChildren(apiReportSteps);
        return apiReportDTO;
    }

    public List<ApiReportDetailDTO> getDetail(String stepId, String reportId) {
        List<ApiReportDetail> apiReportDetails = checkResourceStep(stepId, reportId);
        List<ApiReportDetailDTO> results = new ArrayList<>();
        apiReportDetails.forEach(apiReportDetail -> {
            ApiReportDetailDTO apiReportDetailDTO = new ApiReportDetailDTO();
            BeanUtils.copyBean(apiReportDetailDTO, apiReportDetail);
            apiReportDetailDTO.setContent(ApiDataUtils.parseObject(new String(apiReportDetail.getContent()), RequestResult.class));
            results.add(apiReportDetailDTO);
        });
        return results;
    }

    private List<ApiReportDetail> checkResourceStep(String stepId, String reportId) {
        ApiReportDetailExample apiReportDetailExample = new ApiReportDetailExample();
        apiReportDetailExample.createCriteria().andStepIdEqualTo(stepId).andReportIdEqualTo(reportId);
        List<ApiReportDetail> apiReportDetails = apiReportDetailMapper.selectByExampleWithBLOBs(apiReportDetailExample);
        if (CollectionUtils.isEmpty(apiReportDetails)) {
            throw new MSException(Translator.get("api_case_report_not_exist"));
        }
        return apiReportDetails;
    }

    /**
     * 更新执行中的用例报告
     * @param reportId
     */
    public void updateReportStatus(String reportId, String status) {
        ApiReport apiReport = new ApiReport();
        apiReport.setId(reportId);
        apiReport.setStatus(status);
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }
}
