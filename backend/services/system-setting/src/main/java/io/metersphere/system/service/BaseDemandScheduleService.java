package io.metersphere.system.service;

import io.metersphere.project.domain.ProjectApplication;

import java.util.List;

public interface BaseDemandScheduleService {

	/**
	 * 更新项目的需求同步定时任务
	 * @param demandSyncConfigs 配置
	 * @param projectId 项目ID
	 * @param currentUser 当前用户
	 */
	void updateDemandSyncScheduleConfig(List<ProjectApplication> demandSyncConfigs, String projectId, String currentUser);

	/**
	 * 启用或禁用需求同步定时任务
	 * @param projectId 项目ID
	 * @param currentUser 当前用户
	 * @param enable 开启或禁用
	 */
	void enableOrNotDemandSyncSchedule(String projectId, String currentUser, Boolean enable);
}
