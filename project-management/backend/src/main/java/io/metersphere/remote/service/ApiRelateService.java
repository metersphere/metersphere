package io.metersphere.remote.service;

import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.ReplaceFileIdRequest;
import io.metersphere.remote.dto.ApiDefinitionRequest;
import io.metersphere.remote.dto.ApiScenarioRequest;
import io.metersphere.request.ApiTestCaseRequest;
import io.metersphere.service.MicroService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiRelateService {

    @Resource
    private MicroService microService;

    public List<ApiDefinition> selectApiDefinitionByIds(List<String> apiIds) {
        if (CollectionUtils.isNotEmpty(apiIds)) {
            ApiDefinitionRequest request = new ApiDefinitionRequest();
            request.setIds(apiIds);
            try {
                return microService.postForDataArray(MicroServiceName.API_TEST,
                        "/api/definition/select/by/id/",
                        request, ApiDefinition.class);
            } catch (Exception e) {
                LogUtil.error("调用 接口测试服务失败", e);
            }
        }
        return new ArrayList<>(0);
    }

    public List<ApiTestCase> selectApiTestCaseByIds(List<String> apiTestCaseIds) {
        if (CollectionUtils.isNotEmpty(apiTestCaseIds)) {
            ApiTestCaseRequest request = new ApiTestCaseRequest();
            request.setIds(apiTestCaseIds);
            try {
                return microService.postForDataArray(MicroServiceName.API_TEST,
                        "/api/testcase/select/by/id/",
                        request, ApiTestCase.class);
            } catch (Exception e) {
                LogUtil.error("调用 接口测试服务失败", e);
            }
        }
        return new ArrayList<>(0);
    }

    public List<ApiScenario> selectScenarioByIds(List<String> scenarioIdList) {

        if (CollectionUtils.isNotEmpty(scenarioIdList)) {
            ApiScenarioRequest request = new ApiScenarioRequest();
            request.setIds(scenarioIdList);
            try {
                return microService.postForDataArray(MicroServiceName.API_TEST,
                        "/api/automation/select/by/id/",
                        request, ApiScenario.class);
            } catch (Exception e) {
                LogUtil.error("调用 接口测试服务失败", e);
            }
        }
        return new ArrayList<>(0);
    }

    public void updateFileMetadataIdByRequestList(List<ReplaceFileIdRequest> requestList) {
        if (CollectionUtils.isNotEmpty(requestList)) {
            try {
                microService.postForData(MicroServiceName.API_TEST,
                        "/api/definition/update/file/",
                        requestList);
            } catch (Exception e) {
                LogUtil.error(e);
                MSException.throwException("调用 接口测试服务失败");
            }
        }
    }
}
