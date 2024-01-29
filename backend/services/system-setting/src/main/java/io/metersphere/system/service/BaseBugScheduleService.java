package io.metersphere.system.service;

import io.metersphere.project.domain.ProjectApplication;

import java.util.List;

public interface BaseBugScheduleService {

	/**
	 * 更新项目的缺陷同步定时任务
	 * @param bugSyncConfigs 配置
	 * @param projectId 项目ID
	 * @param currentUser 当前用户
	 */
	void updateBugSyncScheduleConfig(List<ProjectApplication> bugSyncConfigs, String projectId, String currentUser);

	/**
	 * 启用或禁用缺陷同步定时任务
	 * @param projectId 项目ID
	 * @param currentUser 当前用户
	 * @param enable 开启或禁用
	 */
	void enableOrNotBugSyncSchedule(String projectId, String currentUser, Boolean enable);
}
