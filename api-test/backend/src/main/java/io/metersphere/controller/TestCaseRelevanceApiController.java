package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.service.definition.ApiTestCaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/test/case/relevance")
public class TestCaseRelevanceApiController {

    @Resource
    ApiTestCaseService apiTestCaseService;

    @PostMapping("/api/list/{goPage}/{pageSize}")
    public Pager<List<ApiTestCaseDTO>> getTestCaseApiCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiTestCaseService.getRelevanceApiList(request));
    }

    @PostMapping("/scenario/list/{goPage}/{pageSize}")
    public Pager<List<ApiScenarioDTO>> getTestCaseScenarioCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiTestCaseService.getRelevanceScenarioList(request));
    }
}
