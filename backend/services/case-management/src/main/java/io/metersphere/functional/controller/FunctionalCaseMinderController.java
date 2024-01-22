package io.metersphere.functional.controller;

import com.alibaba.excel.util.StringUtils;
import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.functional.request.MinderReviewFunctionalCasePageRequest;
import io.metersphere.functional.service.FunctionalCaseMinderService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guoyuqi
 */
@Tag(name = "用例管理-功能用例-脑图")
@RestController
@RequestMapping("/functional/mind/case")
public class FunctionalCaseMinderController {

    @Resource
    private FunctionalCaseMinderService functionalCaseMinderService;

    @PostMapping("/list")
    @Operation(summary = "用例管理-功能用例-脑图用例列表查询")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public FunctionalMinderTreeDTO getFunctionalCaseMinderTree(@Validated @RequestBody FunctionalCasePageRequest request) {
        return functionalCaseMinderService.getFunctionalCasePage(request, false);
    }

    @PostMapping("/review/list")
    @Operation(summary = "用例管理-用例评审-脑图用例列表查询")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public FunctionalMinderTreeDTO getFunctionalReviewCaseMinderTree(@Validated @RequestBody MinderReviewFunctionalCasePageRequest request) {
        String userId = StringUtils.EMPTY;
        if (request.isViewFlag()) {
            userId = SessionUtils.getUserId();
        }
        return functionalCaseMinderService.getReviewFunctionalCasePage(request, false, userId);
    }

}
