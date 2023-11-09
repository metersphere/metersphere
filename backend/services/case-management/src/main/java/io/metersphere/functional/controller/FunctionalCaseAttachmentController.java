package io.metersphere.functional.controller;

import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.utils.Pager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author wx
 */
@Tag(name = "用例管理-功能用例-附件")
@RestController
@RequestMapping("/attachment/")
public class FunctionalCaseAttachmentController {

    @Resource
    private FileMetadataService fileMetadataService;


    //TODO 附件操作：文件删除/文件下载/文件预览/文件转存/文件更新

    @PostMapping("/page")
    @Operation(summary = "功能用例-关联文件列表分页接口")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public Pager<List<FileInformationResponse>> page(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.page(request);
    }
}
