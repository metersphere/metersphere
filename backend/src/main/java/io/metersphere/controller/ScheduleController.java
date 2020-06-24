package io.metersphere.controller;

import io.metersphere.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("schedule")
@RestController
public class ScheduleController {
    @Resource
    private ScheduleService scheduleService;
}
