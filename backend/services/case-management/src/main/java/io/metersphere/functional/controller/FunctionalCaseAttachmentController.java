package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.request.FunctionalCaseFileRequest;
import io.metersphere.functional.service.FunctionalCaseAttachmentService;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author wx
 */
@Tag(name = "用例管理-功能用例-附件")
@RestController
@RequestMapping("/attachment")
public class FunctionalCaseAttachmentController {

    @Resource
    private FileMetadataService fileMetadataService;

    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;

    @Resource
    private FileAssociationService fileAssociationService;


    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-附件-关联文件列表分页接口")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public Pager<List<FileInformationResponse>> page(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.page(request);
    }


    @PostMapping("/preview")
    @Operation(summary = "用例管理-功能用例-附件-文件预览")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public ResponseEntity<byte[]> preview(@Validated @RequestBody FunctionalCaseFileRequest request) throws Exception {
        if (request.getLocal()) {
            //本地
            return functionalCaseAttachmentService.downloadPreviewImgById(request);
        } else {
            //文件库
            return fileMetadataService.downloadPreviewImgById(request.getFileId());
        }
    }


    @PostMapping("/download")
    @Operation(summary = "用例管理-功能用例-附件-文件下载")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public ResponseEntity<byte[]> download(@Validated @RequestBody FunctionalCaseFileRequest request) throws Exception {
        if (request.getLocal()) {
            //本地
            return functionalCaseAttachmentService.downloadPreviewImgById(request);
        } else {
            //文件库
            return fileMetadataService.downloadById(request.getFileId());
        }
    }


    @PostMapping("/check-update")
    @Operation(summary = "用例管理-功能用例-附件-检查文件是否存在更新")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public List<String> checkUpdate(@RequestBody List<String> fileIds) {
        return fileAssociationService.checkFilesVersion(fileIds);
    }


    @GetMapping("/update/{projectId}/{id}")
    @Operation(summary = "用例管理-功能用例-附件-更新文件")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public String update(@PathVariable String projectId, @PathVariable String id) {
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.FUNCTIONAL_CASE)
                .operator(SessionUtils.getUserId())
                .projectId(projectId)
                .build();
        return fileAssociationService.upgrade(id, fileLogRecord);
    }


    @PostMapping("/transfer")
    @Operation(summary = "用例管理-功能用例-附件-文件转存")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public String transfer(@Validated @RequestBody FunctionalCaseFileRequest request) {
        byte[] fileByte = functionalCaseAttachmentService.getFileByte(request);
        FunctionalCaseAttachment attachment = functionalCaseAttachmentService.getAttachment(request);
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.FUNCTIONAL_CASE)
                .operator(SessionUtils.getUserId())
                .projectId(request.getProjectId())
                .build();

        String fileId = null;
        try {
            fileId = fileAssociationService.transferAndAssociation(attachment.getFileName(), fileByte, attachment.getCaseId(), FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE, fileLogRecord);
            functionalCaseAttachmentService.deleteCaseAttachment(Arrays.asList(request.getFileId()), request.getCaseId(), request.getProjectId());
        } catch (Exception e) {
            throw new MSException(Translator.get("file.transfer.error"));
        }
        return fileId;

    }

}
