package io.metersphere.quota.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.QuotaMapper;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportResultMapper;
import io.metersphere.base.mapper.ext.ExtQuotaMapper;
import io.metersphere.commons.constants.ReportKeys;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.quota.dto.CountDto;
import io.metersphere.quota.dto.QuotaConstants;
import io.metersphere.quota.dto.ReportTimeInfo;
import io.metersphere.quota.dto.TestOverview;
import io.metersphere.request.TestPlanRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseQuotaService {

    @Resource
    private QuotaManagementService quotaManagementService;
    @Resource
    private ExtQuotaMapper extQuotaMapper;
    @Resource
    private QuotaMapper quotaMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ExtLoadTestReportResultMapper extLoadTestReportResultMapper;

    private static final String API = "API";
    private static final String LOAD = "LOAD";
    private static final String DURATION = "DURATION";
    private static final String MAX_THREAD = "MAX_THREAD";
    private static final String MEMBER = "MEMBER";
    private static final String PROJECT = "PROJECT";
    private static final String CHECK_PROJECT = "PROJECT";
    private static final String CHECK_WORKSPACE = "WORKSPACE";

    private boolean isValid(Quota quota, Object obj) {
        boolean sign = quota != null;
        if (obj instanceof Integer) {
            return sign && quotaManagementService.isValid((Integer) obj);
        } else if (obj instanceof String) {
            return sign && quotaManagementService.isValid((String) obj);
        } else if (obj instanceof BigDecimal) {
            return sign && quotaManagementService.isValid((BigDecimal) obj);
        }
        return false;
    }

    private List<String> queryProjectIdsByWorkspaceId(String workspaceId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<Project> projects = projectMapper.selectByExample(example);
        return projects.stream().map(Project::getId).collect(Collectors.toList());
    }

    private String queryWorkspaceId(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return null;
        }
        return project.getWorkspaceId();
    }

    /**
     * 接口定义配额数量检查
     */
    public void checkAPIDefinitionQuota(String projectId) {
        this.checkQuota(extQuotaMapper::countAPIDefinition, API, projectId,
                Translator.get("quota_api_excess_project"),
                Translator.get("quota_api_excess_workspace"));
    }

    /**
     * 接口自动化配额数量检查
     */
    public void checkAPIAutomationQuota(String projectId) {
        this.checkQuota(extQuotaMapper::countAPIAutomation, API, projectId,
                Translator.get("quota_api_excess_project"),
                Translator.get("quota_api_excess_workspace"));
    }

    /**
     * 增量为1的配额检查方法
     *
     * @param queryFunc        查询已存在数量的方法
     * @param checkType        检查配额类型
     * @param projectId        项目ID
     * @param projectMessage   项目超出配额警告信息
     * @param workspaceMessage 工作空间超出配额警告信息
     */
    public void checkQuota(Function<List<String>, Long> queryFunc, String checkType, String projectId, String projectMessage, String workspaceMessage) {
        if (queryFunc == null) {
            LogUtil.info("param warning. function is null");
            return;
        }
        if (StringUtils.isBlank(projectId)) {
            return;
        }
        // 检查项目配额
        Quota qt = quotaManagementService.getProjectQuota(projectId);
        boolean isContinue = true;
        long count;
        if (qt != null) {
            count = queryFunc.apply(Collections.singletonList(projectId));
            // 数量+1后检查
            isContinue = this.doCheckQuota(qt, checkType, projectMessage, count + 1);
        }
        // 检查是否有工作空间限额
        if (isContinue) {
            String workspaceId = this.queryWorkspaceId(projectId);
            if (StringUtils.isBlank(workspaceId)) {
                return;
            }
            Quota quota = quotaManagementService.getWorkspaceQuota(workspaceId);
            if (quota == null) {
                return;
            }
            count = queryFunc.apply(this.queryProjectIdsByWorkspaceId(workspaceId));
            this.doCheckQuota(quota, checkType, workspaceMessage, count + 1);
        }
    }

    private boolean doCheckQuota(Quota quota, String checkType, String errorMsg, long queryCount) {
        if (quota == null) {
            return true;
        }

        Object quotaCount = getQuotaCount(quota, checkType);
        if (isValid(quota, quotaCount)) {
            long count = Long.parseLong(String.valueOf(quotaCount));
            if (queryCount > count) {
                MSException.throwException(errorMsg);
            }
            return false;
        }
        return true;
    }

    private Object getQuotaCount(Quota quota, String type) {
        Object count = null;
        switch (type) {
            case API -> count = quota.getApi();
            case LOAD -> count = quota.getPerformance();
            case DURATION -> count = quota.getDuration();
            case MAX_THREAD -> count = quota.getMaxThreads();
            case MEMBER -> count = quota.getMember();
            case PROJECT -> count = quota.getProject();
            default -> MSException.throwException("get quota count fail, don't have type: " + type);
        }
        return count;
    }

    /**
     * 性能测试配额检查
     * @param request 压力配置
     * @param checkPerformanceCount 是：检查创建数量配额 / 否：检查并发数和时间
     */
    @Transactional(noRollbackFor = MSException.class, rollbackFor = Exception.class)
    public void checkLoadTestQuota(TestPlanRequest request, boolean checkPerformanceCount) {
        String projectId = request.getProjectId();
        if (checkPerformanceCount) {
            this.checkPerformance(projectId);
        } else {
            String loadConfig = request.getLoadConfiguration();
            if (loadConfig != null) {
                checkMaxThread(projectId, getIntegerValue(loadConfig, "TargetLevel"));
                checkDuration(projectId, getIntegerValue(loadConfig, "duration"));
            }
        }
    }

    private void checkPerformance(String projectId) {
        this.checkQuota(extQuotaMapper::countLoadTest, LOAD, projectId,
                Translator.get("quota_performance_excess_project"),
                Translator.get("quota_performance_excess_workspace"));
    }

    private void checkMaxThread(String projectId, int threadNum) {
        // 增量为0的检查
        this.checkQuota(p -> (long) (threadNum - 1), MAX_THREAD, projectId,
                Translator.get("quota_max_threads_excess_project"),
                Translator.get("quota_max_threads_excess_workspace"));
    }

    private void checkDuration(String projectId, long duration) {
        // 增量为0的检查
        this.checkQuota(p -> duration - 1, DURATION, projectId,
                Translator.get("quota_duration_excess_project"),
                Translator.get("quota_duration_excess_workspace"));
    }


    /**
     * 获取可用资源池集合
     * @return 资源池名称Set集合
     */
    public Set<String> getQuotaResourcePools(String  projectId) {
        Set<String> pools = new HashSet<>();
        Quota pjQuota = quotaManagementService.getProjectQuota(projectId);
        if (pjQuota != null) {
            if (isValid(pjQuota, pjQuota.getResourcePool())) {
                pools.addAll(Arrays.asList(pjQuota.getResourcePool().split(",")));
                return pools;
            }
        }

        String workspaceId = this.queryWorkspaceId(projectId);
        if (StringUtils.isBlank(workspaceId)) {
            return pools;
        }
        Quota wsQuota = quotaManagementService.getWorkspaceQuota(workspaceId);
        if (wsQuota != null) {
            if (isValid(wsQuota, wsQuota.getResourcePool())) {
                pools.addAll(Arrays.asList(wsQuota.getResourcePool().split(",")));
            }
        }
        return pools;
    }

    /**
     * 工作空间下被限制使用的资源池
     * @param workspaceId 工作空间ID
     * @return 资源池名称Set
     */
    public Set<String> getQuotaWsResourcePools(String workspaceId) {
        Set<String> pools = new HashSet<>();
        Quota wsQuota = quotaManagementService.getWorkspaceQuota(workspaceId);
        if (wsQuota != null) {
            if (isValid(wsQuota, wsQuota.getResourcePool())) {
                pools.addAll(Arrays.asList(wsQuota.getResourcePool().split(",")));
            }
        }
        return pools;
    }

    /**
     * 检查工作空间项目数量配额
     * @param workspaceId 工作空间ID
     */
    public void checkWorkspaceProject(String workspaceId) {
        this.doCheckQuota(quotaManagementService.getWorkspaceQuota(workspaceId),
                PROJECT, Translator.get("quota_project_excess_project"),
                extQuotaMapper.countWorkspaceProject(workspaceId) + 1);
    }

    /**
     * 检查vumUsed配额
     * 未超过：返回本次执行预计消耗的配额
     * 超过：抛出异常
     * @param request 压力配置
     * @param projectId 性能测试所属项目ID
     * @return 本次执行预计消耗的配额
     */
    @Transactional(noRollbackFor = MSException.class, rollbackFor = Exception.class)
    public BigDecimal checkVumUsed(TestPlanRequest request, String projectId) {
        BigDecimal toVumUsed = this.calcVum(request.getLoadConfiguration());
        if (toVumUsed.compareTo(BigDecimal.ZERO) == 0) {
            return toVumUsed;
        }

        Quota pjQuota = quotaManagementService.getProjectQuota(projectId);
        String pjWarningMsg = Translator.get("quota_vum_used_excess_project");
        boolean isContinue = this.doCheckVumUsed(pjQuota, toVumUsed, pjWarningMsg);

        if (isContinue) {
            String workspaceId = this.queryWorkspaceId(projectId);
            if (StringUtils.isBlank(workspaceId)) {
                return toVumUsed;
            }
            String wsWarningMsg = Translator.get("quota_vum_used_excess_workspace");
            Quota wsQuota = quotaManagementService.getWorkspaceQuota(workspaceId);
            if (wsQuota == null) {
                return toVumUsed;
            }
            // 获取工作空间下已经消耗的vum
            Quota qt = extQuotaMapper.getProjectQuotaSum(workspaceId);
            if (qt == null || qt.getVumUsed() == null) {
                wsQuota.setVumUsed(BigDecimal.ZERO);
            } else {
                wsQuota.setVumUsed(qt.getVumUsed());
            }
            this.doCheckVumUsed(wsQuota, toVumUsed, wsWarningMsg);
        }

        return toVumUsed;
    }

    private BigDecimal calcVum(String loadConfig) {
        BigDecimal vum = BigDecimal.ZERO;
        try {
            List jsonArray = JSON.parseArray(loadConfig);
            this.filterDeleteAndEnabled(jsonArray);
            for (Object value : jsonArray) {
                if (value instanceof List) {
                    List o = (List) value;
                    int thread = 0;
                    long duration = 0;
                    for (Object item : o) {
                        Map b = (Map) item;
                        if (StringUtils.equals((String) b.get("key"), "TargetLevel")) {
                            thread += (int) b.get("value");
                            break;
                        }
                    }
                    for (int j = 0; j < o.size(); j++) {
                        Map b = (Map) o.get(j);
                        if (StringUtils.equals((String) b.get("key"), "duration")) {
                            duration += (int) b.get("value");
                            break;
                        }
                    }
                    // 每个ThreadGroup消耗的vum单独计算
                    vum = vum.add(this.calcVum(thread, duration));
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return vum;
    }

    private BigDecimal calcVum(int thread, long duration) {
        double used = thread * duration * 1.00 / 60;
        DecimalFormat df = new DecimalFormat("#.00000");
        return new BigDecimal(df.format(used));
    }

    private boolean doCheckVumUsed(Quota quota, BigDecimal toVumUsed, String errorMsg) {
        if (quota == null) {
            return true;
        }
        // 如果vumTotal为NULL是不限制
        if (isValid(quota, quota.getVumTotal())) {
            BigDecimal vumTotal = quota.getVumTotal();
            BigDecimal used = quota.getVumUsed();
            if (used == null) {
                used = BigDecimal.ZERO;
            }
            if (used.add(toVumUsed).compareTo(vumTotal) > 0) {
                MSException.throwException(errorMsg);
            }
            return false;
        }
        return true;
    }

    /**
     * 检查向某资源添加人员时是否超额
     * @param addMemberMap 资源ID:添加用户ID列表
     * @param type 检查类型 PROJECT/WORKSPACE
     */
    public void checkMemberCount(Map<String, List<String>> addMemberMap, String type) {
        if (addMemberMap == null || addMemberMap.keySet().size() == 0) {
            return;
        }
        if (!StringUtils.equals(type, CHECK_PROJECT) && !StringUtils.equals(type, CHECK_WORKSPACE)) {
            return;
        }
        List<String> sourceIds = new ArrayList<>(addMemberMap.keySet());
        List<Quota> quotas = extQuotaMapper.listQuotaBySourceIds(sourceIds);

        Quota defaultQuota = null;
        if (StringUtils.equals(CHECK_WORKSPACE, type)) {
            defaultQuota = quotaManagementService.getDefaultQuota(QuotaConstants.DefaultType.workspace);
        }

        Map<String, Integer> quotaMap = new HashMap<>();
        for (Quota quota : quotas) {
            String key;
            if (StringUtils.equals(CHECK_PROJECT, type)) {
                key = quota.getProjectId();
            } else {
                key = quota.getWorkspaceId();
            }
            if (StringUtils.isBlank(key)) {
                continue;
            }

            if (BooleanUtils.isTrue(quota.getUseDefault())) {
                if (StringUtils.equals(CHECK_PROJECT, type)) {
                    Project project = projectMapper.selectByPrimaryKey(key);
                    if (project == null || StringUtils.isBlank(project.getWorkspaceId())) {
                        continue;
                    }
                    defaultQuota = quotaManagementService.getProjectDefaultQuota(project.getWorkspaceId());
                }
                if (defaultQuota == null) {
                    continue;
                }
                quota = defaultQuota;
            }

            if (quota.getMember() == null || quota.getMember() == 0) {
                continue;
            }
            quotaMap.put(key, quota.getMember());
        }

        Set<String> set = quotaMap.keySet();
        if (set.isEmpty()) {
            return;
        }

        Map<String, Integer> memberCountMap = this.getDBMemberCountMap(addMemberMap, type);
        Map<String, String> sourceNameMap = this.getNameMap(sourceIds, type);

        this.doCheckMemberCount(quotaMap, memberCountMap, sourceNameMap, addMemberMap, type);
    }

    private void doCheckMemberCount(Map<String, Integer> quotaMap, Map<String, Integer> memberCountMap,
                                    Map<String, String> sourceNameMap, Map<String, List<String>> addMemberMap, String checkType) {
        Set<String> set = quotaMap.keySet();
        StringBuilder builder = new StringBuilder();
        for (String sourceId : set) {
            // 没有配额限制的跳过
            if (!addMemberMap.containsKey(sourceId)) {
                continue;
            }
            // 当前已存在人员数量
            Integer dbCount = memberCountMap.getOrDefault(sourceId, 0);
            int toAddCount = addMemberMap.get(sourceId) == null ? 0 : addMemberMap.get(sourceId).size();
            // 添加人员之后判断配额
            if (dbCount + toAddCount > quotaMap.get(sourceId)) {
                builder.append(sourceNameMap.get(sourceId));
                builder.append(" ");
            }
        }
        if (builder.length() > 0) {
            builder.append("超出成员数量配额");
            if (StringUtils.equals(CHECK_WORKSPACE, checkType)) {
                builder.append("(工作空间成员配额将其下所有项目成员也计算在内)");
            }
            MSException.throwException(builder.toString());
        }
    }

    private Map<String, Integer> getDBMemberCountMap(Map<String, List<String>> addMemberMap, String type) {
        List<String> sourceIds = new ArrayList<>(addMemberMap.keySet());
        Map<String, Integer> memberCountMap = new HashMap<>();
        if (StringUtils.equals(CHECK_WORKSPACE, type)) {
            // 检查工作空间配额时将其下所有项目成员也计算在其中
            ProjectExample projectExample = new ProjectExample();
            for (String sourceId : sourceIds) {
                projectExample.clear();
                projectExample.createCriteria().andWorkspaceIdEqualTo(sourceId);
                List<Project> projects = projectMapper.selectByExample(projectExample);
                List<String> ids = projects.stream().map(Project::getId).collect(Collectors.toList());
                ids.add(sourceId);
                List<String> memberIds = addMemberMap.getOrDefault(sourceId, new ArrayList<>());
                long count = extQuotaMapper.listUserByWorkspaceAndProjectIds(ids, memberIds);
                memberCountMap.put(sourceId, (int) count);
            }
        } else if (StringUtils.equals(CHECK_PROJECT, type)) {
            List<CountDto> list = extQuotaMapper.listUserBySourceIds(sourceIds);
            memberCountMap = list.stream().collect(Collectors.toMap(CountDto::getSourceId, CountDto::getCount));
        }
        return memberCountMap;
    }


    /**
     * 更新VumUsed配额
     * @param projectId 项目ID
     * @param vumUsed 预计使用数量
     */
    @Transactional(propagation = Propagation.NESTED, rollbackFor = Exception.class)
    public void updateVumUsed(String projectId, BigDecimal vumUsed) {
        if (vumUsed == null) {
            LogUtil.info("update vum count fail. vum count is null.");
            return;
        }
        Quota dbPjQuota = extQuotaMapper.getProjectQuota(projectId);
        Quota newPjQuota = this.newPjQuota(projectId, vumUsed);
        this.doUpdateVumUsed(dbPjQuota, newPjQuota);
    }

    /**
     * 获取需要回退的vum数量
     * @param report 性能测试报告
     * @return 需要回退的vum数量
     */
    @Transactional(propagation = Propagation.NESTED, rollbackFor = Exception.class)
    public BigDecimal getReduceVumUsed(LoadTestReportWithBLOBs report) {
        String reportId = report.getId();
        List<LoadTestReportResult> timeInfos = queryReportResult(reportId, ReportKeys.TimeInfo.toString());
        List<LoadTestReportResult> overviews = queryReportResult(reportId, ReportKeys.Overview.toString());

        // 预计使用的数量
        String loadConfig = report.getLoadConfiguration();
        BigDecimal toUsed = calcVum(loadConfig);
        if (CollectionUtils.isEmpty(timeInfos) || CollectionUtils.isEmpty(overviews)) {
            LogUtil.error("reduce vum used error. load test report time info is null.");
            return toUsed;
        }

        ReportTimeInfo timeInfo = parseReportTimeInfo(timeInfos.get(0));
        TestOverview overview = parseOverview(overviews.get(0));

        long duration = timeInfo.getDuration();
        String maxUserStr = overview.getMaxUsers();
        int maxUsers = 0;
        try {
            maxUsers = Integer.parseInt(maxUserStr);
        } catch (Exception e) {
            //
        }

        if (duration == 0 || maxUsers == 0) {
            return toUsed;
        }

        // 已经使用的数量
        BigDecimal used = calcVum(maxUsers, duration);
        // 实际使用值比预计值大，不回退，否则回退差值
        return used.compareTo(toUsed) >= 0 ? BigDecimal.ZERO : toUsed.subtract(used);
    }

    /**
     * 如果有该项目配额，修改为使用工作空间下项目默认配额
     * 无该配额，新建配额并默认使用工作空间下项目默认配额
     * @return 操作后的配额
     */
    public Quota projectUseDefaultQuota(String projectId) {
        Quota pjQuota = extQuotaMapper.getProjectQuota(projectId);
        if (pjQuota != null) {
            pjQuota.setUseDefault(true);
            quotaMapper.updateByPrimaryKeySelective(pjQuota);
            return pjQuota;
        } else {
            Quota quota = new Quota();
            quota.setId(UUID.randomUUID().toString());
            quota.setUseDefault(true);
            quota.setProjectId(projectId);
            quota.setWorkspaceId(null);
            quota.setUpdateTime(System.currentTimeMillis());
            quotaMapper.insert(quota);
            return quota;
        }
    }

    /**
     * 如果有该工作空间配额，修改为使用系统默认配额，
     * 无该配额，新建配额并默认使用系统配额
     * @param workspaceId 工作空间ID
     * @return 操作后的配额
     */
    public Quota workspaceUseDefaultQuota(String workspaceId) {
        Quota wsQuota = extQuotaMapper.getWorkspaceQuota(workspaceId);
        if (wsQuota != null) {
            wsQuota.setUseDefault(true);
            quotaMapper.updateByPrimaryKeySelective(wsQuota);
            return wsQuota;
        } else {
            Quota quota = new Quota();
            quota.setId(UUID.randomUUID().toString());
            quota.setUseDefault(true);
            quota.setProjectId(null);
            quota.setWorkspaceId(workspaceId);
            quota.setUpdateTime(System.currentTimeMillis());
            quotaMapper.insert(quota);
            return quota;
        }
    }

    private List<LoadTestReportResult> queryReportResult(String id, String key) {
        return extLoadTestReportResultMapper.selectByIdAndKey(id, key);
    }

    private ReportTimeInfo parseReportTimeInfo(LoadTestReportResult reportResult) {
        String content = reportResult.getReportValue();
        ReportTimeInfo timeInfo = new ReportTimeInfo();
        try {
            timeInfo = JSON.parseObject(content, ReportTimeInfo.class);
        } catch (Exception e) {
            // 兼容字符串和数字
            Map jsonObject = JSON.parseObject(content, Map.class);
            String startTime = (String) jsonObject.get("startTime");
            String endTime = (String) jsonObject.get("endTime");
            String duration = (String) jsonObject.get("duration");

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            try {
                timeInfo.setStartTime(df.parse(startTime).getTime());
                timeInfo.setEndTime(df.parse(endTime).getTime());
                timeInfo.setDuration(Long.parseLong(duration));
            } catch (Exception parseException) {
                LogUtil.error("reduce vum error. parse time info error. " + parseException.getMessage());
            }
        }
        return timeInfo;
    }

    private TestOverview parseOverview(LoadTestReportResult reportResult) {
        String content = reportResult.getReportValue();
        TestOverview overview = new TestOverview();
        try {
            overview = JSON.parseObject(content, TestOverview.class);
        } catch (Exception e) {
            LogUtil.error("parse test overview error.", e);
        }
        return overview;
    }

    private void doUpdateVumUsed(Quota dbQuota, Quota newQuota) {
        if (dbQuota == null || StringUtils.isBlank(dbQuota.getId())) {
            quotaMapper.insert(newQuota);
        } else {
            BigDecimal vumUsed = dbQuota.getVumUsed();
            if (vumUsed == null) {
                vumUsed = BigDecimal.ZERO;
            }
            if (newQuota == null || newQuota.getVumUsed() == null) {
                return;
            }
            BigDecimal toSetVum = vumUsed.add(newQuota.getVumUsed());
            if (toSetVum.compareTo(BigDecimal.ZERO) < 0) {
                LogUtil.info("update vum used warning. vum value: " + toSetVum);
                toSetVum = BigDecimal.ZERO;
            }
            LogUtil.info("update vum used add value: " + newQuota.getVumUsed());
            dbQuota.setVumUsed(toSetVum);
            quotaMapper.updateByPrimaryKeySelective(dbQuota);
        }
    }

    private Quota newPjQuota(String projectId, BigDecimal vumUsed) {
        Quota quota = new Quota();
        quota.setId(UUID.randomUUID().toString());
        quota.setUpdateTime(System.currentTimeMillis());
        quota.setUseDefault(false);
        quota.setVumUsed(vumUsed);
        quota.setProjectId(projectId);
        quota.setWorkspaceId(null);
        return quota;
    }

    private Map<String, String> getNameMap(List<String> sourceIds, String type) {
        Map<String, String> nameMap = new HashMap<>(16);
        if (CollectionUtils.isEmpty(sourceIds)) {
            return nameMap;
        }
        if (StringUtils.equals(CHECK_PROJECT, type)) {
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andIdIn(sourceIds);
            List<Project> projects = projectMapper.selectByExample(projectExample);
            nameMap = projects.stream().collect(Collectors.toMap(Project::getId, Project::getName));
        } else if (StringUtils.equals(CHECK_WORKSPACE, type)) {
            WorkspaceExample workspaceExample = new WorkspaceExample();
            workspaceExample.createCriteria().andIdIn(sourceIds);
            List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
            nameMap = workspaces.stream().collect(Collectors.toMap(Workspace::getId, Workspace::getName));
        }
        return nameMap;
    }

    private void filterDeleteAndEnabled(List jsonArray) {
        Iterator<Object> iterator = jsonArray.iterator();
        outer:
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof List<?>) {
                List<?> o = (List<?>) next;
                for (Object o1 : o) {
                    Map jsonObject = (Map) o1;
                    if (StringUtils.equals((String) jsonObject.get("key"), "deleted")) {
                        String value = (String) jsonObject.get("value");
                        if (StringUtils.equals(value, "true")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
                for (Object o1 : o) {
                    Map jsonObject = (Map) o1;
                    if (StringUtils.equals((String) jsonObject.get("key"), "enabled")) {
                        String value = (String) jsonObject.get("value");
                        if (StringUtils.equals(value, "false")) {
                            iterator.remove();
                            continue outer;
                        }
                    }
                }
            }
        }
    }

    private int getIntegerValue(String loadConfiguration, String key) {
        int s = 0;
        try {
            List jsonArray = JSON.parseArray(loadConfiguration);
            this.filterDeleteAndEnabled(jsonArray);
            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i) instanceof List) {
                    List o = (List) jsonArray.get(i);
                    for (int j = 0; j < o.size(); j++) {
                        Map b = (Map) o.get(j);
                        if (StringUtils.equals((String) b.get("key"), key)) {
                            s += (int) b.get("value");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("get load configuration integer value error.");
            LogUtil.error(e.getMessage(), e);
        }
        return s;
    }

}
