package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.plan.request.LoadCaseRequest;
import io.metersphere.track.service.PerformanceTestCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/test/case/relevance")
public class TestCaseRelevancePerformanceController {

    @Resource
    PerformanceTestCaseService performanceTestCaseService;

    @PostMapping("/load/list/{goPage}/{pageSize}")
    public Pager<List<LoadTestDTO>> getTestCaseLoadCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, performanceTestCaseService.getRelevanceLoadList(request));
    }
}
