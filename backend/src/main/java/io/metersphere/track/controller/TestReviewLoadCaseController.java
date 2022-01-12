package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseReviewLoad;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.TestReviewLoadCaseDTO;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testreview.TestReviewRequest;
import io.metersphere.track.service.TestCaseReviewLoadService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/test/review/load/case")
public class TestReviewLoadCaseController {
    @Resource
    private TestCaseReviewLoadService testCaseReviewLoadService;

    @PostMapping("/relevance/list/{goPage}/{pageSize}")
    public Pager<List<LoadTestDTO>> relevanceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestReviewRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseReviewLoadService.relevanceList(request));
    }

    @PostMapping("/relevance")
    public void relevanceCase(@RequestBody TestReviewRequest request) {
        testCaseReviewLoadService.relevanceCase(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestReviewLoadCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TestReviewRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseReviewLoadService.list(request));
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        testCaseReviewLoadService.delete(id);
    }

    @PostMapping("/run")
    public String run(@RequestBody RunTestPlanRequest request) {
        return testCaseReviewLoadService.run(request);
    }

    @PostMapping("/report/exist")
    public Boolean isExistReport(@RequestBody LoadCaseReportRequest request) {
        return testCaseReviewLoadService.isExistReport(request);
    }

    @PostMapping("/batch/delete")
    public void batchDelete(@RequestBody List<String> ids) {
        testCaseReviewLoadService.batchDelete(ids);
    }

    @PostMapping("/update")
    public void update(@RequestBody TestCaseReviewLoad testCaseReviewLoad) {
        testCaseReviewLoadService.update(testCaseReviewLoad);
    }
}
