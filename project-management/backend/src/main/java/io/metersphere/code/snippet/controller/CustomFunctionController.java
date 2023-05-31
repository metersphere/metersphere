package io.metersphere.code.snippet.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.CustomFunction;
import io.metersphere.base.domain.CustomFunctionWithBLOBs;
import io.metersphere.code.snippet.service.CustomFunctionService;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.request.CustomFunctionRequest;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lyh
 */

@RequestMapping("/custom/func")
@RestController
public class CustomFunctionController {

    @Resource
    private CustomFunctionService customFunctionService;

    @PostMapping("/save")
    @RequiresPermissions("PROJECT_CUSTOM_CODE:READ+CREATE")
    public CustomFunctionWithBLOBs save(@RequestBody CustomFunctionRequest request) {
        return customFunctionService.save(request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions("PROJECT_CUSTOM_CODE:READ+DELETE")
    public void delete(@PathVariable String id) {
        customFunctionService.delete(id);
    }

    @PostMapping("/update")
    @RequiresPermissions("PROJECT_CUSTOM_CODE:READ+EDIT")
    public void update(@RequestBody CustomFunctionRequest request) {
        customFunctionService.update(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<CustomFunction>> query(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody CustomFunctionRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, customFunctionService.query(request));
    }

    @GetMapping("/copy/{id}")
    @RequiresPermissions("PROJECT_CUSTOM_CODE:READ+COPY")
    public CustomFunctionWithBLOBs copy(@PathVariable String id) {
        return customFunctionService.copy(id);
    }

    @GetMapping("/get/{id}")
    public CustomFunctionWithBLOBs get(@PathVariable String id) {
        return customFunctionService.get(id);
    }

    @PostMapping("/run")
    @RequiresPermissions(value = {"PROJECT_CUSTOM_CODE:READ+CREATE", "PROJECT_CUSTOM_CODE:READ+COPY"}, logical = Logical.OR)
    public void run(@RequestBody Object request) {
        customFunctionService.run(request);
    }
}
