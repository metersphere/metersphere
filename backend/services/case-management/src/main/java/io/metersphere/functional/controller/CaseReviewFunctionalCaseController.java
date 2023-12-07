package io.metersphere.functional.controller;


import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.ReviewFunctionalCaseDTO;
import io.metersphere.functional.request.ReviewFunctionalCasePageRequest;
import io.metersphere.functional.service.CaseReviewFunctionalCaseService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wx
 */
@Tag(name = "用例管理-用例评审-评审列表-评审详情")
@RestController
@RequestMapping("/case/review/detail")
public class CaseReviewFunctionalCaseController {

    @Resource
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;


    @GetMapping("/get-ids/{reviewId}")
    @Operation(summary = "用例管理-功能用例-评审列表-评审详情-获取已关联用例id集合(关联用例弹窗前调用)")
    public List<String> getCaseIds(@PathVariable String reviewId) {
        return caseReviewFunctionalCaseService.getCaseIdsByReviewId(reviewId);
    }


    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-评审列表-评审详情-已关联用例列表")
    public Pager<List<ReviewFunctionalCaseDTO>> page(@Validated @RequestBody ReviewFunctionalCasePageRequest request) {
        String userId = StringUtils.EMPTY;
        if (request.getViewFlag()) {
            userId = SessionUtils.getUserId();
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, caseReviewFunctionalCaseService.page(request, false, userId));
    }
}
