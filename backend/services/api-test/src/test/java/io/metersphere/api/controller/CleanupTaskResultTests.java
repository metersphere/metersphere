package io.metersphere.api.controller;

import io.metersphere.api.service.CleanupTaskResultServiceImpl;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.mapper.ExecTaskMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupTaskResultTests {

	@Resource
	private ExecTaskMapper execTaskMapper;
	@Resource
	private CleanupTaskResultServiceImpl cleanupTaskResultServiceImpl;

	public void initTaskData() {
		ExecTask task = new ExecTask();
		task.setId("clean-task-id");
		task.setNum(1L);
		task.setTaskName("clean-task-name");
		task.setStatus("PENDING");
		task.setCaseCount(1L);
		task.setTaskType("clean-task-type");
		task.setTriggerMode("clean-trigger-mode");
		task.setOrganizationId("clean-organization-id");
		task.setProjectId("clean-project-id");
		task.setIntegrated(true);
		task.setStartTime(1700000000000L);
		task.setResult("PENDING");
		task.setCreateUser("admin");
		task.setCreateTime(1700000000000L);
		execTaskMapper.insertSelective(task);
	}

	@Test
	@Order(1)
	public void testCleanupTaskResult() {
		initTaskData();
		Map<String, String> map = new HashMap<>();
		map.put(ProjectApplicationType.TASK.TASK_CLEAN_REPORT.name(), "1D");
		cleanupTaskResultServiceImpl.cleanReport(map, "clean-project-id");
		cleanupTaskResultServiceImpl.cleanReport(map, "clean-project-not-exit-id");
	}
}
