package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.delimit.ApiComputeResult;
import io.metersphere.api.dto.delimit.ApiDelimitRequest;
import io.metersphere.api.dto.delimit.ApiDelimitResult;
import io.metersphere.api.dto.delimit.SaveApiDelimitRequest;
import io.metersphere.api.jmeter.ApiTestResult;
import io.metersphere.api.jmeter.DelimitJMeterService;
import io.metersphere.api.parse.JmeterDocumentParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDelimitExecResultMapper;
import io.metersphere.base.mapper.ApiDelimitMapper;
import io.metersphere.base.mapper.ApiTestFileMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.service.FileService;
import io.metersphere.service.QuotaService;
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
public class ApiDelimitService {
    @Resource
    private ApiDelimitMapper apiDelimitMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDelimitExecResultMapper apiDelimitExecResultMapper;
    @Resource
    private DelimitJMeterService jMeterService;
    private static Cache cache = Cache.newHardMemoryCache(0, 3600 * 24);

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    public List<ApiDelimitResult> list(ApiDelimitRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiDelimitResult> resList = apiDelimitMapper.list(request);
        if (!resList.isEmpty()) {
            List<String> ids = resList.stream().map(ApiDelimitResult::getId).collect(Collectors.toList());
            List<ApiComputeResult> results = apiDelimitMapper.selectByIds(ids);
            Map<String, ApiComputeResult> resultMap = results.stream().collect(Collectors.toMap(ApiComputeResult::getApiDelimitId, Function.identity()));
            for (ApiDelimitResult res : resList) {
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

    public ApiDelimit get(String id) {
        return apiDelimitMapper.selectByPrimaryKey(id);
    }

    public void create(SaveApiDelimitRequest request, MultipartFile file, List<MultipartFile> bodyFiles) {
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        ApiDelimit test = createTest(request, file);
        createBodyFiles(test.getId(), bodyUploadIds, bodyFiles);
    }

    private ApiDelimit createTest(SaveApiDelimitRequest request, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        checkQuota();
        request.setBodyUploadIds(null);
        ApiDelimit test = createTest(request);
        saveFile(test.getId(), file);
        return test;
    }

    private void checkQuota() {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPITestQuota();
        }
    }

    public void update(SaveApiDelimitRequest request, MultipartFile file, List<MultipartFile> bodyFiles) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        deleteFileByTestId(request.getId());

        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        request.setBodyUploadIds(null);
        ApiDelimit test = updateTest(request);
        createBodyFiles(test.getId(), bodyUploadIds, bodyFiles);
        saveFile(test.getId(), file);
    }

    private void createBodyFiles(String testId, List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        if (bodyUploadIds.size() > 0) {
            String dir = BODY_FILE_DIR + "/" + testId;
            File testDir = new File(dir);
            if (!testDir.exists()) {
                testDir.mkdirs();
            }
            for (int i = 0; i < bodyUploadIds.size(); i++) {
                MultipartFile item = bodyFiles.get(i);
                File file = new File(testDir + "/" + bodyUploadIds.get(i) + "_" + item.getOriginalFilename());
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
        apiDelimitExecResultMapper.deleteByResourceId(apiId);
        apiDelimitMapper.deleteByPrimaryKey(apiId);
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

    private void checkNameExist(SaveApiDelimitRequest request) {
        ApiDelimitExample example = new ApiDelimitExample();
        example.createCriteria().andUrlEqualTo(request.getUrl()).andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
        if (apiDelimitMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("api_delimit_url_not_repeating"));
        }
    }


    private ApiDelimit updateTest(SaveApiDelimitRequest request) {
        checkNameExist(request);
        final ApiDelimit test = new ApiDelimit();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setProjectId(request.getProjectId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(request.getStatus());
        test.setModulePath(request.getModulePath());
        test.setModuleId(request.getModuleId());
        test.setPath(request.getPath());
        test.setUrl(request.getUrl());
        test.setDescription(request.getDescription());
        test.setResponse(JSONObject.toJSONString(request.getResponse()));
        test.setEnvironmentId(request.getEnvironmentId());
        test.setUserId(request.getUserId());

        apiDelimitMapper.updateByPrimaryKeySelective(test);
        return test;
    }

    private ApiDelimit createTest(SaveApiDelimitRequest request) {
        checkNameExist(request);
        final ApiDelimit test = new ApiDelimit();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setUrl(request.getUrl());
        test.setPath(request.getPath());
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
        apiDelimitMapper.insert(test);
        return test;
    }

    private void saveFile(String apiId, MultipartFile file) {
        final FileMetadata metadata = fileService.saveFile(file);
        ApiTestFile apiTestFile = new ApiTestFile();
        apiTestFile.setTestId(apiId);
        apiTestFile.setFileId(metadata.getId());
        apiTestFileMapper.insert(apiTestFile);
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
     * @param file
     * @param bodyFiles
     * @return
     */
    public String run(SaveApiDelimitRequest request, MultipartFile file, List<MultipartFile> bodyFiles) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        createBodyFiles(request.getId(), bodyUploadIds, bodyFiles);
        InputStream is = null;
        try {
            // 解析 xml 处理 mock 数据
            byte[] bytes = JmeterDocumentParser.parse(file.getBytes());
            is = new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            LogUtil.error(e);
        }
        jMeterService.run(request.getId(), request.getReportId(), is);
        return request.getId();
    }

    public void addResult(ApiTestResult res) {
        cache.put(res.getId(), res);
    }

    /**
     * 获取执行结果报告
     *
     * @param testId
     * @param test
     * @return
     */
    public APIReportResult getResult(String testId, String test) {
        if (test.equals("debug")) {
            Object res = cache.get(testId);
            if (res != null) {
                cache.remove(testId);
                APIReportResult reportResult = new APIReportResult();
                reportResult.setContent(JSON.toJSONString(res));
                return reportResult;
            }
        } else {
            ApiDelimitExecResult data = apiDelimitExecResultMapper.selectByResourceId(testId);
            if (data == null) {
                return null;
            }
            APIReportResult reportResult = new APIReportResult();
            reportResult.setContent(data.getContent());
            return reportResult;
        }
        return null;
    }
}
