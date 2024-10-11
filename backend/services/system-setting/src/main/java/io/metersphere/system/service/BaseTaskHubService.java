package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskhub.TaskHubDTO;
import io.metersphere.system.dto.taskhub.TaskHubItemDTO;
import io.metersphere.system.dto.taskhub.TaskHubScheduleDTO;
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
    private TestPlanMapper testPlanMapper;

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
        return extExecTaskMapper.selectList(request, orgId, projectId);
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
     * @param items
     */
    public void insertExecTaskAndDetail(List<ExecTaskItem> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ExecTaskItemMapper itemMapper = sqlSession.getMapper(ExecTaskItemMapper.class);
            SubListUtils.dealForSubList(items, 1000, subList -> {
                subList.forEach(itemMapper::insertSelective);
            });
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }


    /**
     * 批量任务&任务详情入库接口
     *
     * @param tasks
     * @param items
     */
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
            long successCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ResultStatus.SUCCESS.name(), item.getStatus())).count();
            //失败数量
            long errorCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ResultStatus.ERROR.name(), item.getStatus())).count();
            //误报数量
            long fakeErrorCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ResultStatus.FAKE_ERROR.name(), item.getStatus())).count();
            //未执行数量
            long pendingCount = items.stream().filter(item -> StringUtils.endsWithIgnoreCase(ExecStatus.PENDING.name(), item.getStatus())).count();

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

}
