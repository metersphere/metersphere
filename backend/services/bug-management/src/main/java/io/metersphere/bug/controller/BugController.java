package io.metersphere.bug.controller;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.service.BugService;
import io.metersphere.validation.groups.Created;
import io.swagger.annotations.Api;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author : jianxing
 * @date : 2023-5-17
 */
@Api(tags = "缺陷")
@RestController
@RequestMapping("/bug")
public class BugController {
    @Resource
    private BugService bugService;

    @GetMapping("/list-all")
    public List<Bug> listAll() {
        return bugService.list();
    }

    @GetMapping("/get/{id}")
    public Bug get(@PathVariable String id) {
        return bugService.get(id);
    }

    @PostMapping("/add")
    public Bug add(@Validated({Created.class}) @RequestBody Bug bug) {
        return bugService.add(bug);
    }

    @PostMapping("/update")
    public Bug update(@Validated({Created.class}) @RequestBody Bug bug) {
        return bugService.update(bug);
    }

    @GetMapping("/delete/{id}")
    public int delete(@PathVariable String id) {
        return bugService.delete(id);
    }
}
