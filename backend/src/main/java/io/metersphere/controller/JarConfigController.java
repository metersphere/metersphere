package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.service.FileService;
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
    FileService fileService;

    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Pager<List<FileMetadata>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody FileMetadata request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, fileService.list(request));
    }

    @GetMapping("list/all")
    public List<FileMetadata> listAll() {
        return fileService.list();
    }

    @PostMapping("list")
    public List<FileMetadata> list(@RequestBody FileMetadata jarConfig) {
        return fileService.searchList(jarConfig, true);
    }

    @GetMapping("/get/{id}")
    public FileMetadata get(@PathVariable String id) {
        return fileService.get(id);
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public String add(@RequestPart("request") FileMetadata request, @RequestPart(value = "file") MultipartFile file) {
        return fileService.saveFile(request, file);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void update(@RequestPart("request") FileMetadata request, @RequestPart(value = "file", required = false) MultipartFile file) {
        fileService.updateFile(request, file);
    }

    @GetMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void delete(@PathVariable String id) {
        fileService.deleteFileById(id);
    }

}
