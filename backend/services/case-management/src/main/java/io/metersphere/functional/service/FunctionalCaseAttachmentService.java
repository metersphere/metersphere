package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.mapper.FunctionalCaseAttachmentMapper;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


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
     * 保存文件库文件与用例关联关系
     *
     * @param relateFileMetaIds
     * @param caseId
     * @param userId
     */
    public void relateFileMeta(List<String> relateFileMetaIds, String caseId, String userId) {
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
}