package io.metersphere.api.service;

import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.DeleteAPITestRequest;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.api.dto.SaveAPITestRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestFileMapper;
import io.metersphere.base.mapper.ApiTestMapper;
import io.metersphere.base.mapper.ext.ExtApiTestMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.service.FileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestService {

    @Resource
    private ApiTestMapper apiTestMapper;
    @Resource
    private ExtApiTestMapper extApiTestMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private JMeterService jMeterService;

    public List<APITestResult> list(QueryAPITestRequest request) {
        return extApiTestMapper.list(request);
    }

    public List<APITestResult> recentTest(QueryAPITestRequest request) {
        request.setRecent(true);
        return extApiTestMapper.list(request);
    }

    public String save(SaveAPITestRequest request, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }

        final ApiTestWithBLOBs test;
        if (StringUtils.isNotBlank(request.getId())) {
            // 删除原来的文件
            deleteFileByTestId(request.getId());
            test = updateTest(request);
        } else {
            test = createTest(request);
        }
        // 保存新文件
        files.forEach(file -> {
            final FileMetadata fileMetadata = fileService.saveFile(file);
            ApiTestFile apiTestFile = new ApiTestFile();
            apiTestFile.setTestId(test.getId());
            apiTestFile.setFileId(fileMetadata.getId());
            apiTestFileMapper.insert(apiTestFile);
        });
        return test.getId();
    }

    public ApiTestWithBLOBs get(String id) {
        return apiTestMapper.selectByPrimaryKey(id);
    }

    public void delete(DeleteAPITestRequest request) {
        deleteFileByTestId(request.getId());
        apiTestMapper.deleteByPrimaryKey(request.getId());
    }

    public void run(SaveAPITestRequest request, List<MultipartFile> files) {
        save(request, files);
        try {
            changeStatus(request.getId(), APITestStatus.Running);
            jMeterService.run(files.get(0).getInputStream());
        } catch (IOException e) {
            MSException.throwException(Translator.get("api_load_script_error"));
        }
    }

    public void changeStatus(String id, APITestStatus status) {
        ApiTestWithBLOBs apiTest = new ApiTestWithBLOBs();
        apiTest.setId(id);
        apiTest.setStatus(status.name());
        apiTestMapper.updateByPrimaryKeySelective(apiTest);
    }

    private ApiTestWithBLOBs updateTest(SaveAPITestRequest request) {
        final ApiTestWithBLOBs test = new ApiTestWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setProjectId(request.getProjectId());
        test.setScenarioDefinition(request.getScenarioDefinition());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Saved.name());
        apiTestMapper.updateByPrimaryKeySelective(test);
        return test;
    }

    private ApiTestWithBLOBs createTest(SaveAPITestRequest request) {
        ApiTestExample example = new ApiTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
        if (apiTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }

        final ApiTestWithBLOBs test = new ApiTestWithBLOBs();
        test.setId(UUID.randomUUID().toString());
        test.setName(request.getName());
        test.setProjectId(request.getProjectId());
        test.setScenarioDefinition(request.getScenarioDefinition());
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Saved.name());
        apiTestMapper.insert(test);
        return test;
    }

    private void deleteFileByTestId(String testId) {
        ApiTestFileExample ApiTestFileExample = new ApiTestFileExample();
        ApiTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<ApiTestFile> ApiTestFiles = apiTestFileMapper.selectByExample(ApiTestFileExample);
        apiTestFileMapper.deleteByExample(ApiTestFileExample);

        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            final List<String> fileIds = ApiTestFiles.stream().map(ApiTestFile::getFileId).collect(Collectors.toList());

            fileService.deleteFileByIds(fileIds);
        }
    }

}
