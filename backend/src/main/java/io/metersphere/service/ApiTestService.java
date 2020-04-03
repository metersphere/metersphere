package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiTestMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.testplan.*;
import io.metersphere.dto.ApiTestDTO;
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
public class ApiTestService {
    @Resource
    private ApiTestMapper ApiTestMapper;
    @Resource
    private ExtApiTestMapper extApiTestMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private ApiTestFileMapper ApiTestFileMapper;
    @Resource
    private FileService fileService;

    public List<ApiTestDTO> list(QueryTestPlanRequest request) {
        return extApiTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {
        ApiTestMapper.deleteByPrimaryKey(request.getId());

        fileService.deleteFileByTestId(request.getId());
    }

    public String save(SaveTestPlanRequest request, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空！");
        }

        final FileMetadata fileMetadata = saveFile(file);

        final ApiTestWithBLOBs ApiTest = saveApiTest(request);

        ApiTestFile ApiTestFile = new ApiTestFile();
        ApiTestFile.setTestId(ApiTest.getId());
        ApiTestFile.setFileId(fileMetadata.getId());
        ApiTestFileMapper.insert(ApiTestFile);

        return ApiTest.getId();
    }

    private ApiTestWithBLOBs saveApiTest(SaveTestPlanRequest request) {

        ApiTestExample example = new ApiTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
        if (ApiTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("function_test_already_exists"));
        }

        final ApiTestWithBLOBs ApiTes = new ApiTestWithBLOBs();
        ApiTes.setId(UUID.randomUUID().toString());
        ApiTes.setName(request.getName());
        ApiTes.setProjectId(request.getProjectId());
        ApiTes.setCreateTime(System.currentTimeMillis());
        ApiTes.setUpdateTime(System.currentTimeMillis());
        ApiTes.setDescription("todo");
        ApiTes.setRuntimeConfiguration(request.getRuntimeConfiguration());
        ApiTestMapper.insert(ApiTes);
        return ApiTes;
    }

    private FileMetadata saveFile(MultipartFile file) {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setSize(file.getSize());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setType("jmx");
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
            ApiTestFile ApiTestFile = new ApiTestFile();
            ApiTestFile.setTestId(request.getId());
            ApiTestFile.setFileId(fileMetadata.getId());
            ApiTestFileMapper.insert(ApiTestFile);
        }

        final ApiTestWithBLOBs ApiTest = ApiTestMapper.selectByPrimaryKey(request.getId());
        if (ApiTest == null) {
            MSException.throwException("无法编辑测试，未找到测试：" + request.getId());
        } else {
            ApiTest.setName(request.getName());
            ApiTest.setProjectId(request.getProjectId());
            ApiTest.setUpdateTime(System.currentTimeMillis());
            ApiTest.setDescription("todo");
            ApiTest.setRuntimeConfiguration(request.getRuntimeConfiguration());
            ApiTestMapper.updateByPrimaryKeySelective(ApiTest);
        }

        return request.getId();
    }

    public void run(RunTestPlanRequest request) {
        final ApiTestWithBLOBs ApiTest = ApiTestMapper.selectByPrimaryKey(request.getId());
//        if (ApiTest == null) {
//            MSException.throwException("无法运行测试，未找到测试：" + request.getId());
//        }
//
//        final List<FileMetadata> fileMetadataList = fileService.getFileMetadataByTestId(request.getId());
//        if (fileMetadataList == null) {
//            MSException.throwException("无法运行测试，无法获取测试文件元信息，测试ID：" + request.getId());
//        }
//
//        final FileContent fileContent = fileService.getFileContent(fileMetadata.getId());
//        if (fileContent == null) {
//            MSException.throwException("无法运行测试，无法获取测试文件内容，测试ID：" + request.getId());
//        }
//
//        System.out.println("开始运行：" + ApiTest.getName());
//        final Engine engine = EngineFactory.createEngine(fileMetadata.getType());
//        if (engine == null) {
//            MSException.throwException(String.format("无法运行测试，未识别测试文件类型，测试ID：%s，文件类型：%s",
//                    request.getId(),
//                    fileMetadata.getType()));
//        }
//
//        boolean init = true;
//        try {
////            init = engine.init(EngineFactory.createContext(ApiTest, fileMetadata, fileContent));
//        } catch (Exception e) {
//            MSException.throwException(e);
//        }
//        if (!init) {
//            MSException.throwException(String.format("无法运行测试，初始化运行环境失败，测试ID：%s", request.getId()));
//        }
//
////        engine.start();

        /// todo：通过调用stop方法能够停止正在运行的engine，但是如果部署了多个backend实例，页面发送的停止请求如何定位到具体的engine
    }

    public List<ApiTestDTO> recentTestPlans(QueryTestPlanRequest request) {
        // 查询最近的测试计划
        request.setRecent(true);
        return extApiTestMapper.list(request);
    }

    public ApiTestDTO get(String testId) {
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setId(testId);
        List<ApiTestDTO> testDTOS = extApiTestMapper.list(request);
        if (!CollectionUtils.isEmpty(testDTOS)) {
            return testDTOS.get(0);
        }
        return null;
    }

    public String getRuntimeConfiguration(String testId) {
        ApiTestWithBLOBs ApiTestWithBLOBs = ApiTestMapper.selectByPrimaryKey(testId);
        return Optional.ofNullable(ApiTestWithBLOBs).orElse(new ApiTestWithBLOBs()).getRuntimeConfiguration();
    }

}
