package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.ApiTestFileMapper;
import io.metersphere.base.mapper.LoadTestFileMapper;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.exception.MSException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private ApiTestFileMapper ApiTestFileMapper;
    @Resource
    private FileContentMapper fileContentMapper;

    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);

        return fileContent.getFile();
    }

    public List<FileMetadata> getFileMetadataByTestId(String testId) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);

        if (CollectionUtils.isEmpty(loadTestFiles)) {
            return null;
        }
        List<String> fileIds = loadTestFiles.stream().map(LoadTestFile::getFileId).collect(Collectors.toList());
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileMetadataMapper.selectByExample(example);
    }

    public FileMetadata getApiFileMetadataByTestId(String testId) {
        ApiTestFileExample ApiTestFileExample = new ApiTestFileExample();
        ApiTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<ApiTestFile> loadTestFiles = ApiTestFileMapper.selectByExample(ApiTestFileExample);

        if (CollectionUtils.isEmpty(loadTestFiles)) {
            return null;
        }
        return fileMetadataMapper.selectByPrimaryKey(loadTestFiles.get(0).getFileId());
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public void deleteFileByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileMetadataMapper.deleteByExample(example);

        FileContentExample example2 = new FileContentExample();
        example2.createCriteria().andFileIdIn(ids);
        fileContentMapper.deleteByExample(example2);
    }

    public FileMetadata saveFile(MultipartFile file) {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setSize(file.getSize());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
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

    private FileType getFileType(String filename) {
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return FileType.valueOf(type.toUpperCase());
    }
}