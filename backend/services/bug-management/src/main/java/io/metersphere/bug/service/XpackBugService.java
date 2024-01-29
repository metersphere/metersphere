package io.metersphere.bug.service;

import io.metersphere.bug.dto.request.BugSyncRequest;
import io.metersphere.project.domain.Project;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;

import java.util.List;

/**
 * 缺陷相关xpack功能接口
 */
public interface XpackBugService {

    /**
     * 同步系统下所有第三方平台项目缺陷(定时任务)
     * @param projectId 项目ID
     * @param scheduleUser 定时任务执行用户
     */
    void syncPlatformBugsBySchedule(String projectId, String scheduleUser);

    /**
     * 同步当前项目第三方平台缺陷(前台调用, 全量同步)
     * @param project 项目
     * @param request 同步请求参数
     * @param currentUser 当前用户
     */
    void syncPlatformBugs(Project project, BugSyncRequest request, String currentUser);

    /**
     * 缺陷变更历史分页列表
     * @param request 请求参数
     * @return 变更历史集合
     */
    List<OperationHistoryDTO> listHis(OperationHistoryRequest request);
}
