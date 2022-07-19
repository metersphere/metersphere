package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtAttachmentModuleRelationMapper;
import io.metersphere.commons.constants.AttachmentSyncType;
import io.metersphere.commons.constants.AttachmentType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.service.FileService;
import io.metersphere.track.issue.IssueFactory;
import io.metersphere.track.request.attachment.AttachmentRequest;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author songcc
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AttachmentService {

    @Resource
    FileService fileService;
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private IssueFileMapper issueFileMapper;
    @Resource
    private TestCaseMapper testCaseMapper;
    @Resource
    private TestCaseFileMapper testCaseFileMapper;
    @Resource
    private FileAttachmentMetadataMapper fileAttachmentMetadataMapper;
    @Resource
    private AttachmentModuleRelationMapper attachmentModuleRelationMapper;
    @Resource
    private ExtAttachmentModuleRelationMapper extAttachmentModuleRelationMapper;

    public void uploadAttachment(AttachmentRequest request, MultipartFile file) {
        // 附件上传的前置校验
        if (AttachmentType.ISSUE.type().equals(request.getBelongType())) {
            IssuesWithBLOBs issues = issuesMapper.selectByPrimaryKey(request.getBelongId());
            if (issues == null) {
                MSException.throwException(Translator.get("issues_attachment_upload_not_found") + request.getBelongId());
            }
        } else if (AttachmentType.TEST_CASE.type().equals(request.getBelongType())) {
            TestCaseWithBLOBs testCase = testCaseMapper.selectByPrimaryKey(request.getBelongId());
            if (testCase == null) {
                MSException.throwException(Translator.get("test_case_attachment_upload_not_found") + request.getBelongId());
            }
        }

        // 上传MS平台
        FileAttachmentMetadata fileAttachmentMetadata = fileService.saveAttachment(file, request.getBelongType(), request.getBelongId());
        AttachmentModuleRelation attachmentModuleRelation = new AttachmentModuleRelation();
        attachmentModuleRelation.setRelationId(request.getBelongId());
        attachmentModuleRelation.setRelationType(request.getBelongType());
        attachmentModuleRelation.setAttachmentId(fileAttachmentMetadata.getId());
        attachmentModuleRelationMapper.insert(attachmentModuleRelation);

        // 附件上传完成后的后置操作
        // 缺陷类型的附件, 需单独同步第三方平台
        if (AttachmentType.ISSUE.type().equals(request.getBelongType())) {
            IssuesWithBLOBs issues = issuesMapper.selectByPrimaryKey(request.getBelongId());
            IssuesUpdateRequest updateRequest = new IssuesUpdateRequest();
            updateRequest.setPlatformId(issues.getPlatformId());
            File uploadFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
            Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                    .syncIssuesAttachment(updateRequest, uploadFile, AttachmentSyncType.UPLOAD);
        }
    }

    public void deleteAttachment(String attachmentId, String attachmentType) {
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(attachmentId);
        List<String> ids = List.of(attachmentId);
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andAttachmentIdIn(ids).andRelationTypeEqualTo(attachmentType);
        // 缺陷类型的附件, 需先同步第三方平台
        if (AttachmentType.ISSUE.type().equals(attachmentType)) {
            List<AttachmentModuleRelation> moduleRelations = attachmentModuleRelationMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(moduleRelations) && moduleRelations.size() == 1) {
                IssuesWithBLOBs issues = issuesMapper.selectByPrimaryKey(moduleRelations.get(0).getRelationId());
                IssuesUpdateRequest updateRequest = new IssuesUpdateRequest();
                updateRequest.setPlatformId(issues.getPlatformId());
                File deleteFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
                IssuesRequest issuesRequest = new IssuesRequest();
                issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                        .syncIssuesAttachment(updateRequest, deleteFile, AttachmentSyncType.DELETE);
            }
        }

        // 删除MS附件及关联数据
        fileService.deleteAttachment(ids);
        fileService.deleteFileAttachmentByIds(ids);
        attachmentModuleRelationMapper.deleteByExample(example);
    }

    public void deleteAttachment(AttachmentRequest request) {
        fileService.deleteAttachment(request.getBelongType(), request.getBelongId());
        List<String> attachmentIds = getAttachmentIdsByParam(request);
        if (CollectionUtils.isNotEmpty(attachmentIds)) {
            FileAttachmentMetadataExample fileAttachmentMetadataExample = new FileAttachmentMetadataExample();
            fileAttachmentMetadataExample.createCriteria().andIdIn(attachmentIds);
            fileAttachmentMetadataMapper.deleteByExample(fileAttachmentMetadataExample);
        }
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(request.getBelongId()).andRelationTypeEqualTo(request.getBelongType());
        attachmentModuleRelationMapper.deleteByExample(example);
    }

    public void copyAttachment(AttachmentRequest request) {
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(request.getCopyBelongId()).andRelationTypeEqualTo(request.getBelongType());
        List<AttachmentModuleRelation> attachmentModuleRelations = attachmentModuleRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(attachmentModuleRelations)) {
            attachmentModuleRelations.forEach(attachmentModuleRelation -> {
                FileAttachmentMetadata fileAttachmentMetadata = fileService.copyAttachment(attachmentModuleRelation.getAttachmentId(), request.getBelongType(), request.getBelongId());
                AttachmentModuleRelation record = new AttachmentModuleRelation();
                record.setRelationId(request.getBelongId());
                record.setRelationType(request.getBelongType());
                record.setAttachmentId(fileAttachmentMetadata.getId());
                attachmentModuleRelationMapper.insert(record);
            });
        }
    }

    public List<FileAttachmentMetadata> listMetadata(AttachmentRequest request) {
        List<String> attachmentIds = getAttachmentIdsByParam(request);
        if (CollectionUtils.isEmpty(attachmentIds)) {
            return new ArrayList<>();
        }
        FileAttachmentMetadataExample fileExample = new FileAttachmentMetadataExample();
        fileExample.createCriteria().andIdIn(attachmentIds);
        return fileAttachmentMetadataMapper.selectByExample(fileExample);
    }

    public List<String> getAttachmentIdsByParam(AttachmentRequest request) {
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(request.getBelongId()).andRelationTypeEqualTo(request.getBelongType());
        List<AttachmentModuleRelation> attachmentModuleRelations = attachmentModuleRelationMapper.selectByExample(example);
        List<String> attachmentIds = attachmentModuleRelations.stream().map(AttachmentModuleRelation::getAttachmentId)
                .collect(Collectors.toList());
        return  attachmentIds;
    }

    public void initAttachment() {
        List<AttachmentModuleRelation> attachmentModuleRelations = new ArrayList<>();
        List<IssueFile> issueFiles = issueFileMapper.selectByExample(new IssueFileExample());
        List<TestCaseFile> testCaseFiles = testCaseFileMapper.selectByExample(new TestCaseFileExample());
        if (CollectionUtils.isNotEmpty(issueFiles)) {
            issueFiles.forEach(issueFile -> {
                AttachmentModuleRelation relation = new AttachmentModuleRelation();
                relation.setAttachmentId(issueFile.getFileId());
                relation.setRelationId(issueFile.getIssueId());
                relation.setRelationType(AttachmentType.ISSUE.type());
                attachmentModuleRelations.add(relation);
            });
        }
        if (CollectionUtils.isNotEmpty(testCaseFiles)) {
            testCaseFiles.forEach(testCaseFile -> {
                AttachmentModuleRelation relation = new AttachmentModuleRelation();
                relation.setAttachmentId(testCaseFile.getFileId());
                relation.setRelationId(testCaseFile.getCaseId());
                relation.setRelationType(AttachmentType.TEST_CASE.type());
                attachmentModuleRelations.add(relation);
            });
        }
        extAttachmentModuleRelationMapper.batchInsert(attachmentModuleRelations);
    }
}
