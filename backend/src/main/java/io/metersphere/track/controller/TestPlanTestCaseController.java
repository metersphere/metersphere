package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.track.request.testcase.TestPlanCaseBatchRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test/plan/case")
@RestController
public class TestPlanTestCaseController {

    @Resource
    TestPlanTestCaseService testPlanTestCaseService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestPlanCaseDTO>> getTestPlanCases(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanCaseRequest request){
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testPlanTestCaseService.getTestPlanCases(request));
    }

    @PostMapping("/list/all")
    public List<TestPlanCaseDTO> getTestPlanCases(@RequestBody QueryTestPlanCaseRequest request){
        return testPlanTestCaseService.getTestPlanCases(request);
    }

    @PostMapping("/edit")
    public void editTestCase(@RequestBody TestPlanTestCase testPlanTestCase){
        testPlanTestCaseService.editTestCase(testPlanTestCase);
    }

    @PostMapping("/batch/edit")
    public void editTestCaseBath(@RequestBody TestPlanCaseBatchRequest request){
        testPlanTestCaseService.editTestCaseBath(request);
    }

    @PostMapping("/delete/{id}")
    public int deleteTestCase(@PathVariable String id){
        return testPlanTestCaseService.deleteTestCase(id);
    }

}
