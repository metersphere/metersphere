package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.LoadTestFileMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private FileContentMapper fileContentMapper;

    // 将上传的文件保存在内存，方便测试
    private Map<String, MultipartFile> fileMap = new ConcurrentHashMap<>();

    public void upload(String name, MultipartFile file) throws IOException {
        String result = new BufferedReader(new InputStreamReader(file.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        System.out.println(String.format("upload file: %s, content: \n%s", name, result));

        fileMap.put(name, file);
    }

    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);

        return fileContent.getFile();
    }

    public FileMetadata getFileMetadataByTestId(String testId) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);

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
}