package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtFunctionalTestMapper;
import io.metersphere.commons.constants.EngineType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.testplan.*;
import io.metersphere.dto.FunctionalTestDTO;
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
public class FuctionalTestService {
    @Resource
    private FucTestMapper fucTestMapper;
    @Resource
    private ExtFunctionalTestMapper extFucTestMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private FucTestFileMapper fucTestFileMapper;
    @Resource
    private FileService fileService;

    public List<FunctionalTestDTO> list(QueryTestPlanRequest request) {
        return extFucTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {
        fucTestMapper.deleteByPrimaryKey(request.getId());

        fileService.deleteFileByTestId(request.getId());
    }

    public String save(SaveTestPlanRequest request, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空！");
        }

        final FileMetadata fileMetadata = saveFile(file);

        final FucTestWithBLOBs fucTest = saveFucTest(request);

        FucTestFile fucTestFile = new FucTestFile();
        fucTestFile.setTestId(fucTest.getId());
        fucTestFile.setFileId(fileMetadata.getId());
        fucTestFileMapper.insert(fucTestFile);

        return fucTest.getId();
    }

    private FucTestWithBLOBs saveFucTest(SaveTestPlanRequest request) {

        FucTestExample example = new FucTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
        if (fucTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("function_test_already_exists"));
        }

        final FucTestWithBLOBs fucTes = new FucTestWithBLOBs();
        fucTes.setId(UUID.randomUUID().toString());
        fucTes.setName(request.getName());
        fucTes.setProjectId(request.getProjectId());
        fucTes.setCreateTime(System.currentTimeMillis());
        fucTes.setUpdateTime(System.currentTimeMillis());
        fucTes.setDescription("todo");
        fucTes.setRuntimeConfiguration(request.getRuntimeConfiguration());
        fucTestMapper.insert(fucTes);
        return fucTes;
    }

    private FileMetadata saveFile(MultipartFile file) {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setSize(file.getSize());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setType("jmx");
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
            FucTestFile fucTestFile = new FucTestFile();
            fucTestFile.setTestId(request.getId());
            fucTestFile.setFileId(fileMetadata.getId());
            fucTestFileMapper.insert(fucTestFile);
        }

        final FucTestWithBLOBs fucTest = fucTestMapper.selectByPrimaryKey(request.getId());
        if (fucTest == null) {
            MSException.throwException("无法编辑测试，未找到测试：" + request.getId());
        } else {
            fucTest.setName(request.getName());
            fucTest.setProjectId(request.getProjectId());
            fucTest.setUpdateTime(System.currentTimeMillis());
            fucTest.setDescription("todo");
            fucTest.setRuntimeConfiguration(request.getRuntimeConfiguration());
            fucTestMapper.updateByPrimaryKeySelective(fucTest);
        }

        return request.getId();
    }

    public void run(RunTestPlanRequest request) {
        final FucTestWithBLOBs fucTest = fucTestMapper.selectByPrimaryKey(request.getId());
//        if (fucTest == null) {
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
//        System.out.println("开始运行：" + fucTest.getName());
//        final Engine engine = EngineFactory.createEngine(fileMetadata.getType());
//        if (engine == null) {
//            MSException.throwException(String.format("无法运行测试，未识别测试文件类型，测试ID：%s，文件类型：%s",
//                    request.getId(),
//                    fileMetadata.getType()));
//        }
//
//        boolean init = true;
//        try {
////            init = engine.init(EngineFactory.createContext(fucTest, fileMetadata, fileContent));
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

    public List<FunctionalTestDTO> recentTestPlans(QueryTestPlanRequest request) {
        // 查询最近的测试计划
        request.setRecent(true);
        return extFucTestMapper.list(request);
    }

    public FunctionalTestDTO get(String testId) {
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setId(testId);
        List<FunctionalTestDTO> testDTOS = extFucTestMapper.list(request);
        if (!CollectionUtils.isEmpty(testDTOS)) {
            return testDTOS.get(0);
        }
        return null;
    }

    public String getRuntimeConfiguration(String testId) {
        FucTestWithBLOBs fucTestWithBLOBs = fucTestMapper.selectByPrimaryKey(testId);
        return Optional.ofNullable(fucTestWithBLOBs).orElse(new FucTestWithBLOBs()).getRuntimeConfiguration();
    }

}
