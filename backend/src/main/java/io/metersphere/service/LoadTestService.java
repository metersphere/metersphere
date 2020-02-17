package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.FileStoreMapper;
import io.metersphere.base.mapper.FileStoreResourceMapper;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.commons.constants.LoadTestFileType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.IOUtils;
import io.metersphere.controller.request.testplan.DeleteTestPlanRequest;
import io.metersphere.controller.request.testplan.QueryTestPlanRequest;
import io.metersphere.controller.request.testplan.SaveTestPlanRequest;
import io.metersphere.dto.LoadTestDTO;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestService {
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private FileStoreMapper fileStoreMapper;
    @Resource
    private FileStoreResourceMapper fileStoreResourceMapper;

    // 测试，模拟数据
    @PostConstruct
    public void initData() {
        if (!CollectionUtils.isEmpty(loadTestMapper.selectByExample(null))) {
            return;
        }

        final List<Project> projects = projectMapper.selectByExample(null);

        for (int i = 0; i < 100; i++) {
            final LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
            loadTest.setId(UUID.randomUUID().toString());
            loadTest.setName("load test " + i);
            loadTest.setProjectId(projects.get(RandomUtils.nextInt(0, projects.size())).getId());
            loadTest.setCreateTime(System.currentTimeMillis());
            loadTest.setUpdateTime(System.currentTimeMillis());
            loadTest.setScenarioDefinition(UUID.randomUUID().toString());
            loadTest.setDescription(UUID.randomUUID().toString());
            loadTestMapper.insert(loadTest);
        }
    }

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        return extLoadTestMapper.list(null);
    }

    public void delete(DeleteTestPlanRequest request) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andIdEqualTo(request.getId());

        loadTestMapper.deleteByExample(loadTestExample);
    }

    public void save(SaveTestPlanRequest request, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空！");
        }

        final FileStore fileStore = saveFileStore(file);

        final LoadTestWithBLOBs loadTest = saveLoadTest(request);

        FileStoreResource fileStoreResource = new FileStoreResource();
        fileStoreResource.setTestId(loadTest.getId());
        fileStoreResource.setFileId(fileStore.getId());
        fileStoreResource.setFileType(fileStore.getType());
        fileStoreResourceMapper.insert(fileStoreResource);
    }

    private LoadTestWithBLOBs saveLoadTest(SaveTestPlanRequest request) {
        final LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
        loadTest.setId(UUID.randomUUID().toString());
        loadTest.setName(request.getName());
        loadTest.setProjectId(request.getProjectId());
        loadTest.setCreateTime(System.currentTimeMillis());
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTest.setScenarioDefinition("todo");
        loadTest.setDescription("todo");
        loadTestMapper.insert(loadTest);
        return loadTest;
    }

    private FileStore saveFileStore(MultipartFile file) {
        final FileStore fileStore = new FileStore();
        fileStore.setId(UUID.randomUUID().toString());
        fileStore.setName(file.getOriginalFilename());
        fileStore.setSize(file.getSize());
        fileStore.setCreateTime(System.currentTimeMillis());
        fileStore.setUpdateTime(System.currentTimeMillis());
        fileStore.setType(LoadTestFileType.JMX.name());
        try {
            fileStore.setFile(IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            MSException.throwException(e);
        }
        fileStoreMapper.insert(fileStore);
        return fileStore;
    }
}
