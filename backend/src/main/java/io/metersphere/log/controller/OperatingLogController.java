package io.metersphere.log.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.service.OperatingLogService;
import io.metersphere.log.vo.OperatingLogDTO;
import io.metersphere.log.vo.OperatingLogRequest;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/operating/log")
public class OperatingLogController {
    @Resource
    private OperatingLogService operatingLogService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<OperatingLogDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody OperatingLogRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, operatingLogService.list(request));
    }

    @GetMapping("/get/{id}")
    public OperatingLogDTO get(@PathVariable String id) {
        return operatingLogService.get(id);
    }


    @GetMapping("/get/source/{id}")
    public List<OperatingLogDTO> findBySourceId(@PathVariable String id) {
        return operatingLogService.findBySourceId(id);
    }

}
