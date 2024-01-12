package io.metersphere.functional.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.dto.FunctionalCaseReviewDTO;
import io.metersphere.functional.request.FunctionalCaseReviewListRequest;
import io.metersphere.functional.service.FunctionalCaseReviewService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用例管理-功能用例-评审")
@RestController
@RequestMapping("/functional/case/review")
public class FunctionalCaseReviewController {

    @Resource
    private FunctionalCaseReviewService functionalCaseReviewService;

    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-评审-列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getCaseId()", resourceType = "functional_case")
    public Pager<List<FunctionalCaseReviewDTO>> getFunctionalCasePage(@Validated @RequestBody FunctionalCaseReviewListRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), "update_time desc");
        return PageUtils.setPageInfo(page, functionalCaseReviewService.getFunctionalCaseReviewPage(request));
    }

    @GetMapping("/comment/{caseId}")
    @Operation(summary = "用例管理-功能用例-评审-获取评审评论历史")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#caseId", resourceType = "functional_case")
    public List<CaseReviewHistoryDTO> getCaseReviewHistory(@PathVariable String caseId) {
        return functionalCaseReviewService.getCaseReviewHistory(caseId);
    }

}
