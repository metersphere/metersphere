package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.report.ApiReportListDTO;
import io.metersphere.api.mapper.*;
import io.metersphere.api.utils.ApiDataUtils;
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
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.User;
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
    public void updateReportStatus(String reportId, String status) {
        ApiReport apiReport = new ApiReport();
        apiReport.setId(reportId);
        apiReport.setExecStatus(status);
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReportMapper.updateByPrimaryKeySelective(apiReport);
    }
}
