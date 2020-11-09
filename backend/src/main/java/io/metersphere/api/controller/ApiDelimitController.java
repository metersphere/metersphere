package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.delimit.ApiDelimitRequest;
import io.metersphere.api.dto.delimit.ApiDelimitResult;
import io.metersphere.api.dto.delimit.SaveApiDelimitRequest;
import io.metersphere.api.service.ApiDelimitService;
import io.metersphere.base.domain.ApiDelimit;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping(value = "/api/delimit")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class ApiDelimitController {
    @Resource
    private ApiDelimitService apiDelimitService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiDelimitResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDelimitRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiDelimitService.list(request));
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public void create(@RequestPart("request") SaveApiDelimitRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiDelimitService.create(request, file, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public void update(@RequestPart("request") SaveApiDelimitRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiDelimitService.update(request, file, bodyFiles);
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        apiDelimitService.delete(id);
    }

    @PostMapping("/deleteBatch")
    public void deleteBatch(@RequestBody List<String> ids) {
        apiDelimitService.deleteBatch(ids);
    }

    @GetMapping("/get/{id}")
    public ApiDelimit get(@PathVariable String id) {
        return apiDelimitService.get(id);
    }

}
