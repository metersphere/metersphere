package io.metersphere.api.service.definition;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.mapper.*;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ApiTestCaseService {

    public static final Long ORDER_STEP = 5000L;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiTestCaseFollowerMapper apiTestCaseFollowerMapper;
    @Resource
    private MinioRepository minioRepository;

    public ApiTestCase addCase(ApiTestCaseAddRequest request, List<MultipartFile> files, String userId) {
        ApiTestCase testCase = new ApiTestCase();
        testCase.setId(IDGenerator.nextStr());
        BeanUtils.copyBean(testCase, request);
        ApiDefinition apiDefinition = getApiDefinition(request.getApiDefinitionId());
        testCase.setNum(NumGenerator.nextNum(request.getProjectId()+ "_" + apiDefinition.getNum(), ApplicationNumScope.API_TEST_CASE));
        testCase.setApiDefinitionId(request.getApiDefinitionId());
        testCase.setName(request.getName());
        testCase.setPos(getNextOrder(request.getProjectId()));
        testCase.setProjectId(request.getProjectId());
        checkProjectExist(testCase.getProjectId());
        checkNameExist(testCase);
        testCase.setVersionId(apiDefinition.getVersionId());
        testCase.setPriority(request.getPriority());
        testCase.setStatus(request.getStatus());
        testCase.setCreateUser(userId);
        testCase.setUpdateUser(userId);
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            testCase.setTags(JSON.toJSONString(request.getTags()));
        }
        apiTestCaseMapper.insertSelective(testCase);

        ApiTestCaseBlob caseBlob = new ApiTestCaseBlob();
        caseBlob.setId(testCase.getId());
        caseBlob.setRequest(request.getRequest().getBytes());
        apiTestCaseBlobMapper.insert(caseBlob);
        uploadBodyFile(files, testCase.getId(), request.getProjectId());
        return testCase;
    }

    private void checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    private ApiDefinition getApiDefinition(String apiDefinitionId) {
        //判断是否存在
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdEqualTo(apiDefinitionId).andDeletedEqualTo(false);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiDefinitions)) {
            throw new MSException(Translator.get("api_definition_not_exist"));
        }
        return apiDefinitions.get(0);
    }

    public void checkNameExist(ApiTestCase apiTestCase) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(apiTestCase.getProjectId())
                .andApiDefinitionIdEqualTo(apiTestCase.getApiDefinitionId())
                .andNameEqualTo(apiTestCase.getName()).andIdNotEqualTo(apiTestCase.getId()).andDeletedEqualTo(false);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            throw new MSException(Translator.get("api_test_case_name_exist"));
        }
    }

    public Long getNextOrder(String projectId) {
        Long pos = extApiTestCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public void uploadBodyFile(List<MultipartFile> files, String caseId, String projectId) {
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getName());
                fileRequest.setProjectId(StringUtils.join(MsFileUtils.API_CASE_DIR, projectId));
                fileRequest.setResourceId(caseId);
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    minioRepository.saveFile(file, fileRequest);
                } catch (Exception e) {
                    LogUtils.info("上传body文件失败:  文件名称:" + file.getName(), e);
                    throw new MSException(Translator.get("file_upload_fail"));
                }
            });
        }
    }

    private ApiTestCase checkResourceExist(String id) {
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(id);
        if (testCase == null) {
            throw new MSException(Translator.get("api_test_case_not_exist"));
        }
        return testCase;
    }

    public ApiTestCaseDTO get(String id, String userId) {
        ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
        ApiTestCase testCase = checkResourceExist(id);
        ApiTestCaseBlob testCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);
        BeanUtils.copyBean(apiTestCaseDTO, testCase);
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id).andUserIdEqualTo(userId);
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        apiTestCaseDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        apiTestCaseDTO.setRequest(ApiDataUtils.parseObject(new String(testCaseBlob.getRequest()), AbstractMsTestElement.class));
        return apiTestCaseDTO;
    }

    public void deleteToGc(String id, String userId) {
        checkResourceExist(id);
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId(id);
        apiTestCase.setDeleted(true);
        apiTestCase.setDeleteUser(userId);
        apiTestCase.setDeleteTime(System.currentTimeMillis());
        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
    }

    public void recover(String id) {
        checkResourceExist(id);
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId(id);
        apiTestCase.setDeleted(false);
        apiTestCase.setDeleteUser(null);
        apiTestCase.setDeleteTime(null);
        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
    }

    public void follow(String id, String userId) {
        checkResourceExist(id);
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id).andUserIdEqualTo(userId);
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(followers)) {
            ApiTestCaseFollower follower = new ApiTestCaseFollower();
            follower.setCaseId(id);
            follower.setUserId(userId);
            apiTestCaseFollowerMapper.insert(follower);
        }
    }

    public void unfollow(String id, String userId) {
        checkResourceExist(id);
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id).andUserIdEqualTo(userId);
        apiTestCaseFollowerMapper.deleteByExample(example);
    }

    public void delete(String id) {
        ApiTestCase apiCase = checkResourceExist(id);
        apiTestCaseMapper.deleteByPrimaryKey(id);
        apiTestCaseBlobMapper.deleteByPrimaryKey(id);
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id);
        apiTestCaseFollowerMapper.deleteByExample(example);
        try {
            FileRequest request = new FileRequest();
            request.setProjectId(StringUtils.join(MsFileUtils.API_CASE_DIR, id));
            request.setProjectId(StringUtils.join(MsFileUtils.API_CASE_DIR, apiCase.getProjectId()));
            request.setResourceId(id);
            minioRepository.deleteFolder(request);
        } catch (Exception e) {
            LogUtils.info("删除body文件失败:  文件名称:" + id, e);
        }
    }
}
