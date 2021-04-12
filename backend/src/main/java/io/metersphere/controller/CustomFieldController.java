package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.CustomField;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import io.metersphere.service.CustomFieldService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("custom/field")
@RestController
@RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
public class CustomFieldController {

    @Resource
    private CustomFieldService customFieldService;

    @PostMapping("/add")
    public void add(@RequestBody CustomField customField) {
        customFieldService.add(customField);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<CustomField>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryCustomFieldRequest request) {
        Page<List<CustomField>> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, customFieldService.list(request));
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable(value = "id") String id) {
        customFieldService.delete(id);
    }

    @PostMapping("/update")
    public void update(@RequestBody CustomField customField) {
        customFieldService.update(customField);
    }

}
