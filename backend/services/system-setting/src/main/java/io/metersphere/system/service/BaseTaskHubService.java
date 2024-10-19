package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.domain.ApiReportRelateTaskExample;
import io.metersphere.api.mapper.ApiReportRelateTaskMapper;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.MsHttpClient;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskhub.*;
import io.metersphere.system.dto.taskhub.request.TaskHubItemBatchRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.dto.taskhub.response.TaskStatisticsResponse;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.*;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.TaskRunnerClient;
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
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
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

    private final static String GET_TASK_ITEM_ORDER_URL = "http://%s/api/task/item/order";

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ExtSwaggerMapper extSwaggerMapper;
    @Resource
    private OperationLogService operationLogService;

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
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "start_time desc");
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

    }

    private Map<String, String> getUserMaps(List<String> userIds) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> userList = userMapper.selectByExample(userExample);
        Map<String, String> userMaps = userList.stream().collect(Collectors.toMap(User::getId, User::getName));
        return userMaps;
    }

    private Map<String, String> getOrganizationMaps(List<String> organizationIds) {
        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(organizationIds);
        List<Organization> organizationList = organizationMapper.selectByExample(organizationExample);
        Map<String, String> organizationMaps = organizationList.stream().collect(Collectors.toMap(Organization::getId, Organization::getName));
        return organizationMaps;
    }

    private Map<String, String> getProjectMaps(List<String> projectIds) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projectList = projectMapper.selectByExample(projectExample);
        Map<String, String> projectMaps = projectList.stream().collect(Collectors.toMap(Project::getId, Project::getName));
        return projectMaps;
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
            list.forEach(item -> {
                item.setCreateUserName(userMap.getOrDefault(item.getCreateUserName(), StringUtils.EMPTY));
                item.setOrganizationName(orgMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
            });
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
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "id asc");
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
        Map<String, String> userMaps = getUserMaps(userIds);
        Map<String, String> resourcePoolMaps = getResourcePoolMaps(resourcePoolIds);
        list.forEach(item -> {
            item.setUserName(userMaps.getOrDefault(item.getExecutor(), StringUtils.EMPTY));
            item.setResourcePoolName(resourcePoolMaps.getOrDefault(item.getResourcePoolId(), StringUtils.EMPTY));
        });
    }

    private Map<String, String> getResourcePoolMaps(List<String> resourcePoolIds) {
        TestResourcePoolExample poolExample = new TestResourcePoolExample();
        poolExample.createCriteria().andIdIn(resourcePoolIds);
        List<TestResourcePool> poolList = testResourcePoolMapper.selectByExample(poolExample);
        Map<String, String> poolMaps = poolList.stream().collect(Collectors.toMap(TestResourcePool::getId, TestResourcePool::getName));
        return poolMaps;
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
            long successCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ResultStatus.SUCCESS.name(), item.getResult())).count();
            //失败数量
            long errorCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ResultStatus.ERROR.name(), item.getResult())).count();
            //误报数量
            long fakeErrorCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ResultStatus.FAKE_ERROR.name(), item.getResult())).count();
            //未执行数量
            long pendingCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ExecStatus.PENDING.name(), item.getResult())).count();

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

    private Map<String, List<TestResourcePoolBlob>> getPoolMap(List<TestResourcePool> allResourcePools) {
        List<String> ids = allResourcePools.stream().map(TestResourcePool::getId).toList();
        //获取全部资源池节点
        TestResourcePoolBlobExample blobExample = new TestResourcePoolBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        List<TestResourcePoolBlob> testResourcePoolBlobs = testResourcePoolBlobMapper.selectByExampleWithBLOBs(blobExample);
        Map<String, List<TestResourcePoolBlob>> poolMap = testResourcePoolBlobs.stream().collect(Collectors.groupingBy(TestResourcePoolBlob::getId));
        return poolMap;
    }

    private List<ResourcePoolOptionsDTO> handleOptions(List<TestResourcePool> allResourcePools, Map<String, List<TestResourcePoolBlob>> poolMap) {
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
     * 获取项目下的资源池及节点下拉选项
     *
     * @param projectId
     * @return
     */
    public List<ResourcePoolOptionsDTO> getProjectResourcePoolOptions(String projectId) {
        ProjectTestResourcePoolExample example = new ProjectTestResourcePoolExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        List<ProjectTestResourcePool> projectPools = projectTestResourcePoolMapper.selectByExample(example);
        List<String> poolIds = projectPools.stream().map(ProjectTestResourcePool::getTestResourcePoolId).toList();
        if (CollectionUtils.isNotEmpty(poolIds)) {
            List<TestResourcePool> allResourcePools = extResourcePoolMapper.selectProjectAllResourcePool(poolIds);
            Map<String, List<TestResourcePoolBlob>> poolMap = getPoolMap(allResourcePools);
            return handleOptions(allResourcePools, poolMap);
        }
        return null;
    }

    /**
     * 获取任务详情列表资源节点状态
     *
     * @param ids
     * @return
     */
    public List<ResourcePoolStatusDTO> getResourcePoolStatus(List<String> ids) {
        List<ResourcePoolStatusDTO> statusDTOS = new ArrayList<>();
        List<ExecTaskItem> itemList = extExecTaskItemMapper.selectPoolNodeByIds(ids);
        Map<String, List<ExecTaskItem>> poolNodeMap = itemList.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode));
        poolNodeMap.forEach((k, v) -> {
            String[] split = k.split(":");
            TestResourceNodeDTO node = new TestResourceNodeDTO();
            boolean status = false;
            try {
                node.setIp(split[0]);
                node.setPort(split[1]);
                status = nodeResourcePoolService.validateNode(node);
            } catch (Exception e) {
                status = false;
            }
            boolean finalStatus = status;
            v.forEach(item -> {
                ResourcePoolStatusDTO poolStatusDTO = new ResourcePoolStatusDTO();
                poolStatusDTO.setId(item.getId());
                poolStatusDTO.setStatus(finalStatus);
                statusDTOS.add(poolStatusDTO);
            });
        });
        return statusDTOS;
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
        handleStopTask(List.of(id));
    }

    private void handleStopTask(List<String> ids) {
        //获取详情资源池
        List<ExecTaskItem> list = extExecTaskItemMapper.getResourcePoolsByTaskIds(ids);
        Map<String, List<ExecTaskItem>> resourcePoolMaps = list.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolId));
        resourcePoolMaps.forEach((k, v) -> {
            //判断资源池类型
            TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(k);
            boolean isK8SResourcePool = StringUtils.equals(testResourcePoolDTO.getType(), ResourcePoolTypeEnum.K8S.getName());
            if (isK8SResourcePool) {
                List<String> taskIds = list.stream().map(ExecTaskItem::getTaskId).distinct().toList();
                //K8S
                handleK8STask(taskIds, testResourcePoolDTO);
            } else {
                Map<String, List<ExecTaskItem>> nodeItem = list.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode));
                handleNodeTask(nodeItem);
            }
        });
    }

    private void handleNodeTask(Map<String, List<ExecTaskItem>> nodeItem) {
        //通过任务向节点发起停止
        nodeItem.forEach((node, items) -> {
            String endpoint = "http://".concat(node);
            List<String> itemIds = items.stream().map(ExecTaskItem::getId).toList();
            SubListUtils.dealForSubList(itemIds, 100, subList -> {
                try {
                    LogUtils.info(String.format("开始发送停止请求到 %s 节点执行", endpoint), subList.toString());
                    MsHttpClient.stopApiTaskItem(endpoint, subList);
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
        execTaskItemMapper.deleteByExample(itemExample);
        //3.删除任务与报告关联关系
        deleteReportRelateTask(List.of(id));
        handleStopTask(List.of(id));
    }

    private void deleteReportRelateTask(List<String> ids) {
        List<String> itemIds = extExecTaskItemMapper.getItemIdByTaskIds(ids);
        itemIds.addAll(ids);
        ApiReportRelateTaskExample reportExample = new ApiReportRelateTaskExample();
        reportExample.createCriteria().andTaskResourceIdIn(itemIds);
        apiReportRelateTaskMapper.deleteByExample(reportExample);
    }

    public void batchStopTask(List<String> ids, String userId, String orgId, String projectId) {
        if (CollectionUtils.isNotEmpty(ids)) {
            //1.更新任务状态
            extExecTaskMapper.batchUpdateTaskStatus(ids, userId, orgId, projectId, ExecStatus.STOPPED.name());
            //2.更新任务明细状态
            extExecTaskItemMapper.batchUpdateTaskItemStatus(ids, userId, orgId, projectId, ExecStatus.STOPPED.name());
            handleStopTask(ids);
        }

    }

    public List<String> getTaskIds(TableBatchProcessDTO request, String orgId, String projectId) {
        if (request.isSelectAll()) {
            List<String> ids = extExecTaskMapper.getIds(request, orgId, projectId);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public void batchDeleteTask(List<String> ids, String orgId, String projectId) {
        if (CollectionUtils.isNotEmpty(ids)) {
            //1.删除任务
            extExecTaskMapper.deleteTaskByIds(ids, orgId, projectId);
            //2.删除任务明细
            ExecTaskItemExample itemExample = new ExecTaskItemExample();
            itemExample.createCriteria().andTaskIdIn(ids);
            execTaskItemMapper.deleteByExample(itemExample);
            //3.删除任务与报告关联关系
            deleteReportRelateTask(ids);
            handleStopTask(ids);
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
        handleStopTaskItem(List.of(id));
    }


    private void handleStopTaskItem(List<String> ids) {
        List<ExecTaskItem> execTaskItems = extExecTaskItemMapper.getResourcePoolsByItemIds(ids);
        Map<String, List<ExecTaskItem>> resourcePoolMaps = execTaskItems.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolId));
        resourcePoolMaps.forEach((k, v) -> {
            //判断资源池类型
            TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(k);
            boolean isK8SResourcePool = StringUtils.equals(testResourcePoolDTO.getType(), ResourcePoolTypeEnum.K8S.getName());
            if (isK8SResourcePool) {
                List<String> itemIds = v.stream().map(ExecTaskItem::getId).toList();
                //K8S 通过任务项id停止任务项
                handleK8STaskItem(itemIds, testResourcePoolDTO);
            } else {
                Map<String, List<ExecTaskItem>> nodeItem = execTaskItems.stream().collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode));
                handleNodeTask(nodeItem);
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
            handleStopTaskItem(itemIds);
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
                .collect(Collectors.groupingBy(ExecTaskItem::getResourcePoolNode));

        Map<String, Integer> taskItemOrderMap = new HashMap<>();

        List<Thread> threads = new ArrayList<>();
        nodeResourceMap.forEach((node, items) -> {
            if (StringUtils.isNotBlank(node)) {
                Thread thread = Thread.startVirtualThread(() -> taskItemOrderMap.putAll(getTaskItemOrder(node, taskIdItemIds)));
                threads.add(thread);
            }
        });

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LogUtils.error(e);
            }
        }

        return taskItemOrderMap;
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
}
