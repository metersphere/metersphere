package io.metersphere.controller;

import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.JarConfigRequest;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.JarConfigService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/jar")
public class JarConfigController {

    @Resource
    JarConfigService JarConfigService;

    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresPermissions(value = {"PROJECT_FILE:READ+JAR", "PROJECT_FILE:READ+FILE"}, logical = Logical.OR)
    public Pager<List<JarConfig>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody JarConfigRequest request) {
        return JarConfigService.list(request, goPage, pageSize);
    }

    @GetMapping("list/all")
    @RequiresPermissions(value = {"PROJECT_FILE:READ+JAR", "PROJECT_FILE:READ+FILE"}, logical = Logical.OR)
    public List<JarConfig> listAll() {
        return JarConfigService.list();
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(value = {"PROJECT_FILE:READ+JAR", "PROJECT_FILE:READ+FILE"}, logical = Logical.OR)
    public JarConfig get(@PathVariable String id) {
        return JarConfigService.get(id);
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresPermissions(value = {"PROJECT_FILE:READ+UPLOAD+JAR", "PROJECT_FILE:READ+UPLOAD+FILE"}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_JAR, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = JarConfigService.class)
    public String add(@RequestPart("request") JarConfig request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return JarConfigService.add(request, file);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresPermissions(value = {"PROJECT_FILE:READ+UPLOAD+JAR", "PROJECT_FILE:READ+UPLOAD+FILE"}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_JAR, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = JarConfigService.class)
    public void update(@RequestPart("request") JarConfig request, @RequestPart(value = "file", required = false) MultipartFile file) {
        JarConfigService.update(request, file);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(value = {"PROJECT_FILE:READ+DELETE+JAR", "PROJECT_FILE:READ+DELETE+FILE"}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_JAR, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = JarConfigService.class)
    public void delete(@PathVariable String id) {
        JarConfigService.delete(id);
    }

}
