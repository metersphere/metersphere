package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.mockconfig.MockConfigStaticData;
import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestEnvironmentService {

    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;

    public List<ApiTestEnvironmentWithBLOBs> list(String projectId) {
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return apiTestEnvironmentMapper.selectByExampleWithBLOBs(example);
    }

    public List<ApiTestEnvironmentWithBLOBs> selectByExampleWithBLOBs(ApiTestEnvironmentExample example) {
        return apiTestEnvironmentMapper.selectByExampleWithBLOBs(example);
    }


    public ApiTestEnvironmentWithBLOBs get(String id) {
        return apiTestEnvironmentMapper.selectByPrimaryKey(id);
    }

    public void delete(String id) {
        apiTestEnvironmentMapper.deleteByPrimaryKey(id);
    }

    public void update(ApiTestEnvironmentWithBLOBs apiTestEnvironment) {
        checkEnvironmentExist(apiTestEnvironment);
        apiTestEnvironmentMapper.updateByPrimaryKeyWithBLOBs(apiTestEnvironment);
    }

    public String add(ApiTestEnvironmentWithBLOBs apiTestEnvironmentWithBLOBs) {
        apiTestEnvironmentWithBLOBs.setId(UUID.randomUUID().toString());
        checkEnvironmentExist(apiTestEnvironmentWithBLOBs);
        apiTestEnvironmentMapper.insert(apiTestEnvironmentWithBLOBs);
        return apiTestEnvironmentWithBLOBs.getId();
    }

    private void checkEnvironmentExist(ApiTestEnvironmentWithBLOBs environment) {
        if (environment.getName() != null) {
            ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
            ApiTestEnvironmentExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(environment.getName())
                    .andProjectIdEqualTo(environment.getProjectId());
            if (StringUtils.isNotBlank(environment.getId())) {
                criteria.andIdNotEqualTo(environment.getId());
            }
            if (apiTestEnvironmentMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("api_test_environment_already_exists"));
            }
        }
    }

    /**
     * 通过项目ID获取Mock环境  （暂时定义mock环境为： name = Mock环境）
     *
     * @param projectId
     * @return
     */
    public synchronized ApiTestEnvironmentWithBLOBs getMockEnvironmentByProjectId(String projectId, String baseUrl) {
        String apiName = MockConfigStaticData.MOCK_EVN_NAME;
        ApiTestEnvironmentWithBLOBs returnModel = null;
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(apiName);
        List<ApiTestEnvironmentWithBLOBs> list = this.selectByExampleWithBLOBs(example);
        if (list.isEmpty()) {
            returnModel = this.genHttpApiTestEnvironmentByUrl(projectId, apiName, baseUrl);
            this.add(returnModel);
        } else {
            returnModel = list.get(0);
        }
        return returnModel;
    }

    private ApiTestEnvironmentWithBLOBs genHttpApiTestEnvironmentByUrl(String projectId, String name, String url) {
        String protocol = "";
        String socket = "";
        if (url.startsWith("http://")) {
            protocol = "http";
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            protocol = "https";
            url = url.substring(8);
        }
        socket = url;

        String portStr = "";
        String ipStr = protocol;
        if (url.contains(":") && !url.endsWith(":")) {
            String[] urlArr = url.split(":");
            int port = -1;
            try {
                port = Integer.parseInt(urlArr[urlArr.length - 1]);
            } catch (Exception e) {
            }
            if (port > -1) {
                portStr = String.valueOf(port);
                ipStr = urlArr[0];
            }
        }

        JSONObject commonConfigObj = new JSONObject();
        JSONArray variablesArr = new JSONArray();
        Map<String, Object> map = new HashMap<>();
        map.put("enable", true);
        variablesArr.add(map);
        commonConfigObj.put("variables", variablesArr);
        commonConfigObj.put("enableHost", false);
        commonConfigObj.put("hosts", new String[]{});

        JSONObject httpConfig = new JSONObject();
//        httpConfig.put("socket", url);
//        httpConfig.put("domain", ipStr);
//        httpConfig.put("headers", variablesArr);
//        httpConfig.put("protocol", protocol);
//        if (StringUtils.isNotEmpty(portStr)) {
//            httpConfig.put("port", portStr);
//        }
        httpConfig.put("socket", null);
        httpConfig.put("domain", null);
        httpConfig.put("headers", new JSONArray(variablesArr));
        httpConfig.put("protocol", null);
        httpConfig.put("port", null);
        JSONArray httpItemArr = new JSONArray();
        JSONObject httpItem = new JSONObject();
        httpItem.put("id", UUID.randomUUID().toString());
        httpItem.put("type", "NONE");
        httpItem.put("socket", socket);
        httpItem.put("protocol", protocol);
        httpItem.put("headers", new JSONArray(variablesArr));
        httpItem.put("domain", ipStr);
        if (StringUtils.isNotEmpty(portStr)) {
            httpItem.put("port", portStr);
        } else {
            httpItem.put("port", "");
        }
        JSONArray detailArr = new JSONArray();
        JSONObject detailObj = new JSONObject();
        detailObj.put("name", "");
        detailObj.put("value", "contains");
        detailObj.put("enable", true);
        detailArr.add(detailObj);
        httpItem.put("details", detailArr);

        httpItemArr.add(httpItem);
        httpConfig.put("conditions", httpItemArr);
        httpConfig.put("defaultCondition", "NONE");

        JSONArray databaseConfigObj = new JSONArray();

        JSONObject tcpConfigObj = new JSONObject();
        tcpConfigObj.put("classname", "TCPClientImpl");
        tcpConfigObj.put("reUseConnection", false);
        tcpConfigObj.put("nodelay", false);
        tcpConfigObj.put("closeConnection", false);

        JSONObject object = new JSONObject();
        object.put("commonConfig", commonConfigObj);
        object.put("httpConfig", httpConfig);
        object.put("databaseConfigs", databaseConfigObj);
        object.put("tcpConfig", tcpConfigObj);

        ApiTestEnvironmentWithBLOBs blobs = new ApiTestEnvironmentWithBLOBs();
        blobs.setProjectId(projectId);
        blobs.setName(name);
        blobs.setConfig(object.toString());

        return blobs;
    }
}
