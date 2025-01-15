package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.domain.ApiReportRelateTask;
import io.metersphere.api.domain.ApiReportRelateTaskExample;
import io.metersphere.api.mapper.ApiReportRelateTaskMapper;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.MsHttpClient;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.BatchExecTaskReportDTO;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.dto.request.BatchExecTaskPageRequest;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskhub.*;
import io.metersphere.system.dto.taskhub.request.ScheduleRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemBatchRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.dto.taskhub.response.TaskStatisticsResponse;
import io.metersphere.system.invoker.TaskRerunServiceInvoker;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.*;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.schedule.ApiScheduleNoticeService;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.TaskRunnerClient;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTaskHubService {

    @Resource
    private ExtExecTaskMapper extExecTaskMapper;
    @Resource
    private ExtScheduleMapper extScheduleMapper;
    @Resource
    ExtOrganizationMapper extOrganizationMapper;
    @Resource
    UserLoginService userLoginService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtExecTaskItemMapper extExecTaskItemMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourcePoolBlobMapper testResourcePoolBlobMapper;
    @Resource
    private TestResourcePoolOrganizationMapper testResourcePoolOrganizationMapper;
    @Resource
    private ExtResourcePoolMapper extResourcePoolMapper;
    @Resource
    private NodeResourcePoolService nodeResourcePoolService;
    @Resource
    private ExecTaskMapper execTaskMapper;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TestResourcePoolService testResourcePoolService;
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;
    @Resource
    private ExecTaskService execTaskService;

    private final static String GET_TASK_ITEM_ORDER_URL = "http://%s/api/task/item/order";

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ExtSwaggerMapper extSwaggerMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    ApiScheduleNoticeService apiScheduleNoticeService;
    @Resource
    private UserToolService userToolService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;


    /**
     * 系统-获取执行任务列表
     *
     * @param request
     * @param orgId
     * @param projectId
     * @return
     */
    public Pager<List<TaskHubDTO>> getTaskList(BasePageRequest request, String orgId, String projectId) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, getPage(request, orgId, projectId));
    }

    private List<TaskHubDTO> getPage(BasePageRequest request, String orgId, String projectId) {
        List<TaskHubDTO> list = extExecTaskMapper.selectList(request, orgId, projectId);
        handleList(list);
        return list;
    }

    private void handleList(List<TaskHubDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> projectIds = list.stream().map(TaskHubDTO::getProjectId).distinct().toList();
        List<String> organizationIds = list.stream().map(TaskHubDTO::getOrganizationId).distinct().toList();
        List<String> userIds = list.stream().map(TaskHubDTO::getCreateUser).distinct().toList();
        Map<String, String> projectMaps = getProjectMaps(projectIds);
        Map<String, String> organizationMaps = getOrganizationMaps(organizationIds);
        Map<String, String> userMaps = getUserMaps(userIds);
        list.forEach(item -> {
            item.setProjectName(projectMaps.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
            item.setOrganizationName(organizationMaps.getOrDefault(item.getOrganizationId(), StringUtils.EMPTY));
            item.setCreateUserName(userMaps.getOrDefault(item.getCreateUser(), StringUtils.EMPTY));
        });
        setTaskReportId(list);
    }

    private Map<String, String> getUserMaps(List<String> userIds) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> userList = userMapper.selectByExample(userExample);
        return userList.stream().collect(Collectors.toMap(User::getId, User::getName));
    }

    private Map<String, String> getOrganizationMaps(List<String> organizationIds) {
        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(organizationIds);
        List<Organization> organizationList = organizationMapper.selectByExample(organizationExample);
        return organizationList.stream().collect(Collectors.toMap(Organization::getId, Organization::getName));
    }

    private Map<String, String> getProjectMaps(List<String> projectIds) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projectList = projectMapper.selectByExample(projectExample);
        return projectList.stream().collect(Collectors.toMap(Project::getId, Project::getName));
    }

    /**
     * 设置任务的报告ID
     *
     * @param tasks 任务集合
     */
    private void setTaskReportId(List<TaskHubDTO> tasks) {
        // 集成报告, 独立报告(非批量任务&&非测试计划批量任务)
        List<TaskHubDTO> reportTasks = tasks.stream().filter(task -> task.getIntegrated() || (!StringUtils.equals(task.getTaskType(), ExecTaskType.API_SCENARIO_BATCH.name()) && !StringUtils.equals(task.getTaskType(), ExecTaskType.API_CASE_BATCH.name())
                && !StringUtils.equals(task.getTaskType(), ExecTaskType.TEST_PLAN_API_CASE_BATCH.name()) && !StringUtils.equals(task.getTaskType(), ExecTaskType.TEST_PLAN_API_SCENARIO_BATCH.name()))).toList();
        List<String> integratedTaskIds = reportTasks.stream().filter(task ->
                StringUtils.equalsAny(task.getTaskType(), ExecTaskType.TEST_PLAN.name(), ExecTaskType.TEST_PLAN_GROUP.name()) || task.getIntegrated()).map(ExecTask::getId).toList();
        List<String> noIntegratedTasks = reportTasks.stream().map(ExecTask::getId).filter(id -> !integratedTaskIds.contains(id)).toList();
        List<ExecTaskItem> taskItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(noIntegratedTasks)) {
            ExecTaskItemExample itemExample = new ExecTaskItemExample();
            itemExample.createCriteria()
                    .andTaskIdIn(noIntegratedTasks)
                    .andDeletedEqualTo(false);
            taskItems = execTaskItemMapper.selectByExample(itemExample);
        }
        Map<String, String> taskItemMap = taskItems.stream().collect(Collectors.toMap(ExecTaskItem::getTaskId, ExecTaskItem::getId));
        List<String> noIntegratedTaskItemIds = taskItems.stream().map(ExecTaskItem::getId).toList();
        List<String> resourceIds = ListUtils.union(integratedTaskIds, noIntegratedTaskItemIds);
        if (CollectionUtils.isEmpty(resourceIds)) {
            return;
        }
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdIn(resourceIds);
        List<ApiReportRelateTask> reportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
        Map<String, String> reportMap = new HashMap<>();
        reportRelateTasks.forEach(item -> reportMap.put(item.getTaskResourceId(), item.getReportId()));
        reportTasks.forEach(task -> {
            if (integratedTaskIds.contains(task.getId())) {
                task.setReportId(reportMap.get(task.getId()));
                task.setResultDeleted(false);
            } else {
                if (taskItemMap.containsKey(task.getId())) {
                    task.setReportId(reportMap.get(taskItemMap.get(task.getId())));
                    task.setResultDeleted(false);
                }
            }
        });
    }


    /**
     * 系统-获取后台执行任务列表
     *
     * @param request
     * @param projectIds
     * @return
     */
    public Pager<List<TaskHubScheduleDTO>> getScheduleTaskList(BasePageRequest request, List<String> projectIds) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, getSchedulePage(request, projectIds));
    }

    private List<TaskHubScheduleDTO> getSchedulePage(BasePageRequest request, List<String> projectIds) {
        List<TaskHubScheduleDTO> list = extScheduleMapper.selectScheduleList(request, projectIds);
        processTaskCenterSchedule(list, projectIds);
        return list;
    }

    private void processTaskCenterSchedule(List<TaskHubScheduleDTO> list, List<String> projectIds) {
        if (CollectionUtils.isNotEmpty(list)) {
            if (CollectionUtils.isEmpty(projectIds)) {
                projectIds = list.stream().map(TaskHubScheduleDTO::getProjectId).collect(Collectors.toList());
            }
            // 组织
            List<OptionDTO> orgListByProjectList = getOrgListByProjectIds(projectIds);
            Map<String, String> orgMap = orgListByProjectList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
            // 取所有的userid
            Set<String> userSet = list.stream()
                    .flatMap(item -> Stream.of(item.getCreateUserName()))
                    .collect(Collectors.toSet());
            Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
            for (TaskHubScheduleDTO item : list) {
                item.setCreateUserName(userMap.getOrDefault(item.getCreateUserName(), StringUtils.EMPTY));
                item.setOrganizationName(orgMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
            }
        }

    }

    private List<OptionDTO> getOrgListByProjectIds(List<String> projectIds) {
        return extOrganizationMapper.getOrgListByProjectIds(projectIds);
    }

    /**
     * 单任务详情数据入库接口
     *
     * @param item
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertExecTaskAndDetail(ExecTask task, ExecTaskItem item) {
        execTaskMapper.insertSelective(task);
        execTaskItemMapper.insertSelective(item);
    }

    /**
     * 单任务详情数据入库接口
     *
     * @param task
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertExecTask(ExecTask task) {
        execTaskMapper.insertSelective(task);
    }

    /**
     * 单任务详情数据入库接口
     *
     * @param items
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertExecTaskDetail(List<ExecTaskItem> items) {
        insertExecTaskAndDetail(List.of(), items);
    }

    /**
     * 单任务详情数据入库接口
     *
     * @param items
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertExecTaskAndDetail(ExecTask task, List<ExecTaskItem> items) {
        insertExecTaskAndDetail(List.of(task), items);
    }


    /**
     * 批量任务&任务详情入库接口
     *
     * @param tasks
     * @param items
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertExecTaskAndDetail(List<ExecTask> tasks, List<ExecTaskItem> items) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        if (CollectionUtils.isNotEmpty(tasks)) {
            ExecTaskMapper execTaskMapper = sqlSession.getMapper(ExecTaskMapper.class);
            SubListUtils.dealForSubList(tasks, 1000, subList -> {
                subList.forEach(execTaskMapper::insertSelective);
            });
        }

        if (CollectionUtils.isNotEmpty(items)) {
            ExecTaskItemMapper itemMapper = sqlSession.getMapper(ExecTaskItemMapper.class);
            SubListUtils.dealForSubList(items, 1000, subList -> {
                subList.forEach(itemMapper::insertSelective);
            });
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }


    /**
     * 用例任务详情列表查询
     *
     * @param request
     * @return
     */
    public Pager<List<TaskHubItemDTO>> getCaseTaskItemList(TaskHubItemRequest request, String orgId, String projectId) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, getCaseTaskItemPage(request, orgId, projectId));
    }

    private List<TaskHubItemDTO> getCaseTaskItemPage(TaskHubItemRequest request, String orgId, String projectId) {
        List<TaskHubItemDTO> itemDTOS = extExecTaskItemMapper.selectList(request, orgId, projectId);
        handleTaskItem(itemDTOS);
        return itemDTOS;
    }

    private void handleTaskItem(List<TaskHubItemDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> userIds = list.stream().map(TaskHubItemDTO::getExecutor).distinct().toList();
        List<String> resourcePoolIds = list.stream().map(TaskHubItemDTO::getResourcePoolId).distinct().toList();
        List<String> projectIds = list.stream().map(TaskHubItemDTO::getProjectId).distinct().toList();
        List<String> organizationIds = list.stream().map(TaskHubItemDTO::getOrganizationId).distinct().toList();
        Map<String, String> projectMaps = getProjectMaps(projectIds);
        Map<String, String> organizationMaps = getOrganizationMaps(organizationIds);
        Map<String, String> userMaps = getUserMaps(userIds);
        Map<String, String> resourcePoolMaps = getResourcePoolMaps(resourcePoolIds);
        list.forEach(item -> {
            item.setUserName(userMaps.getOrDefault(item.getExecutor(), StringUtils.EMPTY));
            item.setResourcePoolName(resourcePoolMaps.getOrDefault(item.getResourcePoolId(), StringUtils.EMPTY));
            item.setProjectName(projectMaps.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
            item.setOrganizationName(organizationMaps.getOrDefault(item.getOrganizationId(), StringUtils.EMPTY));
            if (StringUtils.isNotBlank(item.getErrorMessage())) {
                item.setErrorMessage(Translator.get("task_error_message." + item.getErrorMessage().toLowerCase()));
            }
        });
        handleResultFlag(list);
    }

    /**
     * 处理执行结果是否被删除标识
     *
     * @param list
     */
    private void handleResultFlag(List<TaskHubItemDTO> list) {
        List<String> integratedTaskIds = list.stream().filter(item -> BooleanUtils.isTrue(item.getIntegrated())).map(TaskHubItemDTO::getTaskId).toList();
        List<String> noIntegratedTaskItemIds = list.stream().filter(item -> BooleanUtils.isFalse(item.getIntegrated())).map(TaskHubItemDTO::getId).toList();
        List<String> resourceIds = ListUtils.union(integratedTaskIds, noIntegratedTaskItemIds);
        if (CollectionUtils.isEmpty(resourceIds)) {
            return;
        }
        ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
        example.createCriteria().andTaskResourceIdIn(resourceIds);
        List<ApiReportRelateTask> reportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
        Map<String, String> reportMap = reportRelateTasks.stream().collect(Collectors.toMap(ApiReportRelateTask::getTaskResourceId, ApiReportRelateTask::getReportId));
        list.forEach(task -> {
            if (BooleanUtils.isTrue(task.getIntegrated()) && reportMap.containsKey(task.getTaskId())) {
                task.setResultDeleted(false);
            }
            if (BooleanUtils.isFalse(task.getIntegrated()) && reportMap.containsKey(task.getId())) {
                task.setResultDeleted(false);
            }
        });

    }

    private Map<String, String> getResourcePoolMaps(List<String> resourcePoolIds) {
        TestResourcePoolExample poolExample = new TestResourcePoolExample();
        poolExample.createCriteria().andIdIn(resourcePoolIds);
        List<TestResourcePool> poolList = testResourcePoolMapper.selectByExample(poolExample);
        return poolList.stream().collect(Collectors.toMap(TestResourcePool::getId, TestResourcePool::getName));
    }

    /**
     * 计算任务通过率和执行进度
     *
     * @param taskIds
     * @param orgId
     * @param projectId
     * @return
     */
    public List<TaskStatisticsResponse> calculateRate(List<String> taskIds, String orgId, String projectId) {
        List<TaskStatisticsResponse> responseList = new ArrayList<>();
        List<ExecTaskItem> taskItemList = extExecTaskItemMapper.selectItemByTaskIds(taskIds, orgId, projectId);
        Map<String, List<ExecTaskItem>> taskItems = taskItemList.stream().collect(Collectors.groupingBy(ExecTaskItem::getTaskId));
        taskItems.forEach((taskId, items) -> {
            //成功数量
            long successCount = items.stream().filter(item -> StringUtils.equalsIgnoreCase(ResultStatus.SUCCESS.name(), item.getResult())).count();
            //失败数量
            long errorCount = items.stream().filter(item -> StringUtils.equalsIgnoreCase(ResultStatus.ERROR.name(), item.getResult())).count();
            //误报数量
            long fakeErrorCount = items.stream().filter(item -> StringUtils.equalsIgnoreCase(ResultStatus.FAKE_ERROR.name(), item.getResult())).count();
            //未执行数量
            long pendingCount = items.stream().filter(item -> StringUtils.equalsIgnoreCase(ExecStatus.PENDING.name(), item.getResult())).count();

            TaskStatisticsResponse response = new TaskStatisticsResponse();
            response.setId(taskId);
            response.setCaseTotal(items.size());
            response.setSuccessCount(successCount);
            response.setErrorCount(errorCount);
            response.setFakeErrorCount(fakeErrorCount);
            response.setPendingCount(pendingCount);
            response.calculateExecuteRate();
            responseList.add(response);
        });
        return responseList;
    }


    /**
     * 获取所有资源池及节点下拉选项
     *
     * @return
     */
    public List<ResourcePoolOptionsDTO> getResourcePoolOptions() {
        //获取全部资源池
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andDeletedEqualTo(false);
        List<TestResourcePool> allResourcePools = testResourcePoolMapper.selectByExample(example);
        Map<String, List<TestResourcePoolBlob>> poolMap = getPoolMap(allResourcePools);
        return handleOptions(allResourcePools, poolMap);

    }

    public Map<String, List<TestResourcePoolBlob>> getPoolMap(List<TestResourcePool> allResourcePools) {
        List<String> ids = allResourcePools.stream().map(TestResourcePool::getId).toList();
        //获取全部资源池节点
        TestResourcePoolBlobExample blobExample = new TestResourcePoolBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        List<TestResourcePoolBlob> testResourcePoolBlobs = testResourcePoolBlobMapper.selectByExampleWithBLOBs(blobExample);
        return testResourcePoolBlobs.stream().collect(Collectors.groupingBy(TestResourcePoolBlob::getId));
    }

    public List<ResourcePoolOptionsDTO> handleOptions(List<TestResourcePool> allResourcePools, Map<String, List<TestResourcePoolBlob>> poolMap) {
        List<ResourcePoolOptionsDTO> options = new ArrayList<>();
        allResourcePools.forEach(item -> {
            ResourcePoolOptionsDTO optionsDTO = new ResourcePoolOptionsDTO();
            optionsDTO.setId(item.getId());
            optionsDTO.setName(item.getName());
            if (poolMap.containsKey(item.getId())) {
                TestResourcePoolBlob first = poolMap.get(item.getId()).getFirst();
                TestResourceDTO testResourceDTO = JSON.parseObject(new String(first.getConfiguration()), TestResourceDTO.class);
                List<OptionDTO> children = new ArrayList<>();
                testResourceDTO.getNodesList().forEach(node -> {
                    OptionDTO childrenDTO = new OptionDTO();
                    childrenDTO.setId(node.getIp().concat(":").concat(node.getPort()));
                    childrenDTO.setName(node.getIp().concat(":").concat(node.getPort()));
                    children.add(childrenDTO);
                });
                optionsDTO.setChildren(children);
            }
            options.add(optionsDTO);
        });
        return options;
    }


    /**
     * 获取组织下的资源池及节点下拉选项
     *
     * @param orgId
     * @return
     */
    public List<ResourcePoolOptionsDTO> getOrgResourcePoolOptions(String orgId) {
        TestResourcePoolOrganizationExample example = new TestResourcePoolOrganizationExample();
        example.createCriteria().andOrgIdEqualTo(orgId);
        List<TestResourcePoolOrganization> orgPools = testResourcePoolOrganizationMapper.selectByExample(example);
        List<String> poolIds = orgPools.stream().map(TestResourcePoolOrganization::getTestResourcePoolId).toList();
        List<TestResourcePool> allResourcePools = extResourcePoolMapper.selectAllResourcePool(poolIds);
        if (CollectionUtils.isEmpty(allResourcePools)) {
            return null;
        }
        Map<String, List<TestResourcePoolBlob>> poolMap = getPoolMap(allResourcePools);
        return handleOptions(allResourcePools, poolMap);
    }

    /**
     * 获取任务详情列表资源节点状态
     *
     * @param ids
     * @return
     */
    public List<ResourcePoolStatusDTO> getResourcePoolStatus(List<String> ids) {
        List<ExecTaskItem> items = extExecTaskItemMapper.selectPoolNodeByIds(ids);

        return items.stream()
                .filter(item -> StringUtils.isNotBlank(item.getResourcePoolNode()))
                .collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode))
                .entrySet()
                .stream()
                .map(entry -> {
                    String key = entry.getKey();
                    List<ExecTaskItem> itemGroup = entry.getValue();

                    // Asynchronously determine the status
                    CompletableFuture<Boolean> statusFuture = CompletableFuture.supplyAsync(() -> determineStatus(key, itemGroup));

                    return statusFuture.thenApply(status -> itemGroup.stream()
                            .map(item -> new ResourcePoolStatusDTO(item.getId(), status))
                            .collect(Collectors.toList()));
                })
                .toList()
                .stream()
                .flatMap(future -> future.join().stream()) // Wait for all futures to complete
                .collect(Collectors.toList());
    }

    /**
     * Determine the status of a resource pool node
     */
    private boolean determineStatus(String key, List<ExecTaskItem> items) {
        try {
            String[] split = key.split(":");
            if (split.length == 2) {
                var node = new TestResourceNodeDTO();
                node.setIp(split[0]);
                node.setPort(split[1]);
                return nodeResourcePoolService.validateNode(node);
            } else if (!items.isEmpty()) {
                var testResourceDTO = new TestResourceDTO();
                var returnDTO = testResourcePoolService.getTestResourcePoolDetail(items.getFirst().getResourcePoolId());
                BeanUtils.copyBean(testResourceDTO, returnDTO.getTestResourceReturnDTO());
                testResourceDTO.setDeployName(key);
                return EngineFactory.validateNamespaceExists(testResourceDTO);
            }
        } catch (Exception e) {
            // Log the exception if needed
        }
        return false;
    }


    /**
     * 停止任务
     *
     * @param id
     * @param userId
     * @param orgId
     * @param projectId
     */
    public void stopTask(String id, String userId, String orgId, String projectId) {
        //1.更新任务状态
        extExecTaskMapper.batchUpdateTaskStatus(List.of(id), userId, orgId, projectId, ExecStatus.STOPPED.name());

        //2.更新任务明细状态
        extExecTaskItemMapper.batchUpdateTaskItemStatus(List.of(id), userId, orgId, projectId, ExecStatus.STOPPED.name());
        handleStopTaskAsync(List.of(id));
    }

    public void rerunTask(String id, String userId, String orgId, String projectId) {
        ExecTask execTask = execTaskMapper.selectByPrimaryKey(id);
        if (projectId != null && !StringUtils.equals(projectId, execTask.getProjectId())) {
            throw new MSException(Translator.get("no_permission_to_resource"));
        }
        if (orgId != null && !StringUtils.equals(orgId, execTask.getOrganizationId())) {
            throw new MSException(Translator.get("no_permission_to_resource"));
        }

        execTaskService.updateTaskRerunStatus(execTask, userId);

        Thread.startVirtualThread(() -> TaskRerunServiceInvoker.rerun(execTask, userId));
    }

    private void handleStopTaskAsync(List<String> ids) {
        Thread.startVirtualThread(() -> handleStopTask(ids));
    }

    private void handleStopTask(List<String> ids) {
        //获取详情资源池
        List<ExecTaskItem> list = extExecTaskItemMapper.getResourcePoolsByTaskIds(ids);
        Map<String, List<ExecTaskItem>> resourcePoolMaps = list.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolId));
        resourcePoolMaps.forEach((k, v) -> {
            try {
                //判断资源池类型
                TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(k);
                boolean isK8SResourcePool = StringUtils.equals(testResourcePoolDTO.getType(), ResourcePoolTypeEnum.K8S.getName());
                if (isK8SResourcePool) {
                    List<String> taskIds = list.stream().map(ExecTaskItem::getTaskId).distinct().toList();
                    //K8S
                    handleK8STask(taskIds, testResourcePoolDTO);
                } else {
                    Map<String, List<ExecTaskItem>> nodeItem = list.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode));
                    handleNodeTask(nodeItem, false);
                }
            } catch (Exception e) {
                LogUtils.error(e);
            }
        });
    }

    private void handleNodeTask(Map<String, List<ExecTaskItem>> nodeItem, boolean isItem) {
        //通过任务向节点发起停止
        nodeItem.forEach((node, items) -> {
            String endpoint = "http://".concat(node);
            List<String> itemIds = new ArrayList<>();
            if (isItem) {
                itemIds = items.stream().map(ExecTaskItem::getId).toList();
            } else {
                itemIds = items.stream().map(ExecTaskItem::getTaskId).toList();
            }
            SubListUtils.dealForSubList(itemIds, 100, subList -> {
                try {
                    LogUtils.info(String.format("开始发送停止请求到 %s 节点执行", endpoint), subList.toString());
                    if (isItem) {
                        MsHttpClient.stopApiTaskItem(endpoint, subList);
                    } else {
                        MsHttpClient.stopApiTask(endpoint, subList);
                    }
                } catch (Exception e) {
                }
            });
        });
    }

    private void handleK8STask(List<String> taskIds, TestResourcePoolReturnDTO testResourcePoolDTO) {
        SubListUtils.dealForSubList(taskIds, 100, subList -> {
            try {
                TestResourceDTO testResourceDTO = new TestResourceDTO();
                BeanUtils.copyBean(testResourceDTO, testResourcePoolDTO.getTestResourceReturnDTO());
                EngineFactory.stopApiTask(subList, testResourceDTO);
            } catch (Exception e) {
                LogUtils.error(e);
            }
        });
    }

    public void deleteTask(String id, String orgId, String projectId) {
        //1.删除任务
        extExecTaskMapper.deleteTaskByIds(List.of(id), orgId, projectId);
        //2.删除任务明细
        ExecTaskItemExample itemExample = new ExecTaskItemExample();
        itemExample.createCriteria().andTaskIdEqualTo(id);
        ExecTaskItem taskItem = new ExecTaskItem();
        taskItem.setDeleted(true);
        execTaskItemMapper.updateByExampleSelective(taskItem, itemExample);
        handleStopTaskAsync(List.of(id));
    }

    public void batchStopTask(List<String> ids, String userId, String orgId, String projectId) {
        if (CollectionUtils.isNotEmpty(ids)) {
            //1.更新任务状态
            extExecTaskMapper.batchUpdateTaskStatus(ids, userId, orgId, projectId, ExecStatus.STOPPED.name());
            //2.更新任务明细状态
            extExecTaskItemMapper.batchUpdateTaskItemStatus(ids, userId, orgId, projectId, ExecStatus.STOPPED.name());
            handleStopTaskAsync(ids);
        }

    }

    public List<String> getTaskIds(TableBatchProcessDTO request, String orgId, String projectId, boolean flag) {
        if (request.isSelectAll()) {
            List<String> ids = extExecTaskMapper.getIds(request, orgId, projectId, flag);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return extExecTaskMapper.getSelectIds(request.getSelectIds(), flag);
        }
    }

    public void batchDeleteTask(List<String> ids, String orgId, String projectId) {
        if (CollectionUtils.isNotEmpty(ids)) {
            //1.删除任务
            extExecTaskMapper.deleteTaskByIds(ids, orgId, projectId);
            //2.删除任务明细
            ExecTaskItemExample itemExample = new ExecTaskItemExample();
            itemExample.createCriteria().andTaskIdIn(ids);
            ExecTaskItem taskItem = new ExecTaskItem();
            taskItem.setDeleted(true);
            execTaskItemMapper.updateByExampleSelective(taskItem, itemExample);
            handleStopTaskAsync(ids);
        }
    }

    /**
     * 停止任务项
     *
     * @param id
     * @param userId
     * @param orgId
     * @param projectId
     */
    public void stopTaskItem(String id, String userId, String orgId, String projectId) {
        //1.更新任务明细状态
        extExecTaskItemMapper.batchUpdateTaskItemStatusByIds(List.of(id), userId, orgId, projectId, ExecStatus.STOPPED.name());
        //2.通过任务项id停止任务
        handleStopTaskItemAsync(List.of(id));
    }

    private void handleStopTaskItemAsync(List<String> ids) {
        Thread.startVirtualThread(() -> handleStopTaskItem(ids));
    }

    private void handleStopTaskItem(List<String> ids) {
        List<ExecTaskItem> execTaskItems = extExecTaskItemMapper.getResourcePoolsByItemIds(ids);
        Map<String, List<ExecTaskItem>> resourcePoolMaps = execTaskItems.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolId));
        resourcePoolMaps.forEach((k, v) -> {
            try {
                //判断资源池类型
                TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(k);
                boolean isK8SResourcePool = StringUtils.equals(testResourcePoolDTO.getType(), ResourcePoolTypeEnum.K8S.getName());
                if (isK8SResourcePool) {
                    List<String> itemIds = v.stream().map(ExecTaskItem::getId).toList();
                    //K8S 通过任务项id停止任务项
                    handleK8STaskItem(itemIds, testResourcePoolDTO);
                } else {
                    Map<String, List<ExecTaskItem>> nodeItem = execTaskItems.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode));
                    handleNodeTask(nodeItem, true);
                }
            } catch (Exception e) {
                LogUtils.error(e);
            }
        });
    }

    private void handleK8STaskItem(List<String> itemIds, TestResourcePoolReturnDTO testResourcePoolDTO) {
        SubListUtils.dealForSubList(itemIds, 100, subList -> {
            try {
                TestResourceDTO testResourceDTO = new TestResourceDTO();
                BeanUtils.copyBean(testResourceDTO, testResourcePoolDTO.getTestResourceReturnDTO());
                EngineFactory.stopApiTaskItem(subList, testResourceDTO);
            } catch (Exception e) {
                LogUtils.error(e);
            }
        });
    }

    public void batchStopTaskItem(List<String> itemIds, String userId, String orgId, String projectId) {
        if (CollectionUtils.isNotEmpty(itemIds)) {
            //1.更新任务明细状态
            extExecTaskItemMapper.batchUpdateTaskItemStatusByIds(itemIds, userId, orgId, projectId, ExecStatus.STOPPED.name());
            //2.通过任务项id停止任务
            handleStopTaskItemAsync(itemIds);
        }

    }


    public List<String> getTaskItemIds(TaskHubItemBatchRequest request, String orgId, String projectId) {
        if (request.isSelectAll()) {
            List<String> ids = extExecTaskItemMapper.getIds(request, orgId, projectId);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public Map<String, Integer> getTaskItemOrder(List<String> taskIdItemIds) {
        List<ExecTaskItem> taskItemIds = getTaskItemByIds(taskIdItemIds);
        Map<String, List<ExecTaskItem>> nodeResourceMap = taskItemIds.stream()
                .filter(item -> StringUtils.contains(item.getResourcePoolNode(), ":")) // 根据条件过滤
                .collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode));

        List<CompletableFuture<Map<String, Integer>>> futures = nodeResourceMap.keySet().stream()
                .filter(StringUtils::isNotBlank)
                .map(execTaskItems -> CompletableFuture.supplyAsync(() -> getTaskItemOrder(execTaskItems, taskIdItemIds)))
                .toList();

        // 等待所有异步任务完成并合并结果
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Integer> getTaskItemOrder(String node, List<String> taskIdItemIds) {
        try {
            ResultHolder body = TaskRunnerClient.post(String.format(GET_TASK_ITEM_ORDER_URL, node), taskIdItemIds);
            return JSON.parseMap(JSON.toJSONString(body.getData()));
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
        return Map.of();
    }

    private List<ExecTaskItem> getTaskItemByIds(List<String> taskIdItemIds) {
        ExecTaskItemExample itemExample = new ExecTaskItemExample();
        itemExample.createCriteria().andIdIn(taskIdItemIds);
        return execTaskItemMapper.selectByExample(itemExample);
    }

    public void deleteScheduleTask(String id, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        if (!StringUtils.equalsAnyIgnoreCase(schedule.getResourceType(), ScheduleResourceType.BUG_SYNC.name(), ScheduleResourceType.DEMAND_SYNC.name())) {
            if (StringUtils.equals(schedule.getResourceType(), ScheduleTagType.API_IMPORT.name())) {
                extSwaggerMapper.deleteByPrimaryKey(schedule.getResourceId());
            }
            scheduleService.deleteByResourceId(schedule.getResourceId(), schedule.getJob());
            saveLog(List.of(schedule), userId, path, HttpMethodConstants.GET.name(), module, OperationLogType.DELETE.name());
        }
    }

    private void saveLog(List<Schedule> scheduleList, String userId, String path, String method, String module, String type) {
        //取出所有的项目id
        if (scheduleList.isEmpty()) {
            return;
        }
        List<String> projectIds = scheduleList.stream().map(Schedule::getProjectId).distinct().toList();
        //根据项目id取出组织id
        List<ProjectDTO> orgList = extScheduleMapper.getOrgListByProjectIds(projectIds);
        //生成map key:项目id value:组织id
        Map<String, String> orgMap = orgList.stream().collect(Collectors.toMap(ProjectDTO::getId, ProjectDTO::getOrganizationId));
        List<LogDTO> logs = new ArrayList<>();
        scheduleList.forEach(s -> {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(s.getProjectId())
                    .organizationId(orgMap.get(s.getProjectId()))
                    .type(type)
                    .module(module)
                    .method(method)
                    .path(path)
                    .sourceId(s.getResourceId())
                    .content(s.getName())
                    .createUser(userId)
                    .build().getLogDTO();
            logs.add(dto);
        });
        operationLogService.batchAdd(logs);
    }


    public Schedule checkScheduleExit(String id) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(id);
        if (schedule == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return schedule;
    }

    public void enable(String id, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        schedule.setEnable(!schedule.getEnable());
        scheduleService.editSchedule(schedule);
        try {
            scheduleService.addOrUpdateCronJob(schedule, new JobKey(schedule.getKey(), schedule.getJob()),
                    new TriggerKey(schedule.getKey(), schedule.getJob()), Class.forName(schedule.getJob()));
        } catch (ClassNotFoundException e) {
            LogUtils.error(e);
            throw new RuntimeException(e);
        }
        apiScheduleNoticeService.sendScheduleNotice(schedule, userId);
        saveLog(List.of(schedule), userId, path, HttpMethodConstants.GET.name(), module, OperationLogType.UPDATE.name());
    }


    public void scheduleBatchOperation(TableBatchProcessDTO request, String userId, String projectId, String path, String module, boolean enable, List<String> projectIds) {
        List<Schedule> scheduleList = getSchedule(request, projectIds, enable);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ScheduleMapper batchMapper = sqlSession.getMapper(ScheduleMapper.class);
        SubListUtils.dealForSubList(scheduleList, 100, list -> {
            list.forEach(s -> {
                s.setEnable(enable);
                batchMapper.updateByPrimaryKeySelective(s);
                try {
                    scheduleService.addOrUpdateCronJob(s, new JobKey(s.getKey(), s.getJob()),
                            new TriggerKey(s.getKey(), s.getJob()), Class.forName(s.getJob()));
                } catch (ClassNotFoundException e) {
                    LogUtils.error(e);
                    throw new RuntimeException(e);
                }
            });
            sqlSession.flushStatements();
        });
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        apiScheduleNoticeService.batchSendNotice(projectId, scheduleList, userMapper.selectByPrimaryKey(userId), enable ? NoticeConstants.Event.OPEN : NoticeConstants.Event.CLOSE);
        saveLog(scheduleList, userId, path, HttpMethodConstants.POST.name(), module, OperationLogType.UPDATE.name());
    }

    private List<Schedule> getSchedule(TableBatchProcessDTO request, List<String> projectIds, boolean enable) {
        List<Schedule> list;
        if (request.isSelectAll()) {
            list = extScheduleMapper.getSchedules(request, projectIds);
        } else {
            ScheduleExample example = new ScheduleExample();
            example.createCriteria().andIdIn(request.getSelectIds());
            list = scheduleMapper.selectByExample(example);
        }
        list = list.stream().filter(s -> s.getEnable() != enable).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
            list.removeIf(schedule -> request.getExcludeIds().contains(schedule.getId()));
        }
        return list;
    }

    public void updateCron(ScheduleRequest request, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(request.getId());
        schedule.setValue(request.getCron());
        scheduleService.editSchedule(schedule);
        try {
            handleThirdSchedule(schedule, request.getCron());
            scheduleService.addOrUpdateCronJob(schedule, new JobKey(schedule.getKey(), schedule.getJob()),
                    new TriggerKey(schedule.getKey(), schedule.getJob()), Class.forName(schedule.getJob()));
            saveLog(List.of(schedule), userId, path, HttpMethodConstants.GET.name(), module, OperationLogType.UPDATE.name());
        } catch (ClassNotFoundException e) {
            LogUtils.error(e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 处理三方同步任务
     *
     * @param schedule
     */
    private void handleThirdSchedule(Schedule schedule, String cron) {
        if (StringUtils.equalsAnyIgnoreCase(schedule.getResourceType(), ScheduleResourceType.BUG_SYNC.name(), ScheduleResourceType.DEMAND_SYNC.name())) {
            ProjectApplicationExample example = new ProjectApplicationExample();
            ProjectApplicationExample.Criteria criteria = example.createCriteria();
            criteria.andProjectIdEqualTo(schedule.getProjectId());
            if (StringUtils.equalsIgnoreCase(schedule.getResourceType(), ScheduleResourceType.BUG_SYNC.name())) {
                criteria.andTypeEqualTo(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.CRON_EXPRESSION.name());
            }
            if (StringUtils.equalsIgnoreCase(schedule.getResourceType(), ScheduleResourceType.DEMAND_SYNC.name())) {
                criteria.andTypeEqualTo(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CRON_EXPRESSION.name());
            }
            ProjectApplication projectApplication = new ProjectApplication();
            projectApplication.setTypeValue(cron);
            projectApplicationMapper.updateByExampleSelective(projectApplication, example);
        }
    }

    /**
     * 查询批量执行任务报告列表
     *
     * @param request 请求参数
     * @return 执行任务报告集合
     */
    public List<BatchExecTaskReportDTO> listBatchTaskReport(BatchExecTaskPageRequest request) {
        List<BatchExecTaskReportDTO> batchReportList;
        if (StringUtils.equalsAny(request.getBatchType(), ExecTaskType.API_CASE_BATCH.name(), ExecTaskType.TEST_PLAN_API_CASE_BATCH.name())) {
            batchReportList = extExecTaskItemMapper.list(request, "api_report");
        } else {
            batchReportList = extExecTaskItemMapper.list(request, "api_scenario_report");
        }
        if (CollectionUtils.isEmpty(batchReportList)) {
            return new ArrayList<>();
        }
        List<String> userIds = batchReportList.stream().map(BatchExecTaskReportDTO::getCreateUser).toList();
        Map<String, String> userMap = userToolService.getUserMapByIds(userIds);
        batchReportList.forEach(item -> item.setCreateUserName(userMap.get(item.getCreateUser())));
        return batchReportList;
    }
}
