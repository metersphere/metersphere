package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskhub.*;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.dto.taskhub.response.TaskStatisticsResponse;
import io.metersphere.system.mapper.*;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<String> projectIds = list.stream().map(TaskHubDTO::getProjectId).distinct().toList();
        List<String> organizationIds = list.stream().map(TaskHubDTO::getProjectId).distinct().toList();
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projectList = projectMapper.selectByExample(projectExample);
        Map<String, String> projectMaps = projectList.stream().collect(Collectors.toMap(Project::getId, Project::getName));

        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(organizationIds);
        List<Organization> organizationList = organizationMapper.selectByExample(organizationExample);
        Map<String, String> organizationMaps = organizationList.stream().collect(Collectors.toMap(Organization::getId, Organization::getName));

        list.forEach(item -> {
            item.setProjectName(projectMaps.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
            item.setOrganizationName(organizationMaps.getOrDefault(item.getOrganizationId(), StringUtils.EMPTY));
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
        return extExecTaskItemMapper.selectList(request, orgId, projectId);
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
        ExecTask execTask = new ExecTask();
        execTask.setId(id);
        execTask.setStatus(ExecStatus.STOPPED.name());
        execTask.setCreateUser(userId);
        execTask.setProjectId(projectId);
        execTask.setOrganizationId(orgId);
        extExecTaskMapper.updateTaskStatus(execTask);
        //2.更新任务明细状态
        ExecTaskItemExample itemExample = new ExecTaskItemExample();
        itemExample.createCriteria().andTaskIdEqualTo(id).andStatusEqualTo(ExecStatus.RUNNING.name());
        ExecTaskItem execTaskItem = new ExecTaskItem();
        execTaskItem.setStatus(ExecStatus.STOPPED.name());
        execTaskItem.setExecutor(userId);
        execTaskItemMapper.updateByExampleSelective(execTaskItem, itemExample);
        //TODO 3.调用jmeter触发停止


    }

    public void deleteTask(String id, String orgId, String projectId) {
        //1.删除任务
        extExecTaskMapper.deleteTaskById(id, orgId, projectId);
        //2.删除任务明细
        ExecTaskItemExample itemExample = new ExecTaskItemExample();
        itemExample.createCriteria().andTaskIdEqualTo(id);
        execTaskItemMapper.deleteByExample(itemExample);
        //TODO jmeter执行队列中移除
    }
}
