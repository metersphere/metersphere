package io.metersphere.dashboard.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.service.BugService;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.request.CaseReviewPageRequest;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author song-cc-rock
 */

@Tag(name = "工作台-我的待办")
@RestController
@RequestMapping("/dashboard/todo")
public class ToDoController {

	@Resource
	private CaseReviewService caseReviewService;
	@Resource
	private BugService bugService;

	@PostMapping("/review/page")
	@Operation(summary = "我的待办-用例评审-列表分页查询")
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<CaseReviewDTO>> reviewPage(@Validated @RequestBody CaseReviewPageRequest request) {
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
		request.setMyTodo(true);
		request.setMyTodoUserId(SessionUtils.getUserId());
		return PageUtils.setPageInfo(page, caseReviewService.getCaseReviewPage(request));
	}

	@PostMapping("/bug/page")
	@Operation(summary = "我的待办-缺陷-列表分页查询")
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<BugDTO>> bugPage(@Validated @RequestBody BugPageRequest request) {
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
		request.setUseTrash(false);
		request.setMyTodo(true);
		request.setMyTodoUserId(SessionUtils.getUserId());
		return PageUtils.setPageInfo(page, bugService.list(request));
	}
}
