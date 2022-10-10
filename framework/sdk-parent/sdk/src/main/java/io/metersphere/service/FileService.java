package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.ext.BaseFileMetadataMapper;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.request.QueryProjectFileRequest;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private BaseFileMetadataMapper extFileMetadataMapper;

    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);

        return fileContent.getFile();
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public void setFileContent(String fileId, byte[] content) {
        FileContent record = new FileContent();
        record.setFile(content);
        record.setFileId(fileId);
        fileContentMapper.updateByPrimaryKeySelective(record);
    }

    public void deleteFileByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileMetadataMapper.deleteByExample(example);

        List<String> refIdList = extFileMetadataMapper.selectRefIdsByIds(ids);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(refIdList)) {
            //删除其余版本的文件
            example.clear();
            example.createCriteria().andRefIdIn(refIdList);
            fileMetadataMapper.deleteByExample(example);
        }

        FileContentExample example2 = new FileContentExample();
        example2.createCriteria().andFileIdIn(ids);
        fileContentMapper.deleteByExample(example2);
    }

    public void deleteFileRelatedByIds(List<String> ids) {
        deleteFileByIds(ids);
    }

    public FileMetadata saveFile(MultipartFile file, String projectId, String fileId) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        if (StringUtils.isEmpty(fileId)) {
            fileMetadata.setId(UUID.randomUUID().toString());
        } else {
            fileMetadata.setId(fileId);
        }
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setSize(file.getSize());
        fileMetadata.setProjectId(projectId);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        try {
            fileContent.setFile(file.getBytes());
        } catch (IOException e) {
            MSException.throwException(e);
        }
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata saveFile(MultipartFile file, String projectId) {
        return saveFile(file, projectId, null);
    }

    public FileMetadata saveFile(MultipartFile file) {
        return saveFile(file, null);
    }

    public FileMetadata saveFile(File file, byte[] fileByte) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getName());
        fileMetadata.setSize(file.length());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata insertFileByFileName(File file, byte[] fileByte, String projectId) {
        if (StringUtils.isEmpty(file.getName())) {
            return null;
        } else {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(file.getName());
            List<FileMetadataWithBLOBs> fileMetadatasInDataBase = fileMetadataMapper.selectByExampleWithBLOBs(example);
            if (CollectionUtils.isEmpty(fileMetadatasInDataBase)) {
                final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
                fileMetadata.setId(UUID.randomUUID().toString());
                fileMetadata.setName(file.getName());
                fileMetadata.setSize(file.length());
                fileMetadata.setProjectId(projectId);
                fileMetadata.setCreateTime(System.currentTimeMillis());
                fileMetadata.setUpdateTime(System.currentTimeMillis());
                FileType fileType = getFileType(fileMetadata.getName());
                fileMetadata.setType(fileType.name());
                fileMetadata.setLatest(true);
                fileMetadata.setRefId(fileMetadata.getId());
                fileMetadataMapper.insert(fileMetadata);

                FileContent fileContent = new FileContent();
                fileContent.setFileId(fileMetadata.getId());
                fileContent.setFile(fileByte);
                fileContentMapper.insert(fileContent);
                return fileMetadata;
            } else {
                FileMetadataWithBLOBs fileMetadata = fileMetadatasInDataBase.get(0);
                fileMetadata.setName(file.getName());
                fileMetadata.setSize(file.length());
                fileMetadata.setProjectId(projectId);
                fileMetadata.setUpdateTime(System.currentTimeMillis());
                FileType fileType = getFileType(fileMetadata.getName());
                fileMetadata.setType(fileType.name());
                fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);

                fileContentMapper.deleteByPrimaryKey(fileMetadata.getId());
                FileContent fileContent = new FileContent();
                fileContent.setFileId(fileMetadata.getId());
                fileContent.setFile(fileByte);
                fileContentMapper.insert(fileContent);
                return fileMetadata;
            }
        }

    }

    public FileMetadata saveFile(File file, byte[] fileByte, String projectId) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getName());
        fileMetadata.setSize(file.length());
        fileMetadata.setProjectId(projectId);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata saveFile(byte[] fileByte, String fileName, Long fileSize) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(fileName);
        fileMetadata.setSize(fileSize);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata copyFile(String fileId) {
        FileMetadataWithBLOBs fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        FileContent fileContent = getFileContent(fileId);
        if (fileMetadata != null && fileContent != null) {
            fileMetadata.setId(UUID.randomUUID().toString());
            fileMetadata.setCreateTime(System.currentTimeMillis());
            fileMetadata.setUpdateTime(System.currentTimeMillis());
            fileMetadata.setLatest(true);
            fileMetadata.setRefId(fileMetadata.getId());
            fileMetadataMapper.insert(fileMetadata);

            fileContent.setFileId(fileMetadata.getId());
            fileContentMapper.insert(fileContent);
        }
        return fileMetadata;
    }

    private FileType getFileType(String filename) {
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return FileType.valueOf(type.toUpperCase());
    }

    public void deleteFileById(String fileId) {
        deleteFileByIds(Collections.singletonList(fileId));
    }

    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }

    public void updateFileMetadata(FileMetadataWithBLOBs fileMetadata) {
        fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);
    }

    public boolean isFileExsits(String fileId) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileId);
        long fileCount = fileMetadataMapper.countByExample(example);
        if (fileCount > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<FileMetadata> getProjectFiles(String projectId, QueryProjectFileRequest request) {
        FileMetadataExample example = new FileMetadataExample();
        FileMetadataExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        if (!StringUtils.isEmpty(request.getName())) {
            criteria.andNameEqualTo(request.getName());
        }
        return fileMetadataMapper.selectByExample(example);
    }

    public List<FileMetadata> getFileMetadataByIds(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return new ArrayList<>();
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileMetadataMapper.selectByExample(example);
    }

    public RsaKey checkRsaKey() {
        String key = "ms.login.rsa.key";
        FileContent value = getFileContent(key);
        if (value == null) {
            try {
                RsaKey rsaKey = RsaUtil.getRsaKey();
                byte[] bytes = SerializationUtils.serialize(rsaKey);
                final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
                fileMetadata.setId(key);
                fileMetadata.setName(key);
                fileMetadata.setSize((long) bytes.length);
                fileMetadata.setCreateTime(System.currentTimeMillis());
                fileMetadata.setUpdateTime(System.currentTimeMillis());
                fileMetadata.setType("RSA_KEY");
                fileMetadata.setLatest(true);
                fileMetadata.setRefId(fileMetadata.getId());
                fileMetadataMapper.insert(fileMetadata);

                FileContent fileContent = new FileContent();
                fileContent.setFileId(fileMetadata.getId());
                fileContent.setFile(bytes);
                fileContentMapper.insert(fileContent);
                return rsaKey;
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return SerializationUtils.deserialize(value.getFile());
    }
}
