package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.ApiTagRequest;
import io.metersphere.api.dto.automation.SaveApiTagRequest;
import io.metersphere.api.service.ApiTagService;
import io.metersphere.base.domain.ApiTag;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/tag")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
public class ApiTagController {

    @Resource
    ApiTagService apiTagService;

    @PostMapping("/list")
    public List<ApiTag> list(@RequestBody ApiTagRequest request) {
        return apiTagService.list(request);
    }

    @PostMapping("/getTgas/{goPage}/{pageSize}")
    public Pager<List<ApiTag>> getTgas(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTagRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiTagService.getTgas(request));
    }

    @PostMapping(value = "/create")
    public void create(@RequestBody SaveApiTagRequest request) {
        apiTagService.create(request);
    }

    @PostMapping(value = "/update")
    public void update(@RequestBody SaveApiTagRequest request) {
        apiTagService.update(request);
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        apiTagService.delete(id);
    }

}

