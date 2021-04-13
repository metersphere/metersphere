package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestCaseFieldTemplate;
import io.metersphere.base.domain.TestCaseFieldTemplateWithBLOBs;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateCaseFieldTemplateRequest;
import io.metersphere.service.TestCaseFieldTemplateService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("field/template/case")
@RestController
@RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
public class TestCaseFieldTemplateController {

    @Resource
    private TestCaseFieldTemplateService testCaseFieldTemplateService;

    @PostMapping("/add")
    public void add(@RequestBody UpdateCaseFieldTemplateRequest request) {
        testCaseFieldTemplateService.add(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TestCaseFieldTemplate>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseQueryRequest request) {
        Page<List<TestCaseFieldTemplate>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseFieldTemplateService.list(request));
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable(value = "id") String id) {
        testCaseFieldTemplateService.delete(id);
    }

    @PostMapping("/update")
    public void update(@RequestBody UpdateCaseFieldTemplateRequest request) {
        testCaseFieldTemplateService.update(request);
    }

}
