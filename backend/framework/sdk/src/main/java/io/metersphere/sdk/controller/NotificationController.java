package io.metersphere.sdk.controller;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.metersphere.project.domain.Notification;
import io.metersphere.sdk.dto.request.NotificationRequest;
import io.metersphere.sdk.service.NotificationService;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "消息中心")
@RestController
@RequestMapping(value = "notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @PostMapping(value = "/list/all/page")
    @Operation(summary = "消息中心-获取消息中心所有消息列表")
    public Pager<List<Notification>> listNotification(@Validated @RequestBody NotificationRequest notificationRequest) {
        Page<Object> page = PageHelper.startPage(notificationRequest.getCurrent(), notificationRequest.getPageSize(), true);
        return PageUtils.setPageInfo(page, notificationService.listNotification(notificationRequest, SessionUtils.getUserId()));
    }

    @GetMapping(value = "/read/{id}")
    @Operation(summary = "消息中心-将消息设置为已读")
    public Integer read(@PathVariable int id) {
        return notificationService.read(id, SessionUtils.getUserId());
    }

    @GetMapping(value = "/read/all")
    @Operation(summary = "消息中心-获取消息中心所有已读消息")
    public Integer readAll() {
        return notificationService.readAll(SessionUtils.getUserId());
    }

    @PostMapping(value = "/count")
    public Integer countNotification(@RequestBody Notification notification) {
        return notificationService.countNotification(notification, SessionUtils.getUserId());
    }
}
