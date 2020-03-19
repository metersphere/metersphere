package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.LoadTestFileMapper;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.commons.constants.EngineType;
import io.metersphere.commons.constants.TestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.testplan.*;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineFactory;
import io.metersphere.i18n.Translator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestService {
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

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        return extLoadTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {
        loadTestMapper.deleteByPrimaryKey(request.getId());

        fileService.deleteFileByTestId(request.getId());
    }

    public String save(SaveTestPlanRequest request, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }

        final FileMetadata fileMetadata = saveFile(file);

        final LoadTestWithBLOBs loadTest = saveLoadTest(request);

        LoadTestFile loadTestFile = new LoadTestFile();
        loadTestFile.setTestId(loadTest.getId());
        loadTestFile.setFileId(fileMetadata.getId());
        loadTestFileMapper.insert(loadTestFile);

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
        fileMetadata.setType("JMX");
        // TODO engine 选择
        fileMetadata.setEngine(EngineType.DOCKER.name());
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

    public String edit(EditTestPlanRequest request, MultipartFile file) {
        // 新选择了一个文件，删除原来的文件
        if (file != null) {
            fileService.deleteFileByTestId(request.getId());
            final FileMetadata fileMetadata = saveFile(file);
            LoadTestFile loadTestFile = new LoadTestFile();
            loadTestFile.setTestId(request.getId());
            loadTestFile.setFileId(fileMetadata.getId());
            loadTestFileMapper.insert(loadTestFile);
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
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
        }

        return request.getId();
    }

    public void run(RunTestPlanRequest request) {
        final LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(request.getId());
        if (loadTest == null) {
            MSException.throwException(Translator.get("run_load_test_not_found") + request.getId());
        }

        final FileMetadata fileMetadata = fileService.getFileMetadataByTestId(request.getId());
        if (fileMetadata == null) {
            MSException.throwException(Translator.get("run_load_test_file_not_found") + request.getId());
        }

        final FileContent fileContent = fileService.getFileContent(fileMetadata.getId());
        if (fileContent == null) {
            MSException.throwException(Translator.get("run_load_test_file_content_not_found") + request.getId());
        }

        LogUtil.info("Load test started " + loadTest.getName());
        // engine type (DOCKER|KUBERNETES)
        // todo set type
        final Engine engine = EngineFactory.createEngine(fileMetadata.getEngine());
        if (engine == null) {
            MSException.throwException(String.format("Test cannot be run，test ID：%s，file type：%s",
                    request.getId(),
                    fileMetadata.getType()));
        }

        boolean init = true;
        try {
            init = engine.init(EngineFactory.createContext(loadTest, fileMetadata, fileContent));
        } catch (Exception e) {
            MSException.throwException(e);
        }
        if (!init) {
            MSException.throwException(Translator.get("run_load_test_file_init_error") + request.getId());
        }

        engine.start();
        // 标记running状态
        loadTest.setStatus(TestStatus.Running.name());
        loadTestMapper.updateByPrimaryKeySelective(loadTest);
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
}
