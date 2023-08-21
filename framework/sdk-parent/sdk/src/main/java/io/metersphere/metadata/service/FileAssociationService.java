package io.metersphere.metadata.service;

import io.metersphere.base.domain.FileAssociation;
import io.metersphere.base.domain.FileAssociationExample;
import io.metersphere.base.mapper.ExtFileAssociationMapper;
import io.metersphere.base.mapper.FileAssociationMapper;
import io.metersphere.request.BodyFile;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Service
public class FileAssociationService {
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtFileAssociationMapper extFileAssociationMapper;

    public void save(FileAssociation fileAssociation) {
        fileAssociationMapper.insert(fileAssociation);
    }

    public void deleteByFileId(String fileId) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andFileMetadataIdEqualTo(fileId);
        fileAssociationMapper.deleteByExample(example);
    }

    public void deleteByFileIdsAndResourceIds(List<String> resourceIds, List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria()
                .andFileMetadataIdIn(fileIds)
                .andSourceIdIn(resourceIds);
        fileAssociationMapper.deleteByExample(example);
    }

    public void deleteByResourceId(String sourceId) {
        if (StringUtils.isNotEmpty(sourceId)) {
            FileAssociationExample example = new FileAssociationExample();
            example.createCriteria().andSourceIdEqualTo(sourceId);
            fileAssociationMapper.deleteByExample(example);
        }
    }

    public void deleteByResourceIds(List<String> sourceIds) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdIn(sourceIds);
        fileAssociationMapper.deleteByExample(example);
    }

    public void deleteByResourceItemId(String id) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceItemIdEqualTo(id);
        fileAssociationMapper.deleteByExample(example);
    }


    public List<FileAssociation> getByFileId(String fileId) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andFileMetadataIdEqualTo(fileId);
        return fileAssociationMapper.selectByExample(example);
    }

    public List<FileAssociation> getByFileIds(List<String> fileIds) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andFileMetadataIdIn(fileIds);
        return fileAssociationMapper.selectByExample(example);
    }

    public List<FileAssociation> getByResourceId(String sourceId) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(sourceId);
        return fileAssociationMapper.selectByExample(example);
    }

    public List<FileAssociation> getByResourceIdAndType(String sourceId, String type) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceId)
                .andTypeEqualTo(type);
        return fileAssociationMapper.selectByExample(example);
    }

    public List<FileAssociation> getByResourceIds(List<String> sourceIds) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdIn(sourceIds);
        return fileAssociationMapper.selectByExample(example);
    }

    public List<FileAssociation> getByResourceItemId(String id) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceItemIdEqualTo(id);
        return fileAssociationMapper.selectByExample(example);
    }

    public void save(List<BodyFile> files, String type, String sourceId) {
        if (!CollectionUtils.isEmpty(files)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            FileAssociationMapper batchMapper = sqlSession.getMapper(FileAssociationMapper.class);
            files.forEach(item -> {
                FileAssociation fileAssociation = new FileAssociation();
                fileAssociation.setId(UUID.randomUUID().toString());
                fileAssociation.setFileMetadataId(item.getFileId());
                fileAssociation.setFileType(item.getFileType());
                fileAssociation.setType(type);
                fileAssociation.setProjectId(item.getProjectId());
                fileAssociation.setSourceItemId(item.getId());
                fileAssociation.setSourceId(sourceId);
                batchMapper.insert(fileAssociation);
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public List<String> getFileIdsByProjectIdAndType(String projectId, String fileAssociationType) {
        return extFileAssociationMapper.getFileIdsByProjectIdAndType(projectId, fileAssociationType);
    }

    public void deleteByProjectIdAndType(String projectId, String fileAssociationType) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId)
                .andTypeEqualTo(fileAssociationType);
        fileAssociationMapper.deleteByExample(example);
    }
}
