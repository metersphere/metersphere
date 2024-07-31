package io.metersphere.functional.controller;

import io.metersphere.functional.dto.CaseReviewHistoryDTO;
import io.metersphere.functional.request.FunctionalCaseFileRequest;
import io.metersphere.functional.request.ReviewFunctionalCaseRequest;
import io.metersphere.functional.service.FunctionalCaseAttachmentService;
import io.metersphere.functional.service.ReviewFunctionalCaseService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "用例管理-用例评审-评审功能用例")
@RestController
@RequestMapping("/review/functional/case")
public class ReviewFunctionalCaseController {

    @Resource
    private ReviewFunctionalCaseService reviewFunctionalCaseService;
    
    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;
    
    @Resource
    private CommonFileService commonFileService;

    @PostMapping("/save")
    @Operation(summary = "用例管理-用例评审-评审功能用例-提交评审")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_REVIEW)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void saveReview(@Validated @RequestBody ReviewFunctionalCaseRequest request) {
        reviewFunctionalCaseService.saveReview(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/list/{reviewId}/{caseId}")
    @Operation(summary = "用例管理-用例评审-评审功能用例-获取用例评审历史")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    public List<CaseReviewHistoryDTO> getCaseReviewHistoryList(@PathVariable String reviewId, @PathVariable String caseId) {
        return reviewFunctionalCaseService.getCaseReviewHistoryList(reviewId,  caseId);
    }

    @PostMapping("/upload/temp/file")
    @Operation(summary = "用例管理-用例评审-上传富文本里所需的文件资源，并返回文件ID")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_REVIEW)
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        return commonFileService.uploadTempImgFile(file);
    }

    @PostMapping("/preview")
    @Operation(summary = "用例管理-用例评审-附件/富文本(原图/文件)-文件预览")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_REVIEW)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ResponseEntity<byte[]> preview(@Validated @RequestBody FunctionalCaseFileRequest request) throws Exception {
        return functionalCaseAttachmentService.downloadPreviewImgById(request);

    }

    @PostMapping("/download")
    @Operation(summary = "用例管理-功能用例-附件/富文本(原图/文件)-文件下载")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_REVIEW)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ResponseEntity<byte[]> download(@Validated @RequestBody FunctionalCaseFileRequest request) throws Exception {
        return functionalCaseAttachmentService.downloadPreviewImgById(request);
    }

    @GetMapping(value = "/download/file/{projectId}/{fileId}/{compressed}")
    @Operation(summary = "用例管理-功能用例-预览上传的富文本里所需的文件资源原图")
    public ResponseEntity<byte[]> downloadImgById(@PathVariable String projectId, @PathVariable String fileId, @PathVariable boolean compressed) throws Exception {
        return functionalCaseAttachmentService.downloadImgById(projectId, fileId, compressed);
    }

}
