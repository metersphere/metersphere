package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.request.AttachmentTransferRequest;
import io.metersphere.functional.request.FunctionalCaseAssociationFileRequest;
import io.metersphere.functional.request.FunctionalCaseDeleteFileRequest;
import io.metersphere.functional.request.FunctionalCaseFileRequest;
import io.metersphere.functional.service.FunctionalCaseAttachmentService;
import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.project.dto.filemanagement.FileAssociationDTO;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Resource
    private FileModuleService fileModuleService;

    @Resource
    private CommonFileService commonFileService;


    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-附件-关联文件列表分页接口")
    @RequiresPermissions(value = {
            PermissionConstants.FUNCTIONAL_CASE_READ,
            PermissionConstants.PROJECT_API_DEBUG_READ,
            PermissionConstants.PROJECT_API_DEFINITION_READ,
            PermissionConstants.PROJECT_API_DEFINITION_CASE_READ,
            PermissionConstants.PROJECT_API_SCENARIO_READ,
    }, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FileInformationResponse>> page(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.page(request);
    }


    @PostMapping("/preview")
    @Operation(summary = "用例管理-功能用例-附件/富文本(原图/文件)-文件预览")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
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
    @Operation(summary = "用例管理-功能用例-附件/富文本(原图/文件)-文件下载")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
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
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public String update(@PathVariable String projectId, @PathVariable String id) {
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.CASE_MANAGEMENT_CASE_CASE)
                .operator(SessionUtils.getUserId())
                .projectId(projectId)
                .build();
        return fileAssociationService.upgrade(id, fileLogRecord);
    }


    @PostMapping("/transfer")
    @Operation(summary = "用例管理-功能用例-附件-文件转存")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String transfer(@Validated @RequestBody AttachmentTransferRequest request) {
        byte[] fileByte = functionalCaseAttachmentService.getFileByte(request);
        FunctionalCaseAttachment attachment = functionalCaseAttachmentService.getAttachment(request);
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.CASE_MANAGEMENT_CASE_CASE)
                .operator(SessionUtils.getUserId())
                .projectId(request.getProjectId())
                .build();

        String fileId = null;
        try {
            FileAssociationDTO fileAssociationDTO = new FileAssociationDTO(request.getFileName(), attachment.getFileName(), fileByte, attachment.getCaseId(), FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE, fileLogRecord);
            fileAssociationDTO.setModuleId(request.getModuleId());
            fileId = fileAssociationService.transferAndAssociation(fileAssociationDTO);
            functionalCaseAttachmentService.deleteCaseAttachment(Arrays.asList(request.getFileId()), request.getCaseId(), request.getProjectId());
        } catch (MSException e) {
            throw new MSException(e.getMessage());
        } catch (Exception e) {
            throw new MSException(Translator.get("file.transfer.failed"));
        }
        return fileId;

    }


    @PostMapping("/upload/file")
    @Operation(summary = "用例管理-功能用例-上传文件并关联用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateFunctionalCaseFileLog(#request)", msClass = FunctionalCaseLogService.class)
    public void uploadFile(@Validated @RequestPart("request") FunctionalCaseAssociationFileRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        String userId = SessionUtils.getUserId();
        functionalCaseAttachmentService.uploadOrAssociationFile(request, file, userId);
    }

    @PostMapping("/delete/file")
    @Operation(summary = "用例管理-功能用例-删除文件并取消关联用例")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.deleteFunctionalCaseFileLog(#request)", msClass = FunctionalCaseLogService.class)
    public void deleteFile(@Validated @RequestBody FunctionalCaseDeleteFileRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseAttachmentService.deleteFile(request, userId);
    }

    @GetMapping("/options/{projectId}")
    @Operation(summary = "用例管理-功能用例-附件-转存目录下拉框")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> options(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @PostMapping("/upload/temp/file")
    @Operation(summary = "用例管理-功能用例-上传富文本里所需的文件资源，并返回文件ID")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_COMMENT})
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        return commonFileService.uploadTempImgFile(file);
    }

    @GetMapping(value = "/download/file/{projectId}/{fileId}/{compressed}")
    @Operation(summary = "用例管理-功能用例-预览上传的富文本里所需的文件资源原图")
    public ResponseEntity<byte[]> downloadImgById(@PathVariable String projectId, @PathVariable String fileId, @Schema(description = "查看压缩图片", requiredMode = Schema.RequiredMode.REQUIRED)
    @PathVariable("compressed") boolean compressed) {
        return functionalCaseAttachmentService.downloadImgById(projectId, fileId, compressed);
    }

}
