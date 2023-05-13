package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtFileContentMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportKeys;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.*;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineFactory;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.performance.PerformanceReference;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.quota.service.BaseQuotaService;
import io.metersphere.request.DeleteReportRequest;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.RenameReportRequest;
import io.metersphere.request.ReportRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PerformanceReportService {

    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private LoadTestReportResultMapper loadTestReportResultMapper;
    @Resource
    private LoadTestReportResultPartMapper loadTestReportResultPartMapper;
    @Resource
    private LoadTestReportResultRealtimeMapper loadTestReportResultRealtimeMapper;
    @Resource
    private LoadTestReportLogMapper loadTestReportLogMapper;
    @Resource
    private BaseTestResourceService baseTestResourceService;
    @Resource
    private LoadTestReportDetailMapper loadTestReportDetailMapper;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private LoadTestReportFileMapper loadTestReportFileMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private BaseEnvironmentService baseEnvironmentService;
    @Resource
    private BaseQuotaService baseQuotaService;

    public List<ReportDTO> getRecentReportList(ReportRequest request) {
        List<OrderRequest> orders = new ArrayList<>();
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setName("update_time");
        orderRequest.setType("desc");
        orders.add(orderRequest);
        request.setOrders(orders);
        return extLoadTestReportMapper.getReportList(request);
    }

    public List<ReportDTO> getReportList(ReportRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extLoadTestReportMapper.getReportList(request);
    }

    public void deleteReport(String reportId) {
        if (StringUtils.isBlank(reportId)) {
            MSException.throwException("report id cannot be null");
        }

        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(loadTestReport.getTestId());

        LogUtil.info("Delete report started, report ID: %s" + reportId);

        if (loadTest != null) {
            try {
                final Engine engine = EngineFactory.createEngine(loadTestReport);
                if (engine == null) {
                    MSException.throwException(String.format("Delete report fail. create engine fail，report ID：%s", reportId));
                }

                String reportStatus = loadTestReport.getStatus();
                boolean isRunning = StringUtils.equals(reportStatus, PerformanceTestStatus.Running.name());
                boolean isStarting = StringUtils.equals(reportStatus, PerformanceTestStatus.Starting.name());
                boolean isError = StringUtils.equals(reportStatus, PerformanceTestStatus.Error.name());
                if (isError) {
                    LogUtil.info("Start stop engine, report status: %s" + reportStatus);
                    stopEngine(loadTest, engine);
                } else if (isRunning || isStarting) {
                    stopEngineHandleVum(loadTestReport, engine);
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                loadTest.setStatus(PerformanceTestStatus.Saved.name());
                loadTestMapper.updateByPrimaryKeySelective(loadTest);
            }
        }

        // delete load_test_report_result
        LoadTestReportResultExample loadTestReportResultExample = new LoadTestReportResultExample();
        loadTestReportResultExample.createCriteria().andReportIdEqualTo(reportId);
        loadTestReportResultMapper.deleteByExample(loadTestReportResultExample);

        // delete load_test_report_result
        LoadTestReportResultPartExample loadTestReportResultPartExample = new LoadTestReportResultPartExample();
        loadTestReportResultPartExample.createCriteria().andReportIdEqualTo(reportId);
        loadTestReportResultPartMapper.deleteByExample(loadTestReportResultPartExample);

        // delete load_test_report_result
        LoadTestReportResultRealtimeExample loadTestReportResultRealtimeExample = new LoadTestReportResultRealtimeExample();
        loadTestReportResultRealtimeExample.createCriteria().andReportIdEqualTo(reportId);
        loadTestReportResultRealtimeMapper.deleteByExample(loadTestReportResultRealtimeExample);

        // delete load_test_report_detail
        LoadTestReportDetailExample example = new LoadTestReportDetailExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        loadTestReportDetailMapper.deleteByExample(example);

        // delete load_test_report_file
        LoadTestReportFileExample loadTestReportFileExample = new LoadTestReportFileExample();
        loadTestReportFileExample.createCriteria().andReportIdEqualTo(reportId);
        loadTestReportFileMapper.deleteByExample(loadTestReportFileExample);

        // delete load_test_report_log
        LoadTestReportLogExample loadTestReportLogExample = new LoadTestReportLogExample();
        loadTestReportLogExample.createCriteria().andReportIdEqualTo(reportId);
        loadTestReportLogMapper.deleteByExample(loadTestReportLogExample);

        // delete jtl file
        fileMetadataService.deleteFile(loadTestReport.getFileId());

        // check test_plan_load_case 的 status
        List<String> updatedId = extTestPlanLoadCaseMapper.selectIdByLoadCaseReportIdAndStatusIsRun(reportId);
        if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(updatedId)) {
            for (String id : updatedId) {
                extTestPlanLoadCaseMapper.updateStatusNullById(id);
            }
        }
        loadTestReportMapper.deleteByPrimaryKey(reportId);
    }

    public void stopEngine(LoadTestWithBLOBs loadTest, Engine engine) {
        engine.stop();
        loadTest.setStatus(PerformanceTestStatus.Saved.name());
        loadTestMapper.updateByPrimaryKeySelective(loadTest);
    }

    public void stopEngineHandleVum(LoadTestReportWithBLOBs report, Engine engine) {
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(report.getTestId());
        String projectId = report.getProjectId();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null || StringUtils.isBlank(project.getWorkspaceId())) {
            MSException.throwException("project is null or workspace_id of project is null. project id: " + projectId);
        }
        RLock lock = redissonClient.getLock(project.getWorkspaceId());
        try {
            lock.lock();
            BigDecimal toReduceVum = baseQuotaService.getReduceVumUsed(report);
            if (toReduceVum.compareTo(BigDecimal.ZERO) != 0) {
                baseQuotaService.updateVumUsed(projectId, toReduceVum.negate());
            }
            engine.stop();
            loadTest.setStatus(PerformanceTestStatus.Saved.name());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
        } finally {
            lock.unlock();
        }
    }

    public ReportDTO getReportTestAndProInfo(String reportId) {
        ReportDTO reportDTO = extLoadTestReportMapper.getReportTestAndProInfo(reportId);
        this.parseRunEnvironment(reportDTO);
        return reportDTO;
    }

    private String getContent(String id, ReportKeys reportKey) {
        LoadTestReportResultExample example = new LoadTestReportResultExample();
        example.createCriteria().andReportIdEqualTo(id).andReportKeyEqualTo(reportKey.name());
        List<LoadTestReportResult> loadTestReportResults = loadTestReportResultMapper.selectByExampleWithBLOBs(example);
        if (loadTestReportResults.size() == 0) {
            LogUtil.warn("get report result error");
            return null;
        }
        return loadTestReportResults.get(0).getReportValue();
    }

    public List<Statistics> getReportStatistics(String id) {
        if (isReportError(id)) {
            return Collections.emptyList();
        }
        String reportValue = getContent(id, ReportKeys.RequestStatistics);
        if (StringUtils.isEmpty(reportValue)) {
            return Collections.emptyList();
        }
        // 确定顺序
        List<Statistics> statistics = JSON.parseArray(reportValue, Statistics.class);
        if (CollectionUtils.isEmpty(statistics)) {
            return Collections.emptyList();
        }
        List<LoadTestExportJmx> jmxContent = getJmxContent(id);
        String jmx = jmxContent.get(0).getJmx();
        // 按照JMX顺序重新排序
        statistics.sort(Comparator.comparingInt(a -> jmx.indexOf("\"" + a.getLabel() + "\"")));
        // 把 total 放到最后
        List<Statistics> total = statistics.stream()
                .filter(r -> StringUtils.equalsAnyIgnoreCase(r.getLabel(), "Total"))
                .collect(Collectors.toList());
        statistics.removeAll(total);
        statistics.addAll(total);
        return statistics;
    }

    public List<Errors> getReportErrors(String id) {
        if (isReportError(id)) {
            return Collections.emptyList();
        }
        String content = getContent(id, ReportKeys.Errors);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(content, Errors.class);
    }

    public List<ErrorsTop5> getReportErrorsTOP5(String id) {
        if (isReportError(id)) {
            return Collections.emptyList();
        }
        String content = getContent(id, ReportKeys.ErrorsTop5);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(content, ErrorsTop5.class);
    }

    public TestOverview getTestOverview(String id) {
        if (isReportError(id)) {
            return new TestOverview();
        }
        String content = getContent(id, ReportKeys.Overview);
        if (StringUtils.isEmpty(content)) {
            return new TestOverview();
        }
        return JSON.parseObject(content, TestOverview.class);
    }

    public ReportTimeInfo getReportTimeInfo(String id) {
        if (isReportError(id)) {
            return new ReportTimeInfo();
        }
        String content = getContent(id, ReportKeys.TimeInfo);
        if (StringUtils.isEmpty(content)) {
            return new ReportTimeInfo();
        }
        try {
            return JSON.parseObject(content, ReportTimeInfo.class);
        } catch (Exception e) {
            // 兼容字符串和数字
            ReportTimeInfo reportTimeInfo = new ReportTimeInfo();
            Map jsonObject = JSON.parseObject(content, Map.class);
            String startTime = (String) jsonObject.get("startTime");
            String endTime = (String) jsonObject.get("endTime");
            String duration = (String) jsonObject.get("duration");

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            try {
                reportTimeInfo.setStartTime(df.parse(startTime).getTime());
                reportTimeInfo.setEndTime(df.parse(endTime).getTime());
                reportTimeInfo.setDuration(Long.parseLong(duration));
            } catch (Exception parseException) {
            }
            return reportTimeInfo;
        }
    }

    public List<ChartsData> getLoadChartData(String id) {
        if (isReportError(id)) {
            return Collections.emptyList();
        }
        String content = getContent(id, ReportKeys.LoadChart);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(content, ChartsData.class);
    }

    public List<ChartsData> getResponseTimeChartData(String id) {
        if (isReportError(id)) {
            return Collections.emptyList();
        }
        String content = getContent(id, ReportKeys.ResponseTimeChart);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(content, ChartsData.class);
    }

    public boolean isReportError(String reportId) {
        LoadTestReport loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        if (loadTestReport == null) {
            return true;
        }
        return StringUtils.equals(PerformanceTestStatus.Error.name(), loadTestReport.getStatus());
    }

    public LoadTestReportInfoDTO getLoadTestReport(String id) {
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(id);
        LoadTestReportInfoDTO returnDTO = new LoadTestReportInfoDTO();
        BeanUtils.copyBean(returnDTO, loadTestReport);
        this.parseRunEnvironment(returnDTO);
        return returnDTO;
    }

    private void parseRunEnvironment(LoadTestReportInfoDTO loadTestReportInfoDTO) {
        if (loadTestReportInfoDTO != null && StringUtils.isNotEmpty(loadTestReportInfoDTO.getEnvInfo())) {
            Map<String, List<String>> projectEnvIdMap = new HashMap<>();
            try {
                projectEnvIdMap = JSON.parseObject(loadTestReportInfoDTO.getEnvInfo(), Map.class);

                LinkedHashMap<String, List<String>> projectEnvNameMap = baseEnvironmentService.selectProjectNameAndEnvName(projectEnvIdMap);
                if (MapUtils.isNotEmpty(projectEnvNameMap)) {
                    loadTestReportInfoDTO.setProjectEnvMap(projectEnvNameMap);
                }
            } catch (Exception e) {
                LogUtil.error("性能测试报告解析运行环境信息失败!解析参数:" + loadTestReportInfoDTO.getEnvInfo(), e);
            }
        }
    }

    public List<LogDetailDTO> getReportLogResource(String reportId) {
        List<LogDetailDTO> result = new ArrayList<>();
        List<String> resourceIdAndIndexes = extLoadTestReportMapper.selectResourceId(reportId);
        resourceIdAndIndexes.forEach(resourceIdAndIndex -> {
            LogDetailDTO detailDTO = new LogDetailDTO();
            String[] split = StringUtils.split(resourceIdAndIndex, "_");
            String resourceId = split[0];
            TestResource testResource = baseTestResourceService.getTestResource(resourceId);
            detailDTO.setResourceId(resourceIdAndIndex);
            if (testResource == null) {
                detailDTO.setResourceName(resourceId);
                result.add(detailDTO);
                return;
            }
            String configuration = testResource.getConfiguration();
            if (StringUtils.isBlank(configuration)) {
                detailDTO.setResourceName(resourceId);
                result.add(detailDTO);
                return;
            }
            Map object = JSON.parseObject(configuration, Map.class);
            if (StringUtils.isNotBlank((String) object.get("masterUrl"))) {
                detailDTO.setResourceName((String) object.get("masterUrl"));
                result.add(detailDTO);
                return;
            }
            if (StringUtils.isNotBlank((String) object.get("ip"))) {
                detailDTO.setResourceName((String) object.get("ip"));
                result.add(detailDTO);
            }
        });
        return result;
    }

    public List<LoadTestReportLog> getReportLogs(String reportId, String resourceId) {
        LoadTestReportLogExample example = new LoadTestReportLogExample();
        example.createCriteria().andReportIdEqualTo(reportId).andResourceIdEqualTo(resourceId);
        example.setOrderByClause("part");
        return loadTestReportLogMapper.selectByExampleWithBLOBs(example);
    }

    public void downloadLog(HttpServletResponse response, String reportId, String resourceId) throws Exception {
        LoadTestReportLogExample example = new LoadTestReportLogExample();
        LoadTestReportLogExample.Criteria criteria = example.createCriteria();
        criteria.andReportIdEqualTo(reportId).andResourceIdEqualTo(resourceId);
        example.setOrderByClause("part");

        long count = loadTestReportLogMapper.countByExample(example);

        try (OutputStream outputStream = response.getOutputStream()) {
            response.setContentType("application/x-download");
            response.addHeader("Content-Disposition", "attachment;filename=jmeter.log");
            for (long i = 1; i <= count; i++) {
                example.clear();
                LoadTestReportLogExample.Criteria innerCriteria = example.createCriteria();
                innerCriteria.andReportIdEqualTo(reportId).andResourceIdEqualTo(resourceId).andPartEqualTo(i);

                List<LoadTestReportLog> loadTestReportLogs = loadTestReportLogMapper.selectByExampleWithBLOBs(example);
                LoadTestReportLog content = loadTestReportLogs.get(0);
                outputStream.write(content.getContent().getBytes());
                outputStream.flush();
            }
        }
    }

    public LoadTestReportWithBLOBs getReport(String reportId) {
        return loadTestReportMapper.selectByPrimaryKey(reportId);
    }

    public void updateStatus(String reportId, String status) {
        LoadTestReportWithBLOBs report = new LoadTestReportWithBLOBs();
        report.setId(reportId);
        report.setStatus(status);
        loadTestReportMapper.updateByPrimaryKeySelective(report);
    }

    public void deleteReportBatch(DeleteReportRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> getLoadTestReportIds(request.getCondition()));

        List<String> ids = request.getIds();
        ids.forEach(this::deleteReport);
    }

    private List<String> getLoadTestReportIds(ReportRequest request) {
        return this.getReportList(request).stream().map(LoadTestReport::getId).collect(Collectors.toList());
    }

    public List<ChartsData> getErrorChartData(String id) {
        if (isReportError(id)) {
            return Collections.emptyList();
        }
        String content = getContent(id, ReportKeys.ErrorsChart);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(content, ChartsData.class);
    }

    public List<ChartsData> getResponseCodeChartData(String id) {
        if (isReportError(id)) {
            return Collections.emptyList();
        }
        String content = getContent(id, ReportKeys.ResponseCodeChart);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        return JSON.parseArray(content, ChartsData.class);
    }

    /**
     * 流下载 jtl zip
     */
    public void downloadJtlZip(String reportId, HttpServletResponse response) {
        LoadTestReportWithBLOBs report = getReport(reportId);
        if (StringUtils.isBlank(report.getFileId())) {
            MSException.throwException(Translator.get("load_test_report_file_not_exist"));
        }
        response.setHeader("Content-Disposition", "attachment;fileName=" + reportId + ".zip");

        FileMetadata metadata = fileMetadataService.getFileMetadataById(report.getFileId());
        if (StringUtils.isEmpty(metadata.getStorage())) {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                ExtFileContentMapper mapper = sqlSession.getMapper(ExtFileContentMapper.class);
                try (InputStream inputStream = mapper.selectZipBytes(report.getFileId())) {
                    ServletOutputStream outputStream = response.getOutputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int read;
                    while ((read = inputStream.read(buffer)) > -1) {
                        outputStream.write(buffer, 0, read);
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                    MSException.throwException(e);
                }
            }
        } else {
            try (InputStream inputStream = fileMetadataService.getFileAsStream(report.getFileId())) {
                ServletOutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[1024 * 4];
                int read;
                while ((read = inputStream.read(buffer)) > -1) {
                    outputStream.write(buffer, 0, read);
                }
            } catch (Exception e) {
                LogUtil.error(e);
                MSException.throwException(e);
            }
        }
    }

    public String getPoolTypeByReportId(String reportId) {
        LoadTestReportWithBLOBs report = getReport(reportId);
        String poolId = report.getTestResourcePoolId();
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(poolId);
        if (testResourcePool != null) {
            return testResourcePool.getType();
        }
        return StringUtils.EMPTY;
    }

    public List<LoadTestExportJmx> getJmxContent(String reportId) {
        LoadTestReportWithBLOBs loadTestReportWithBLOBs = loadTestReportMapper.selectByPrimaryKey(reportId);
        if (loadTestReportWithBLOBs == null) {
            return new ArrayList<>();
        }
        LoadTestExportJmx loadTestExportJmx = new LoadTestExportJmx(loadTestReportWithBLOBs.getTestName(), loadTestReportWithBLOBs.getJmxContent());
        return Collections.singletonList(loadTestExportJmx);
    }

    public void renameReport(RenameReportRequest request) {
        LoadTestReportWithBLOBs record = new LoadTestReportWithBLOBs();
        record.setId(request.getId());
        record.setName(request.getName());
        loadTestReportMapper.updateByPrimaryKeySelective(record);
    }

    public String getLogDetails(String id) {
        LoadTestReportWithBLOBs loadTest = loadTestReportMapper.selectByPrimaryKey(id);
        if (loadTest != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(loadTest, PerformanceReference.reportColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(loadTest.getId()), loadTest.getProjectId(), loadTest.getName(), null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andIdIn(ids);
        List<LoadTestReport> loadTests = loadTestReportMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(loadTests)) {
            List<String> names = loadTests.stream().map(LoadTestReport::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), loadTests.get(0).getProjectId(), String.join(",", names), null, new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<LoadTestReport> getReportList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andIdIn(ids);
        return loadTestReportMapper.selectByExample(example);
    }

    public List<ChartsData> getReportChart(String reportKey, String reportId) {
        if (isReportError(reportId)) {
            return Collections.emptyList();
        }
        try {
            String content = getContent(reportId, ReportKeys.valueOf(reportKey));
            if (StringUtils.isEmpty(content)) {
                return Collections.emptyList();
            }
            return JSON.parseArray(content, ChartsData.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getLoadConfiguration(String reportId) {
        LoadTestReportWithBLOBs loadTestReportWithBLOBs = loadTestReportMapper.selectByPrimaryKey(reportId);
        if (loadTestReportWithBLOBs == null) {
            return null;
        }
        return loadTestReportWithBLOBs.getLoadConfiguration();
    }

    public String getAdvancedConfiguration(String reportId) {
        LoadTestReportWithBLOBs loadTestReportWithBLOBs = loadTestReportMapper.selectByPrimaryKey(reportId);
        if (loadTestReportWithBLOBs == null) {
            return null;
        }
        return loadTestReportWithBLOBs.getAdvancedConfiguration();
    }

    public void cleanUpReport(long time, String projectId) {
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andCreateTimeLessThan(time).andProjectIdEqualTo(projectId);
        List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(example);
        List<String> ids = loadTestReports.stream().map(LoadTestReport::getId).collect(Collectors.toList());
        DeleteReportRequest request = new DeleteReportRequest();
        request.setIds(ids);
        deleteReportBatch(request);
    }

    public List<FileMetadata> getFileMetadataByReportId(String reportId) {
        return extLoadTestReportMapper.getFileMetadataById(reportId);
    }

    public List<String> selectForPlanReport(List<String> apiReportIds) {
        return extLoadTestReportMapper.getStatusByIds(apiReportIds);
    }
}
