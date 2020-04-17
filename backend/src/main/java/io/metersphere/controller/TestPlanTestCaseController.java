package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.controller.request.testcase.TestCaseBatchRequest;
import io.metersphere.controller.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.dto.TestPlanCaseDTO;
import io.metersphere.service.TestPlanTestCaseService;
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

    @PostMapping("/edit")
    public void editTestCase(@RequestBody TestPlanTestCase testPlanTestCase){
        testPlanTestCaseService.editTestCase(testPlanTestCase);
    }

    @PostMapping("/batch/edit")
    public void editTestCaseBath(@RequestBody TestCaseBatchRequest request){
        testPlanTestCaseService.editTestCaseBath(request);
    }

    @PostMapping("/delete/{id}")
    public int deleteTestCase(@PathVariable Integer id){
        return testPlanTestCaseService.deleteTestCase(id);
    }

}
