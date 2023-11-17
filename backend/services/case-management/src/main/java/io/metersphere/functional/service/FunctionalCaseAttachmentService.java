package io.metersphere.functional.service;


import com.google.common.collect.Lists;
import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.domain.FunctionalCaseAttachmentExample;
import io.metersphere.functional.dto.FunctionalCaseAttachmentDTO;
import io.metersphere.functional.dto.FunctionalCaseDetailDTO;
import io.metersphere.functional.mapper.FunctionalCaseAttachmentMapper;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseAttachmentService {

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;

    @Resource
    private FileMetadataMapper fileMetadataMapper;

    @Resource
    private MinioRepository minioRepository;

    /**
     * 保存本地上传文件和用例关联关系
     *
     * @param fileId
     * @param file
     * @param caseId
     * @param isLocal
     * @param userId
     */
    public void saveCaseAttachment(String fileId, MultipartFile file, String caseId, Boolean isLocal, String userId) {
        FunctionalCaseAttachment caseAttachment = creatModule(fileId, file.getName(), file.getSize(), caseId, isLocal, userId);
        functionalCaseAttachmentMapper.insertSelective(caseAttachment);
    }


    /**
     * 功能用例上传附件
     *
     * @param request
     * @param files
     */
    public void uploadFile(FunctionalCaseAddRequest request, String caseId, List<MultipartFile> files, Boolean isLocal, String userId) {
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                String fileId = IDGenerator.nextStr();
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getName());
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(request.getProjectId(), caseId) + "/" + fileId);
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    minioRepository.saveFile(file, fileRequest);
                } catch (Exception e) {
                    throw new MSException("save file error");
                }
                saveCaseAttachment(fileId, file, caseId, isLocal, userId);
            });
        }
    }


    /**
     * 保存文件库文件与用例关联关系
     *
     * @param relateFileMetaIds
     * @param caseId
     * @param userId
     */
    public void relateFileMeta(List<String> relateFileMetaIds, String caseId, String userId) {
        if (CollectionUtils.isNotEmpty(relateFileMetaIds)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            FunctionalCaseAttachmentMapper sessionMapper = sqlSession.getMapper(FunctionalCaseAttachmentMapper.class);
            relateFileMetaIds.forEach(fileMetaId -> {
                FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileMetaId);
                FunctionalCaseAttachment caseAttachment = creatModule(fileMetadata.getId(), fileMetadata.getName(), fileMetadata.getSize(), caseId, false, userId);
                sessionMapper.insertSelective(caseAttachment);
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private FunctionalCaseAttachment creatModule(String fileId, String fileName, long fileSize, String caseId, Boolean isLocal, String userId) {
        FunctionalCaseAttachment caseAttachment = new FunctionalCaseAttachment();
        caseAttachment.setId(IDGenerator.nextStr());
        caseAttachment.setCaseId(caseId);
        caseAttachment.setFileId(fileId);
        caseAttachment.setFileName(fileName);
        caseAttachment.setSize(fileSize);
        caseAttachment.setLocal(isLocal);
        caseAttachment.setCreateUser(userId);
        caseAttachment.setCreateTime(System.currentTimeMillis());
        return caseAttachment;
    }


    /**
     * 获取附件信息
     *
     * @param functionalCaseDetailDTO
     */
    public void getAttachmentInfo(FunctionalCaseDetailDTO functionalCaseDetailDTO) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andCaseIdEqualTo(functionalCaseDetailDTO.getId());
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(caseAttachments)) {
            caseAttachments.stream().filter(caseAttachment -> !caseAttachment.getLocal()).forEach(caseAttachment -> {
                FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(caseAttachment.getFileId());
                caseAttachment.setFileName(fileMetadata.getName());
            });
        }
        List<FunctionalCaseAttachmentDTO> attachmentDTOs = Lists.transform(caseAttachments, (functionalCaseAttachment) -> {
            FunctionalCaseAttachmentDTO attachmentDTO = new FunctionalCaseAttachmentDTO();
            BeanUtils.copyBean(attachmentDTO, functionalCaseAttachment);
            return attachmentDTO;
        });
        functionalCaseDetailDTO.setAttachments(attachmentDTOs);
    }


    /**
     * 更新用例时删除文件 取消关联关系
     *
     * @param deleteFileMetaIds
     */
    public void deleteCaseAttachment(List<String> deleteFileMetaIds, String caseId, String projectId) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andFileIdIn(deleteFileMetaIds).andCaseIdEqualTo(caseId).andLocalEqualTo(true);
        List<FunctionalCaseAttachment> delAttachment = functionalCaseAttachmentMapper.selectByExample(example);
        example.clear();
        example.createCriteria().andFileIdIn(deleteFileMetaIds).andCaseIdEqualTo(caseId);
        functionalCaseAttachmentMapper.deleteByExample(example);
        this.deleteMinioFile(delAttachment, projectId, caseId);
    }


    private void deleteMinioFile(List<FunctionalCaseAttachment> files, String caseId, String projectId) {
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getFileName());
                fileRequest.setFolder(DefaultRepositoryDir.getFunctionalCaseDir(projectId, caseId) + "/" + file.getFileId());
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    minioRepository.delete(fileRequest);
                } catch (Exception e) {
                    throw new MSException("delete file error");
                }
            });
        }
    }

    /**
     * 通过caseId获取附件信息
     *
     * @param ids
     * @return
     */
    public Map<String, List<FunctionalCaseAttachment>> getAttachmentByCaseIds(List<String> ids) {
        FunctionalCaseAttachmentExample example = new FunctionalCaseAttachmentExample();
        example.createCriteria().andCaseIdIn(ids);
        List<FunctionalCaseAttachment> caseAttachments = functionalCaseAttachmentMapper.selectByExample(example);
        Map<String, List<FunctionalCaseAttachment>> attachmentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(caseAttachments)) {
            attachmentMap = caseAttachments.stream().collect(Collectors.groupingBy(FunctionalCaseAttachment::getCaseId));
        }
        return attachmentMap;
    }

    public void batchSaveAttachment(List<FunctionalCaseAttachment> attachments) {
        functionalCaseAttachmentMapper.batchInsert(attachments);
    }
}