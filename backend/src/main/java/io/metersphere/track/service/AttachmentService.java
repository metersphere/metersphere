package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtAttachmentModuleRelationMapper;
import io.metersphere.commons.constants.AttachmentSyncType;
import io.metersphere.commons.constants.AttachmentType;
import io.metersphere.commons.constants.FileAssociationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.service.FileService;
import io.metersphere.track.issue.IssueFactory;
import io.metersphere.track.request.attachment.AttachmentRequest;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
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
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    SqlSessionFactory sqlSessionFactory;

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
            // 本地附件
            List<String> localAttachments = attachmentModuleRelations.stream()
                    .filter(relation -> StringUtils.isEmpty(relation.getFileMetadataRefId()))
                    .map(AttachmentModuleRelation::getAttachmentId)
                    .filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            localAttachments.forEach(localAttachmentId -> {
                FileAttachmentMetadata fileAttachmentMetadata = fileService.copyAttachment(localAttachmentId, request.getBelongType(), request.getBelongId());
                AttachmentModuleRelation record = new AttachmentModuleRelation();
                record.setRelationId(request.getBelongId());
                record.setRelationType(request.getBelongType());
                record.setAttachmentId(fileAttachmentMetadata.getId());
                attachmentModuleRelationMapper.insert(record);
            });
            // 文件管理关联附件
            List<AttachmentModuleRelation> refAttachments = attachmentModuleRelations.stream()
                    .filter(relation -> StringUtils.isNotEmpty(relation.getFileMetadataRefId())).collect(Collectors.toList());
            refAttachments.forEach(refAttachment -> {
                refAttachment.setRelationId(request.getBelongId());
                attachmentModuleRelationMapper.insert(refAttachment);
                // 缺陷类型的附件, 关联时需单独同步第三方平台
                if (AttachmentType.ISSUE.type().equals(request.getBelongType())) {
                    String metadataRefId = getRefIdByAttachmentId(refAttachment.getAttachmentId());
                    FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(metadataRefId);
                    IssuesWithBLOBs issues = issuesMapper.selectByPrimaryKey(request.getBelongId());
                    IssuesUpdateRequest updateRequest = new IssuesUpdateRequest();
                    updateRequest.setPlatformId(issues.getPlatformId());
                    File refFile = downloadMetadataFile(metadataRefId, fileMetadata.getName());
                    IssuesRequest issuesRequest = new IssuesRequest();
                    issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                    Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                            .syncIssuesAttachment(updateRequest, refFile, AttachmentSyncType.UPLOAD);
                    FileUtils.deleteFile(FileUtils.ATTACHMENT_TMP_DIR + File.separator + fileMetadata.getName());
                }
            });
        }
    }

    public List<FileAttachmentMetadata> listMetadata(AttachmentRequest request) {
        List<FileAttachmentMetadata> attachments = new ArrayList<FileAttachmentMetadata>();
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(request.getBelongId()).andRelationTypeEqualTo(request.getBelongType());
        List<AttachmentModuleRelation> attachmentModuleRelations = attachmentModuleRelationMapper.selectByExample(example);
        Map<String, String> relationMap = attachmentModuleRelations.stream()
                .collect(Collectors.toMap(AttachmentModuleRelation::getAttachmentId,
                        relation -> relation.getFileMetadataRefId() == null ? "" : relation.getFileMetadataRefId()));
        List<String> attachmentIds = attachmentModuleRelations.stream().map(AttachmentModuleRelation::getAttachmentId)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(attachmentIds)) {
            FileAttachmentMetadataExample fileExample = new FileAttachmentMetadataExample();
            fileExample.createCriteria().andIdIn(attachmentIds);
            List<FileAttachmentMetadata> fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByExample(fileExample);
            fileAttachmentMetadata.forEach(file -> {
                String fileRefId = relationMap.get(file.getId());
                if (StringUtils.isEmpty(fileRefId)) {
                    file.setIsLocal(Boolean.TRUE);
                    file.setIsRelatedDeleted(Boolean.FALSE);
                } else {
                    file.setIsLocal(Boolean.FALSE);
                    FileAssociation fileAssociation = fileAssociationMapper.selectByPrimaryKey(fileRefId);
                    if (fileAssociation != null) {
                        // 关联文件信息同步
                        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileAssociation.getFileMetadataId());
                        file.setIsRelatedDeleted(Boolean.FALSE);
                        file.setName(fileMetadata.getName());
                        file.setSize(fileMetadata.getSize());
                        file.setCreator(fileMetadata.getCreateUser());
                        file.setCreateTime(fileMetadata.getCreateTime());
                    } else {
                        file.setIsRelatedDeleted(Boolean.TRUE);
                    }
                }
            });
            attachments.addAll(fileAttachmentMetadata);
        }
        return attachments;
    }

    public void relate(AttachmentRequest request) {
        // 批量关联文件管理
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FileAssociationMapper associationBatchMapper = sqlSession.getMapper(FileAssociationMapper.class);
        AttachmentModuleRelationMapper attachmentModuleRelationBatchMapper = sqlSession.getMapper(AttachmentModuleRelationMapper.class);
        FileAttachmentMetadataMapper fileAttachmentMetadataBatchMapper = sqlSession.getMapper(FileAttachmentMetadataMapper.class);
        if (CollectionUtils.isNotEmpty(request.getMetadataRefIds())) {
            request.getMetadataRefIds().forEach(metadataRefId -> {
                FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(metadataRefId);
                FileAssociation fileAssociation = new FileAssociation();
                fileAssociation.setId(UUID.randomUUID().toString());
                fileAssociation.setFileMetadataId(metadataRefId);
                fileAssociation.setFileType(fileMetadata.getType());
                if (AttachmentType.TEST_CASE.type().equals(request.getBelongType())) {
                    fileAssociation.setType(FileAssociationType.TEST_CASE.name());
                } else {
                    fileAssociation.setType(FileAssociationType.ISSUE.name());
                }
                fileAssociation.setProjectId(fileMetadata.getProjectId());
                fileAssociation.setSourceItemId(metadataRefId);
                fileAssociation.setSourceId(request.getBelongId());
                associationBatchMapper.insert(fileAssociation);
                AttachmentModuleRelation record = new AttachmentModuleRelation();
                record.setRelationId(request.getBelongId());
                record.setRelationType(request.getBelongType());
                record.setFileMetadataRefId(fileAssociation.getId());
                record.setAttachmentId(UUID.randomUUID().toString());
                attachmentModuleRelationBatchMapper.insert(record);
                FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
                BeanUtils.copyBean(fileAttachmentMetadata, fileMetadata);
                fileAttachmentMetadata.setId(record.getAttachmentId());
                fileAttachmentMetadata.setCreator(fileMetadata.getCreateUser() == null ? "" : fileMetadata.getCreateUser());
                fileAttachmentMetadata.setFilePath(fileMetadata.getPath() == null ? "" : fileMetadata.getPath());
                fileAttachmentMetadataBatchMapper.insert(fileAttachmentMetadata);
                // 缺陷类型的附件, 关联时需单独同步第三方平台
                if (AttachmentType.ISSUE.type().equals(request.getBelongType())) {
                    IssuesWithBLOBs issues = issuesMapper.selectByPrimaryKey(request.getBelongId());
                    IssuesUpdateRequest updateRequest = new IssuesUpdateRequest();
                    updateRequest.setPlatformId(issues.getPlatformId());
                    File refFile = downloadMetadataFile(metadataRefId, fileMetadata.getName());
                    IssuesRequest issuesRequest = new IssuesRequest();
                    issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                    Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                            .syncIssuesAttachment(updateRequest, refFile, AttachmentSyncType.UPLOAD);
                    FileUtils.deleteFile(FileUtils.ATTACHMENT_TMP_DIR + File.separator + fileMetadata.getName());
                }
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }

    }

    public void unrelated(AttachmentRequest request) {
        // 缺陷类型的附件, 取消关联时同步第三方平台
        if (AttachmentType.ISSUE.type().equals(request.getBelongType())) {
            IssuesWithBLOBs issues = issuesMapper.selectByPrimaryKey(request.getBelongId());
            request.getMetadataRefIds().forEach(metadataRefId -> {
                FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(metadataRefId);
                IssuesUpdateRequest updateRequest = new IssuesUpdateRequest();
                updateRequest.setPlatformId(issues.getPlatformId());
                File deleteFile = new File(FileUtils.ATTACHMENT_TMP_DIR + File.separator + fileAttachmentMetadata.getName());
                IssuesRequest issuesRequest = new IssuesRequest();
                issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                        .syncIssuesAttachment(updateRequest, deleteFile, AttachmentSyncType.DELETE);
            });
        }
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(request.getBelongId())
                .andRelationTypeEqualTo(request.getBelongType())
                .andAttachmentIdIn(request.getMetadataRefIds());
        List<AttachmentModuleRelation> relations = attachmentModuleRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(relations)) {
            List<String> refIds = relations.stream().map(AttachmentModuleRelation::getFileMetadataRefId).collect(Collectors.toList());
            FileAssociationExample associationExample = new FileAssociationExample();
            associationExample.createCriteria().andIdIn(refIds);
            fileAssociationMapper.deleteByExample(associationExample);
        }
        FileAttachmentMetadataExample exampleAttachment = new FileAttachmentMetadataExample();
        exampleAttachment.createCriteria().andIdIn(request.getMetadataRefIds());
        fileAttachmentMetadataMapper.deleteByExample(exampleAttachment);
        attachmentModuleRelationMapper.deleteByExample(example);
    }

    public List<String> getAttachmentIdsByParam(AttachmentRequest request) {
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(request.getBelongId()).andRelationTypeEqualTo(request.getBelongType());
        List<AttachmentModuleRelation> attachmentModuleRelations = attachmentModuleRelationMapper.selectByExample(example);
        List<String> attachmentIds = attachmentModuleRelations.stream().map(AttachmentModuleRelation::getAttachmentId)
                .collect(Collectors.toList());
        return  attachmentIds;
    }

    public String getRefIdByAttachmentId(String attachmentId) {
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andAttachmentIdEqualTo(attachmentId);
        List<AttachmentModuleRelation> relations = attachmentModuleRelationMapper.selectByExample(example);
        String associationId = relations.get(0).getFileMetadataRefId();
        FileAssociation fileAssociation = fileAssociationMapper.selectByPrimaryKey(associationId);
        return fileAssociation.getFileMetadataId();
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

    public File downloadMetadataFile(String fileMetadataRefId, String filename) {
        byte[] refFileBytes = fileMetadataService.loadFileAsBytes(fileMetadataRefId);
        return FileUtils.byteToFile(refFileBytes, FileUtils.ATTACHMENT_TMP_DIR, filename);
    }
}
