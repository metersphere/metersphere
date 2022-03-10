package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseTemplate;
import io.metersphere.base.domain.TestCaseTemplateWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateCaseFieldTemplateRequest;
import io.metersphere.dto.TestCaseTemplateDao;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.TestCaseTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("field/template/case")
@RestController

public class TestCaseTemplateController {

    @Resource
    private TestCaseTemplateService testCaseTemplateService;

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_CASE, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseTemplateService.class)
    public void add(@RequestBody UpdateCaseFieldTemplateRequest request) {
        testCaseTemplateService.add(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestCaseTemplateWithBLOBs>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseQueryRequest request) {
        Page<List<TestCaseTemplateWithBLOBs>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseTemplateService.list(request));
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = TestCaseTemplateService.class)
    public void delete(@PathVariable(value = "id") String id) {
        testCaseTemplateService.delete(id);
    }

    @PostMapping("/update")
    @MsAuditLog(module = OperLogModule.WORKSPACE_TEMPLATE_SETTINGS_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseTemplateService.class)
    public void update(@RequestBody UpdateCaseFieldTemplateRequest request) {
        testCaseTemplateService.update(request);
    }

    @GetMapping({"/option/{projectId}", "/option"})
    public List<TestCaseTemplate> list(@PathVariable(required = false) String projectId) {
        return testCaseTemplateService.getOption(projectId);
    }

    @GetMapping("/get/relate/{projectId}")
    public TestCaseTemplateDao getTemplate(@PathVariable String projectId) {
        return testCaseTemplateService.getTemplate(projectId);
    }
}
