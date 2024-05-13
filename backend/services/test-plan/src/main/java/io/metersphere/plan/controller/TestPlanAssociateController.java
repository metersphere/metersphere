package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.plan.service.TestPlanConfigService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "测试计划关联用例弹窗接口相关")
@RequestMapping("/test-plan/association")
public class TestPlanAssociateController {

    @Resource
    private TestPlanConfigService testPlanConfigService;
    @Resource
    private FunctionalCaseService functionalCaseService;

    @PostMapping("/page")
    @Operation(summary = "测试计划-关联用例弹窗-功能用例列表查询(项目)")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<FunctionalCasePageDTO>> getFunctionalCasePage(@Validated @RequestBody FunctionalCasePageRequest request) {
        Boolean isRepeat = false;
        if (StringUtils.isNotBlank(request.getTestPlanId())) {
            isRepeat = testPlanConfigService.getConfigById(request.getTestPlanId());
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, functionalCaseService.getFunctionalCasePage(request, false, isRepeat));
    }

}
