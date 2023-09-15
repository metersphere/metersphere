package io.metersphere.project.controller;


import io.metersphere.project.service.NoticeTemplateService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.OptionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目管理-消息设置-模版设置")
@RestController
@RequestMapping("notice/template/")
public class NoticeTemplateController {
    @Resource
    private NoticeTemplateService noticeTemplateService;

    @GetMapping("get/fields/{type}")
    @Operation(summary = "项目管理-消息设置-模版设置-获取消息模版字段")
    @RequiresPermissions(PermissionConstants.PROJECT_MESSAGE_READ)
    public List<OptionDTO> getTemplateFields(@PathVariable String type) {
        return noticeTemplateService.getTemplateFields(type);
    }

}

