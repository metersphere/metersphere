package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.FucTestFileMapper;
import io.metersphere.base.mapper.LoadTestFileMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private FucTestFileMapper fucTestFileMapper;
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

    public FileMetadata getFucFileMetadataByTestId(String testId) {
        FucTestFileExample fucTestFileExample = new FucTestFileExample();
        fucTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<FucTestFile> loadTestFiles = fucTestFileMapper.selectByExample(fucTestFileExample);

        if (CollectionUtils.isEmpty(loadTestFiles)) {
            return null;
        }
        return fileMetadataMapper.selectByPrimaryKey(loadTestFiles.get(0).getFileId());
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public void deleteFileByTestId(String testId) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);
        loadTestFileMapper.deleteByExample(loadTestFileExample);

        if (!CollectionUtils.isEmpty(loadTestFiles)) {
            final List<String> fileIds = loadTestFiles.stream().map(LoadTestFile::getFileId).collect(Collectors.toList());

            FileMetadataExample fileMetadataExample = new FileMetadataExample();
            fileMetadataExample.createCriteria().andIdIn(fileIds);
            fileMetadataMapper.deleteByExample(fileMetadataExample);

            FileContentExample fileContentExample = new FileContentExample();
            fileContentExample.createCriteria().andFileIdIn(fileIds);
            fileContentMapper.deleteByExample(fileContentExample);
        }
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
}