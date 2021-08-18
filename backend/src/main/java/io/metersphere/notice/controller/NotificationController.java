package io.metersphere.notice.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Notification;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.notice.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @PostMapping(value = "/list/all/{goPage}/{pageSize}")
    public Pager<List<Notification>> listNotification(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Notification notification) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, notificationService.listNotification(notification));
    }

    @PostMapping(value = "/list/read/{goPage}/{pageSize}")
    public Pager<List<Notification>> listReadNotification(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Notification notification) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, notificationService.listReadNotification(notification));
    }

    @PostMapping(value = "/list/unread/{goPage}/{pageSize}")
    public Pager<List<Notification>> listUnreadNotification(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Notification notification) {
        Page page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, notificationService.listUnreadNotification(notification));
    }

    @GetMapping(value = "/get/{id}")
    public Notification getNotification(@PathVariable int id) {
        return notificationService.getNotification(id);
    }

    @GetMapping(value = "/read/{id}")
    public Integer read(@PathVariable int id) {
        return notificationService.read(id);
    }

    @GetMapping(value = "/read/all")
    public Integer readAll() {
        return notificationService.readAll();
    }

    @PostMapping(value = "/count")
    public Integer countNotification(@RequestBody Notification notification) {
        return notificationService.countNotification(notification);
    }
}
