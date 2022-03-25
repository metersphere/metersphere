package io.metersphere.log.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.OperatingLogWithBLOBs;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.service.OperatingLogService;
import io.metersphere.log.vo.OperatingLogDTO;
import io.metersphere.log.vo.OperatingLogRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

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


    @PostMapping("/get/source")
    public List<OperatingLogDTO> findBySourceId(@RequestBody OperatingLogRequest request) {
        return operatingLogService.findBySourceId(request);
    }

    @PostMapping("/save")
    public void save(@RequestBody OperatingLogWithBLOBs msOperLog) {
        //保存获取的操作
        msOperLog.setId(UUID.randomUUID().toString());
        String sourceIds = msOperLog.getSourceId();
        operatingLogService.create(msOperLog, sourceIds);
    }
}
