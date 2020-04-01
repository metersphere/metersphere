package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.constants.TestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.testplan.*;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineFactory;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestService {
    private static final String HEADERS = "timestamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        return extLoadTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {
        loadTestMapper.deleteByPrimaryKey(request.getId());

        fileService.deleteFileByTestId(request.getId());
    }

    public String save(SaveTestPlanRequest request, List<MultipartFile> files) {
        if (files == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        final LoadTestWithBLOBs loadTest = saveLoadTest(request);
        files.forEach(file -> {
            final FileMetadata fileMetadata = saveFile(file);
            LoadTestFile loadTestFile = new LoadTestFile();
            loadTestFile.setTestId(loadTest.getId());
            loadTestFile.setFileId(fileMetadata.getId());
            loadTestFileMapper.insert(loadTestFile);
        });
        return loadTest.getId();
    }

    private LoadTestWithBLOBs saveLoadTest(SaveTestPlanRequest request) {

        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
        if (loadTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }

        final LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
        loadTest.setId(UUID.randomUUID().toString());
        loadTest.setName(request.getName());
        loadTest.setProjectId(request.getProjectId());
        loadTest.setCreateTime(System.currentTimeMillis());
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTest.setScenarioDefinition("todo");
        loadTest.setDescription("todo");
        loadTest.setTestResourcePoolId(request.getTestResourcePoolId());
        loadTest.setLoadConfiguration(request.getLoadConfiguration());
        loadTest.setAdvancedConfiguration(request.getAdvancedConfiguration());
        loadTestMapper.insert(loadTest);
        return loadTest;
    }

    private FileMetadata saveFile(MultipartFile file) {
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

    public String edit(EditTestPlanRequest request, List<MultipartFile> files) {
        // 新选择了一个文件，删除原来的文件
        List<FileMetadata> updatedFiles = request.getUpdatedFileList();
        List<FileMetadata> originFiles = fileService.getFileMetadataByTestId(request.getId());
        List<String> updatedFileIds = updatedFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        // 相减
        List<String> deleteFileIds = ListUtils.subtract(originFileIds, updatedFileIds);
        fileService.deleteFileByIds(deleteFileIds);

        if (files != null) {
            files.forEach(file -> {
                final FileMetadata fileMetadata = saveFile(file);
                LoadTestFile loadTestFile = new LoadTestFile();
                loadTestFile.setTestId(request.getId());
                loadTestFile.setFileId(fileMetadata.getId());
                loadTestFileMapper.insert(loadTestFile);
            });
        }

        final LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(request.getId());
        if (loadTest == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + request.getId());
        } else {
            loadTest.setName(request.getName());
            loadTest.setProjectId(request.getProjectId());
            loadTest.setUpdateTime(System.currentTimeMillis());
            loadTest.setScenarioDefinition("todo");
            loadTest.setDescription("todo");
            loadTest.setLoadConfiguration(request.getLoadConfiguration());
            loadTest.setAdvancedConfiguration(request.getAdvancedConfiguration());
            loadTest.setTestResourcePoolId(request.getTestResourcePoolId());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
        }

        return request.getId();
    }

    public void run(RunTestPlanRequest request) {
        final LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(request.getId());
        if (loadTest == null) {
            MSException.throwException(Translator.get("run_load_test_not_found") + request.getId());
        }
        if (TestStatus.Running.name().equals(loadTest.getStatus())) {
            MSException.throwException(Translator.get("load_test_is_running"));
        }

        LogUtil.info("Load test started " + loadTest.getName());
        // engine type (NODE|K8S)
        final Engine engine = EngineFactory.createEngine(loadTest);
        if (engine == null) {
            MSException.throwException(String.format("Test cannot be run，test ID：%s", request.getId()));
        }
        LoadTestReportWithBLOBs testReport = new LoadTestReportWithBLOBs();
        testReport.setId(engine.getReportId());
        testReport.setCreateTime(engine.getStartTime());
        testReport.setUpdateTime(engine.getStartTime());
        testReport.setTestId(loadTest.getId());
        testReport.setName(loadTest.getName());

        // 启动测试
        try {
            engine.start();
            // 标记running状态
            loadTest.setStatus(TestStatus.Starting.name());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);

            testReport.setContent(HEADERS);
            testReport.setStatus(TestStatus.Starting.name());
            loadTestReportMapper.insertSelective(testReport);
            // append \n
            extLoadTestReportMapper.appendLine(testReport.getId(), "\n");

        } catch (Exception e) {
            loadTest.setStatus(TestStatus.Error.name());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
            //
            testReport.setStatus(TestStatus.Error.name());
            testReport.setDescription(e.getMessage());
            loadTestReportMapper.insertSelective(testReport);
        }
        // todo：通过调用stop方法能够停止正在运行的engine，但是如果部署了多个backend实例，页面发送的停止请求如何定位到具体的engine
    }

    public List<LoadTestDTO> recentTestPlans(QueryTestPlanRequest request) {
        // 查询最近的测试计划
        request.setRecent(true);
        return extLoadTestMapper.list(request);
    }

    public LoadTestDTO get(String testId) {
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setId(testId);
        List<LoadTestDTO> testDTOS = extLoadTestMapper.list(request);
        if (!CollectionUtils.isEmpty(testDTOS)) {
            return testDTOS.get(0);
        }
        return null;
    }

    public String getAdvancedConfiguration(String testId) {
        LoadTestWithBLOBs loadTestWithBLOBs = loadTestMapper.selectByPrimaryKey(testId);
        return Optional.ofNullable(loadTestWithBLOBs).orElse(new LoadTestWithBLOBs()).getAdvancedConfiguration();
    }

    public String getLoadConfiguration(String testId) {
        LoadTestWithBLOBs loadTestWithBLOBs = loadTestMapper.selectByPrimaryKey(testId);
        return Optional.ofNullable(loadTestWithBLOBs).orElse(new LoadTestWithBLOBs()).getLoadConfiguration();
    }

    public List<LoadTestWithBLOBs> selectByTestResourcePoolId(String resourcePoolId) {
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andTestResourcePoolIdEqualTo(resourcePoolId);
        return loadTestMapper.selectByExampleWithBLOBs(example);
    }
}
