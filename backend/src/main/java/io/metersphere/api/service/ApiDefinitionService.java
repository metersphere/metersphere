package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiTestFileMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.service.FileService;
import org.apache.jorphan.collections.HashTree;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.security.util.Cache;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionService {
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private JMeterService jMeterService;
    private static Cache cache = Cache.newHardMemoryCache(0, 3600 * 24);

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    public List<ApiDefinitionResult> list(ApiDefinitionRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiDefinitionResult> resList = apiDefinitionMapper.list(request);
        if (!resList.isEmpty()) {
            List<String> ids = resList.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList());
            List<ApiComputeResult> results = apiDefinitionMapper.selectByIds(ids);
            Map<String, ApiComputeResult> resultMap = results.stream().collect(Collectors.toMap(ApiComputeResult::getApiDefinitionId, Function.identity()));
            for (ApiDefinitionResult res : resList) {
                ApiComputeResult compRes = resultMap.get(res.getId());
                if (compRes != null) {
                    res.setCaseTotal(compRes.getCaseTotal());
                    res.setCasePassingRate(compRes.getPassRate());
                    res.setCaseStatus(compRes.getStatus());
                } else {
                    res.setCaseTotal("-");
                    res.setCasePassingRate("-");
                    res.setCaseStatus("-");
                }
            }
        }
        return resList;
    }

    public ApiDefinition get(String id) {
        return apiDefinitionMapper.selectByPrimaryKey(id);
    }

    public void create(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        createTest(request);
        createBodyFiles(bodyUploadIds, bodyFiles);
    }

    public void update(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        deleteFileByTestId(request.getRequest().getId());
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        request.setBodyUploadIds(null);
        updateTest(request);
        createBodyFiles(bodyUploadIds, bodyFiles);
    }

    private void createBodyFiles(List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        if (bodyUploadIds.size() > 0) {
            File testDir = new File(BODY_FILE_DIR);
            if (!testDir.exists()) {
                testDir.mkdirs();
            }
            for (int i = 0; i < bodyUploadIds.size(); i++) {
                MultipartFile item = bodyFiles.get(i);
                File file = new File(BODY_FILE_DIR + "/" + bodyUploadIds.get(i) + "_" + item.getOriginalFilename());
                try (InputStream in = item.getInputStream(); OutputStream out = new FileOutputStream(file)) {
                    file.createNewFile();
                    FileUtil.copyStream(in, out);
                } catch (IOException e) {
                    LogUtil.error(e);
                    MSException.throwException(Translator.get("upload_fail"));
                }
            }
        }
    }

    public void delete(String apiId) {
        apiTestCaseService.checkIsRelateTest(apiId);
        deleteFileByTestId(apiId);
        apiDefinitionExecResultMapper.deleteByResourceId(apiId);
        apiDefinitionMapper.deleteByPrimaryKey(apiId);
        deleteBodyFiles(apiId);
    }

    public void deleteBatch(List<String> apiIds) {
        // 简单处理后续优化
        apiIds.forEach(item -> {
            delete(item);
        });
    }


    public void deleteBodyFiles(String apiId) {
        File file = new File(BODY_FILE_DIR + "/" + apiId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    private void checkNameExist(SaveApiDefinitionRequest request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andUrlEqualTo(request.getUrl()).andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
        if (apiDefinitionMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("api_definition_url_not_repeating"));
        }
    }


    private ApiDefinition updateTest(SaveApiDefinitionRequest request) {
        checkNameExist(request);
        final ApiDefinition test = new ApiDefinition();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setProjectId(request.getProjectId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(request.getStatus());
        test.setModulePath(request.getModulePath());
        test.setModuleId(request.getModuleId());
        test.setMethod(request.getMethod());
        test.setUrl(request.getUrl());
        test.setDescription(request.getDescription());
        test.setResponse(JSONObject.toJSONString(request.getResponse()));
        test.setEnvironmentId(request.getEnvironmentId());
        test.setUserId(request.getUserId());

        apiDefinitionMapper.updateByPrimaryKeySelective(test);
        return test;
    }

    private ApiDefinition createTest(SaveApiDefinitionRequest request) {
        checkNameExist(request);
        final ApiDefinition test = new ApiDefinition();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setUrl(request.getUrl());
        test.setMethod(request.getMethod());
        test.setModuleId(request.getModuleId());
        test.setProjectId(request.getProjectId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Underway.name());
        test.setModulePath(request.getModulePath());
        test.setResponse(JSONObject.toJSONString(request.getResponse()));
        test.setEnvironmentId(request.getEnvironmentId());
        if (request.getUserId() == null) {
            test.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            test.setUserId(request.getUserId());
        }
        test.setDescription(request.getDescription());
        apiDefinitionMapper.insert(test);
        return test;
    }

    private void deleteFileByTestId(String apiId) {
        ApiTestFileExample apiTestFileExample = new ApiTestFileExample();
        apiTestFileExample.createCriteria().andTestIdEqualTo(apiId);
        final List<ApiTestFile> ApiTestFiles = apiTestFileMapper.selectByExample(apiTestFileExample);
        apiTestFileMapper.deleteByExample(apiTestFileExample);

        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            final List<String> fileIds = ApiTestFiles.stream().map(ApiTestFile::getFileId).collect(Collectors.toList());
            fileService.deleteFileByIds(fileIds);
        }
    }

    /**
     * 测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public String run(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        createBodyFiles(bodyUploadIds, bodyFiles);

        HashTree hashTree = request.getTestElement().generateHashTree();
        request.getTestElement().getJmx(hashTree);
        // 调用执行方法
        jMeterService.runDefinition(request.getId(), hashTree, request.getReportId(), ApiRunMode.DELIMIT.name());
        return request.getId();
    }

    public void addResult(TestResult res) {
        if (!res.getScenarios().isEmpty() && !res.getScenarios().get(0).getRequestResults().isEmpty()) {
            cache.put(res.getTestId(), res.getScenarios().get(0).getRequestResults().get(0));
        } else {
            MSException.throwException(Translator.get("test_not_found"));
        }
    }

    /**
     * 获取零时执行结果报告
     *
     * @param testId
     * @param test
     * @return
     */
    public APIReportResult getResult(String testId, String test) {
        Object res = cache.get(testId);
        if (res != null) {
            cache.remove(testId);
            APIReportResult reportResult = new APIReportResult();
            reportResult.setContent(JSON.toJSONString(res));
            return reportResult;
        }
        return null;
    }

    /**
     * 获取存储执行结果报告
     *
     * @param testId
     * @return
     */
    public APIReportResult getDbResult(String testId) {
        ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByResourceId(testId);
        if (result == null) {
            return null;
        }
        APIReportResult reportResult = new APIReportResult();
        reportResult.setContent(result.getContent());
        return reportResult;
    }
}
