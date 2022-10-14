package io.metersphere.api.service;

import io.metersphere.api.dto.*;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiLoadTestMapper;
import io.metersphere.base.mapper.LoadTestFileMapper;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.ext.ExtApiLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.EditTestPlanRequest;
import io.metersphere.service.MicroService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiPerformanceService {

    @Resource
    private ApiLoadTestMapper apiLoadTestMapper;
    @Resource
    private ExtApiLoadTestMapper extApiLoadTestMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private MicroService microService;

    public ScenarioToPerformanceInfoDTO exportJmx(Object request) {
        return microService.postForData(MicroServiceName.API_TEST, "/api/automation/export/jmx", request, ScenarioToPerformanceInfoDTO.class);
    }

    public List<JmxInfoDTO> exportApiCaseJmx(Object request) {
        return microService.postForDataArray(MicroServiceName.API_TEST, "/api/testcase/export/jmx", request, JmxInfoDTO.class);
    }

    public Object list(int goPage, int pageSize, Object request) {
        return microService.postForResultHolder(MicroServiceName.API_TEST, "/api/automation/list/" + goPage + "/" + pageSize, request);
    }

    public boolean isNeedUpdate(String loadTestId) {
        int count = extApiLoadTestMapper.countNeedUpdateApiCase(loadTestId);
        if (count > 0) {
            return true;
        }
        count = extApiLoadTestMapper.countNeedUpdateApiScenario(loadTestId);
        return count > 0;
    }

    public List<ApiLoadTest> getByLoadTestId(String loadTestId) {
        ApiLoadTestExample example = new ApiLoadTestExample();
        example.createCriteria().andLoadTestIdEqualTo(loadTestId);
        return apiLoadTestMapper.selectByExample(example);
    }

    public int updateVersion(String loadTestId, String apiId, int version) {
        ApiLoadTest apiLoadTest = new ApiLoadTest();
        apiLoadTest.setApiVersion(version);
        ApiLoadTestExample example = new ApiLoadTestExample();
        example.createCriteria()
                .andLoadTestIdEqualTo(loadTestId)
                .andApiIdEqualTo(apiId);
        return apiLoadTestMapper.updateByExampleSelective(apiLoadTest, example);
    }

    public void add(List<ApiLoadTest> apiList, String loadTestId) {
        if (CollectionUtils.isNotEmpty(apiList)) {
            apiList.forEach(item -> {
                item.setId(UUID.randomUUID().toString());
                item.setLoadTestId(loadTestId);
                item.setApiVersion(0);
                apiLoadTestMapper.insert(item);
            });
        }
    }


    /**
     * 一键更新由接口用例或者场景用例转换的性能测试
     *
     * @param request
     */
    public void syncApi(EditTestPlanRequest request) {
        String lostTestId = request.getId();
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(lostTestId);
        if (loadTest == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + lostTestId);
        }
        if (StringUtils.containsAny(loadTest.getStatus(), PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name())) {
            MSException.throwException(Translator.get("cannot_edit_load_test_running"));
        }
        List<ApiLoadTest> apiLoadTests = this.getByLoadTestId(loadTest.getId());
        syncScenario(loadTest, apiLoadTests);
        syncApiCase(loadTest, apiLoadTests);
    }

    public void syncScenario(LoadTestWithBLOBs loadTest, List<ApiLoadTest> apiLoadTests) {
        List<String> scenarioIds = apiLoadTests.stream()
                .filter(i -> i.getType().equals(ApiLoadType.SCENARIO.name()))
                .map(ApiLoadTest::getApiId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(scenarioIds)) {
            ApiScenarioBatchRequest scenarioRequest = new ApiScenarioBatchRequest();
            scenarioRequest.setIds(scenarioIds);
            List<ApiScenarioExportJmxDTO> apiScenrioExportJmxes = this.exportJmx(scenarioRequest).getScenarioJmxList();

            deleteLoadTestFiles(loadTest.getId());

            apiScenrioExportJmxes.forEach(item -> {
                this.updateVersion(loadTest.getId(), item.getId(), item.getVersion());
                saveJmxFile(item.getJmx(), item.getName(), loadTest.getProjectId(), loadTest.getId());
                saveOtherFile(item.getFileMetadataList(), loadTest.getId());
            });
        }
    }

    public void syncApiCase(LoadTestWithBLOBs loadTest, List<ApiLoadTest> apiLoadTests) {
        List<String> caseIds = apiLoadTests.stream()
                .filter(i -> i.getType().equals(ApiLoadType.API_CASE.name()))
                .map(ApiLoadTest::getApiId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(caseIds)) {
            ApiCaseExportJmxRequest request = new ApiCaseExportJmxRequest();
            request.setCaseIds(caseIds);
            request.setEnvId(apiLoadTests.get(0).getEnvId());
            List<JmxInfoDTO> jmxInfoDTOS = this.exportApiCaseJmx(request);
            deleteLoadTestFiles(loadTest.getId());
            jmxInfoDTOS.forEach(item -> {
                this.updateVersion(loadTest.getId(), item.getId(), item.getVersion());
                saveJmxFile(item.getXml(), item.getName(), loadTest.getProjectId(), loadTest.getId());
                saveBodyFile(item.getFileMetadataList(), loadTest.getId(), item.getId());
            });
        }
    }


    private void saveJmxFile(String jmx, String name, String projectId, String loadTestId) {
        byte[] jmxBytes = jmx.getBytes(StandardCharsets.UTF_8);
        String jmxName = name + "_" + System.currentTimeMillis() + ".jmx";
        FileMetadata fileMetadata = fileMetadataService.saveFile(jmxBytes, jmxName, (long) jmxBytes.length);
        fileMetadata.setProjectId(projectId);
        saveLoadTestFile(fileMetadata, loadTestId, 0);
    }

    private void saveOtherFile(List<FileMetadata> fileNames, String loadTestId) {
        for (int i = 0; i < fileNames.size(); i++) {
            FileMetadata model = fileNames.get(i);
            String fileName = model.getName();
            File file = FileUtils.getFileByName(fileName);
            saveUploadFile(file, loadTestId, i + 1);
        }
    }

    private void saveBodyFile(List<FileMetadata> fileNames, String loadTestId, String requestId) {
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i).getName();
            File file = FileUtils.getBodyFileByName(fileName, requestId);
            saveUploadFile(file, loadTestId, i + 1);
        }
    }

    private void saveUploadFile(File file, String loadTestId, int sort) {
        if (file != null) {
            FileMetadata fileMetadata = null;
            try {
                fileMetadata = fileMetadataService.saveFile(file);
            } catch (Exception e) {
                LogUtil.error(e);
            }
            saveLoadTestFile(fileMetadata, loadTestId, sort);
        }
    }

    private void saveLoadTestFile(FileMetadata fileMetadata, String loadTestId, int sort) {
        if (fileMetadata != null) {
            LoadTestFile loadTestFile = new LoadTestFile();
            loadTestFile.setTestId(loadTestId);
            loadTestFile.setFileId(fileMetadata.getId());
            loadTestFile.setSort(sort);
            loadTestFileMapper.insert(loadTestFile);
        }
    }

    private void deleteLoadTestFiles(String testId) {
        List<FileMetadata> originFiles = extLoadTestMapper.getFileMetadataByIds(testId);
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        LoadTestFileExample example = new LoadTestFileExample();
        example.createCriteria().andTestIdEqualTo(testId);
        loadTestFileMapper.deleteByExample(example);
        fileMetadataService.deleteBatch(originFileIds);
    }
}
