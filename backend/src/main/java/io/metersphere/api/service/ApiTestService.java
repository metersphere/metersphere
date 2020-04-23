package io.metersphere.api.service;

import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.DeleteAPITestRequest;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.api.dto.SaveAPITestRequest;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiTestMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.service.FileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestService {

    @Resource
    private ApiTestMapper apiTestMapper;
    @Resource
    private ExtApiTestMapper extApiTestMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private FileService fileService;

    public List<APITestResult> list(QueryAPITestRequest request) {
        return extApiTestMapper.list(request);
    }

    public List<APITestResult> recentTest(QueryAPITestRequest request) {
        request.setRecent(true);
        return extApiTestMapper.list(request);
    }

    public String save(SaveAPITestRequest request) {
        final ApiTestWithBLOBs test;
        if (StringUtils.isNotBlank(request.getId())) {
            test = updateTest(request);
        } else {
            test = createTest(request);
        }
        return test.getId();
    }

    public ApiTestWithBLOBs get(String id) {
        return apiTestMapper.selectByPrimaryKey(id);
    }

    public void delete(DeleteAPITestRequest request) {
        apiTestMapper.deleteByPrimaryKey(request.getId());
    }

    public void run(SaveAPITestRequest request) {
        save(request);
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

}
