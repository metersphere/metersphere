package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("schedule")
@RestController
public class ScheduleController {
    @Resource
    private ScheduleService scheduleService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public List<ScheduleDao> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryScheduleRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return scheduleService.list(request);
    }

}
