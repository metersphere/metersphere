package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.service.JarConfigService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/jar")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class JarConfigController {

    @Resource
    JarConfigService JarConfigService;

    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Pager<List<JarConfig>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody JarConfig request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, JarConfigService.list(request));
    }

    @GetMapping("list/all")
    public List<JarConfig> listAll() {
        return JarConfigService.list();
    }

    @PostMapping("list")
    public List<JarConfig> list(@RequestBody JarConfig jarConfig) {
        return JarConfigService.searchList(jarConfig);
    }

    @GetMapping("/get/{id}")
    public JarConfig get(@PathVariable String id) {
        return JarConfigService.get(id);
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public String add(@RequestPart("request") JarConfig request, @RequestPart(value = "file") MultipartFile file) {
        return JarConfigService.add(request, file);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void update(@RequestPart("request") JarConfig request, @RequestPart(value = "file", required = false) MultipartFile file) {
        JarConfigService.update(request, file);
    }

    @GetMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void delete(@PathVariable String id) {
        JarConfigService.delete(id);
    }

}
