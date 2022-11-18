package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtAttachmentModuleRelationMapper;
import io.metersphere.commons.constants.FileAssociationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.AttachmentType;
import io.metersphere.i18n.Translator;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.platform.domain.SyncIssuesAttachmentRequest;
import io.metersphere.request.attachment.AttachmentRequest;
import io.metersphere.service.issue.platform.IssueFactory;
import io.metersphere.xmind.utils.FileUtil;
import io.metersphere.xpack.track.dto.AttachmentSyncType;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author songcc
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AttachmentService {

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
    private BaseUserService baseUserService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    PlatformPluginService platformPluginService;

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
        FileAttachmentMetadata fileAttachmentMetadata = saveAttachment(file, request.getBelongType(), request.getBelongId());
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

            if (PlatformPluginService.isPluginPlatform(issues.getPlatform())) {
                syncIssuesAttachment(issues, uploadFile, AttachmentSyncType.UPLOAD);
            } else {
                IssuesRequest issuesRequest = new IssuesRequest();
                issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
                Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                        .syncIssuesAttachment(updateRequest, uploadFile, AttachmentSyncType.UPLOAD);
            }
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

                if (PlatformPluginService.isPluginPlatform(issues.getPlatform())) {
                    syncIssuesAttachment(issues, deleteFile, AttachmentSyncType.DELETE);
                } else {
                    IssuesRequest issuesRequest = new IssuesRequest();
                    issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                    issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
                    Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                            .syncIssuesAttachment(updateRequest, deleteFile, AttachmentSyncType.DELETE);
                }
            }
        }

        // 删除MS附件及关联数据
        deleteAttachmentByIds(ids);
        deleteFileAttachmentByIds(ids);
        attachmentModuleRelationMapper.deleteByExample(example);
    }

    public void deleteAttachment(AttachmentRequest request) {
        deleteAttachmentFile(request.getBelongType(), request.getBelongId());
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
                FileAttachmentMetadata fileAttachmentMetadata = copyAttachment(localAttachmentId, request.getBelongType(), request.getBelongId());
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

                    if (PlatformPluginService.isPluginPlatform(issues.getPlatform())) {
                        syncIssuesAttachment(issues, refFile, AttachmentSyncType.UPLOAD);
                    } else {
                        IssuesRequest issuesRequest = new IssuesRequest();
                        issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                        issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
                        Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                                .syncIssuesAttachment(updateRequest, refFile, AttachmentSyncType.UPLOAD);
                    }
                    FileUtils.deleteFile(FileUtils.ATTACHMENT_TMP_DIR + File.separator + fileMetadata.getName());
                }
            });
        }
    }

    public void syncIssuesAttachment(IssuesWithBLOBs issues, File refFile, AttachmentSyncType attachmentSyncType) {
        SyncIssuesAttachmentRequest attachmentRequest = new SyncIssuesAttachmentRequest();
        attachmentRequest.setPlatformId(issues.getPlatformId());
        attachmentRequest.setFile(refFile);
        attachmentRequest.setSyncType(attachmentSyncType.syncOperateType());
        platformPluginService.getPlatform(issues.getPlatform())
                .syncIssuesAttachment(attachmentRequest);
    }

    public List<FileAttachmentMetadata> listMetadata(AttachmentRequest request) {
        List<FileAttachmentMetadata> attachments = new ArrayList<FileAttachmentMetadata>();
        AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
        example.createCriteria().andRelationIdEqualTo(request.getBelongId()).andRelationTypeEqualTo(request.getBelongType());
        List<AttachmentModuleRelation> attachmentModuleRelations = attachmentModuleRelationMapper.selectByExample(example);
        Map<String, String> relationMap = attachmentModuleRelations.stream()
                .collect(Collectors.toMap(AttachmentModuleRelation::getAttachmentId,
                        relation -> relation.getFileMetadataRefId() == null ? StringUtils.EMPTY : relation.getFileMetadataRefId()));
        List<String> attachmentIds = attachmentModuleRelations.stream().map(AttachmentModuleRelation::getAttachmentId)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(attachmentIds)) {
            FileAttachmentMetadataExample fileExample = new FileAttachmentMetadataExample();
            fileExample.createCriteria().andIdIn(attachmentIds);
            List<FileAttachmentMetadata> fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByExample(fileExample);
            Map<String, List<User>> userMap = baseUserService.getUserList().stream().collect(Collectors.groupingBy(User::getId));
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
                        List<User> users = userMap.get(fileMetadata.getCreateUser());
                        if (CollectionUtils.isNotEmpty(users)) {
                            file.setCreator(users.get(0).getName());
                        } else {
                            file.setCreator(fileMetadata.getCreateUser());
                        }
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
                fileAttachmentMetadata.setCreator(fileMetadata.getCreateUser() == null ? StringUtils.EMPTY : fileMetadata.getCreateUser());
                fileAttachmentMetadata.setFilePath(fileMetadata.getPath() == null ? StringUtils.EMPTY : fileMetadata.getPath());
                fileAttachmentMetadataBatchMapper.insert(fileAttachmentMetadata);
                // 缺陷类型的附件, 关联时需单独同步第三方平台
                if (AttachmentType.ISSUE.type().equals(request.getBelongType())) {
                    IssuesWithBLOBs issues = issuesMapper.selectByPrimaryKey(request.getBelongId());
                    IssuesUpdateRequest updateRequest = new IssuesUpdateRequest();
                    updateRequest.setPlatformId(issues.getPlatformId());
                    File refFile = downloadMetadataFile(metadataRefId, fileMetadata.getName());

                    if (PlatformPluginService.isPluginPlatform(issues.getPlatform())) {
                        syncIssuesAttachment(issues, refFile, AttachmentSyncType.UPLOAD);
                    } else {
                        IssuesRequest issuesRequest = new IssuesRequest();
                        issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                        issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
                        Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                                .syncIssuesAttachment(updateRequest, refFile, AttachmentSyncType.UPLOAD);
                        FileUtils.deleteFile(FileUtils.ATTACHMENT_TMP_DIR + File.separator + fileMetadata.getName());
                    }
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

                if (PlatformPluginService.isPluginPlatform(issues.getPlatform())) {
                    syncIssuesAttachment(issues, deleteFile, AttachmentSyncType.UPLOAD);
                } else {
                    IssuesRequest issuesRequest = new IssuesRequest();
                    issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                    issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
                    Objects.requireNonNull(IssueFactory.createPlatform(issues.getPlatform(), issuesRequest))
                            .syncIssuesAttachment(updateRequest, deleteFile, AttachmentSyncType.DELETE);
                }
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
        return attachmentIds;
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

    public byte[] getAttachmentBytes(String id) {
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
        File attachmentFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        return FileUtils.fileToByte(attachmentFile);
    }

    public ResponseEntity<byte[]> downloadLocalAttachment(String id) {
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
        File attachmentFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        byte[] bytes = FileUtils.fileToByte(attachmentFile);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + MetadataUtils.getFileName(fileAttachmentMetadata.getName(), fileAttachmentMetadata.getType()) + "\"")
                .contentLength(bytes.length)
                .body(bytes);
    }

    public MultipartFile getAttachmentMultipartFile(String id) {
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
        File attachmentFile = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        return FileUtil.fileToMultipartFile(attachmentFile);
    }

    public void deleteFileAttachmentByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileAttachmentMetadataExample example = new FileAttachmentMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileAttachmentMetadataMapper.deleteByExample(example);
    }

    public FileAttachmentMetadata saveAttachment(MultipartFile file, String attachmentType, String belongId) {
        String uploadPath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        FileUtils.uploadFile(file, uploadPath);
        final FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
        fileAttachmentMetadata.setId(UUID.randomUUID().toString());
        fileAttachmentMetadata.setName(file.getOriginalFilename());
        fileAttachmentMetadata.setType(getFileTypeWithoutEnum(fileAttachmentMetadata.getName()));
        fileAttachmentMetadata.setSize(file.getSize());
        fileAttachmentMetadata.setCreateTime(System.currentTimeMillis());
        fileAttachmentMetadata.setUpdateTime(System.currentTimeMillis());
        fileAttachmentMetadata.setCreator(SessionUtils.getUser().getName());
        fileAttachmentMetadata.setFilePath(uploadPath);
        fileAttachmentMetadataMapper.insert(fileAttachmentMetadata);
        return fileAttachmentMetadata;
    }

    public FileAttachmentMetadata saveAttachmentByBytes(byte[] bytes, String attachmentType, String belongId, String attachmentName) {
        String uploadPath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        File parentFile = new File(uploadPath);
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try (OutputStream os = new FileOutputStream(uploadPath + "/" + attachmentName)) {
            InputStream in = new ByteArrayInputStream(bytes);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            os.flush();

            final FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
            fileAttachmentMetadata.setId(UUID.randomUUID().toString());
            fileAttachmentMetadata.setName(attachmentName);
            fileAttachmentMetadata.setType(getFileTypeWithoutEnum(attachmentName));
            fileAttachmentMetadata.setSize(Integer.valueOf(bytes.length).longValue());
            fileAttachmentMetadata.setCreateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setUpdateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setCreator(SessionUtils.getUser().getName());
            fileAttachmentMetadata.setFilePath(uploadPath);
            fileAttachmentMetadataMapper.insert(fileAttachmentMetadata);
            return fileAttachmentMetadata;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteAttachmentByIds(List<String> ids) {
        for (String id : ids) {
            FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(id);
            FileUtils.deleteFile(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
        }
    }

    public void deleteAttachmentFile(String attachmentType, String belongId) {
        String deletePath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        FileUtils.deleteDir(deletePath);
    }

    public FileAttachmentMetadata copyAttachment(String fileId, String attachmentType, String belongId) {
        String copyPath = FileUtils.ATTACHMENT_DIR + "/" + attachmentType + "/" + belongId;
        FileAttachmentMetadata fileAttachmentMetadata = fileAttachmentMetadataMapper.selectByPrimaryKey(fileId);
        if (fileAttachmentMetadata != null) {
            File copyFile = new File(copyPath);
            if (!copyFile.exists()) {
                FileUtils.copyFolder(fileAttachmentMetadata.getFilePath(), copyPath);
            }
            fileAttachmentMetadata.setId(UUID.randomUUID().toString());
            fileAttachmentMetadata.setCreateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setUpdateTime(System.currentTimeMillis());
            fileAttachmentMetadata.setCreator(SessionUtils.getUser().getName());
            fileAttachmentMetadata.setFilePath(copyPath);
            fileAttachmentMetadataMapper.insert(fileAttachmentMetadata);
        }
        return fileAttachmentMetadata;
    }

    public List<FileAttachmentMetadata> getFileAttachmentMetadataByCaseId(String caseId) {
        TestCaseFileExample testCaseFileExample = new TestCaseFileExample();
        testCaseFileExample.createCriteria().andCaseIdEqualTo(caseId);
        final List<TestCaseFile> testCaseFiles = testCaseFileMapper.selectByExample(testCaseFileExample);

        if (org.apache.commons.collections.CollectionUtils.isEmpty(testCaseFiles)) {
            return new ArrayList<>();
        }

        List<String> fileIds = testCaseFiles.stream().map(TestCaseFile::getFileId).collect(Collectors.toList());
        FileAttachmentMetadataExample example = new FileAttachmentMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileAttachmentMetadataMapper.selectByExample(example);
    }

    public List<FileAttachmentMetadata> getFileAttachmentMetadataByIssueId(String issueId) {
        IssueFileExample issueFileExample = new IssueFileExample();
        issueFileExample.createCriteria().andIssueIdEqualTo(issueId);
        final List<IssueFile> issueFiles = issueFileMapper.selectByExample(issueFileExample);

        if (org.apache.commons.collections.CollectionUtils.isEmpty(issueFiles)) {
            return new ArrayList<>();
        }

        List<String> fileIds = issueFiles.stream().map(IssueFile::getFileId).collect(Collectors.toList());
        FileAttachmentMetadataExample example = new FileAttachmentMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileAttachmentMetadataMapper.selectByExample(example);
    }

    public FileAttachmentMetadata getFileAttachmentMetadataByFileId(String fileId) {
        return fileAttachmentMetadataMapper.selectByPrimaryKey(fileId);
    }

    public List<FileMetadata> getFileMetadataByCaseId(String caseId) {
        TestCaseFileExample testCaseFileExample = new TestCaseFileExample();
        testCaseFileExample.createCriteria().andCaseIdEqualTo(caseId);
        final List<TestCaseFile> testCaseFiles = testCaseFileMapper.selectByExample(testCaseFileExample);

        if (org.apache.commons.collections.CollectionUtils.isEmpty(testCaseFiles)) {
            return new ArrayList<>();
        }

        List<String> fileIds = testCaseFiles.stream().map(TestCaseFile::getFileId).collect(Collectors.toList());
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileMetadataMapper.selectByExample(example);
    }

    private String getFileTypeWithoutEnum(String filename) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(filename)) {
            return StringUtils.EMPTY;
        }
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return type.toUpperCase();
    }

}
