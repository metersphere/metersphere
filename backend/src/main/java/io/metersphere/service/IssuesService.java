package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseIssuesMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.RestTemplateUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.track.dto.IssuesDTO;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.service.TestCaseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssuesService {

    @Resource
    private IntegrationService integrationService;
    @Resource
    private TestCaseIssuesMapper testCaseIssuesMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private TestCaseService testCaseService;


    public void testAuth() {
        String url = "https://api.tapd.cn/quickstart/testauth";
        ResultHolder call = call(url);
        System.out.println(call.getData());
    }

    public void addIssues(IssuesRequest issuesRequest) {
        String url = "https://api.tapd.cn/bugs";
        String testCaseId = issuesRequest.getTestCaseId();
        String tapdId = getTapdProjectId(testCaseId);

        if (StringUtils.isBlank(tapdId)) {
            MSException.throwException("未关联Tapd 项目ID");
        }

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("title", issuesRequest.getTitle());
        paramMap.add("workspace_id", tapdId);
        paramMap.add("description", issuesRequest.getContent());

        ResultHolder result = call(url, HttpMethod.POST, paramMap);

        String listJson = JSON.toJSONString(result.getData());
        JSONObject jsonObject = JSONObject.parseObject(listJson);
        String issuesId = jsonObject.getObject("Bug", IssuesDTO.class).getId();

        // 用例与第三方缺陷平台中的缺陷关联
        TestCaseIssues issues = new TestCaseIssues();
        issues.setId(UUID.randomUUID().toString());
        issues.setIssuesId(issuesId);
        issues.setTestCaseId(testCaseId);
        testCaseIssuesMapper.insert(issues);
    }

    private ResultHolder call(String url) {
        return call(url, HttpMethod.GET, null);
    }

    private ResultHolder call(String url, HttpMethod httpMethod, Object params) {
        String responseJson;

        String config = tapdConfig();
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");

        HttpHeaders header = tapdAuth(account, password);

        if (httpMethod.equals(HttpMethod.GET)) {
            responseJson = RestTemplateUtils.get(url, header);
        } else {
            responseJson = RestTemplateUtils.post(url, params, header);
        }

        ResultHolder result = JSON.parseObject(responseJson, ResultHolder.class);

        if (!result.isSuccess()) {
            MSException.throwException(result.getMessage());
        }
        return JSON.parseObject(responseJson, ResultHolder.class);

    }

    private String tapdConfig() {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(orgId)) {
            MSException.throwException("organization id is null");
        }
        request.setOrgId(orgId);
        request.setPlatform(IssuesManagePlatform.Tapd.toString());

        ServiceIntegration integration = integrationService.get(request);
        return integration.getConfiguration();
    }

    private HttpHeaders tapdAuth(String apiUser, String password) {
        String authKey = EncryptUtils.base64Encoding(apiUser + ":" + password);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + authKey);
        return headers;
    }

    public IssuesDTO getTapdIssues(String projectId, String issuesId) {
        String url = "https://api.tapd.cn/bugs?workspace_id=" + projectId + "&id=" + issuesId;
        ResultHolder call = call(url);
        String listJson = JSON.toJSONString(call.getData());
        if (StringUtils.equals(Boolean.FALSE.toString(), listJson)) {
            return new IssuesDTO();
        }
        JSONObject jsonObject = JSONObject.parseObject(listJson);
        return jsonObject.getObject("Bug", IssuesDTO.class);
    }

    public List<IssuesDTO> getIssues(String caseId) {
        List<IssuesDTO> list = new ArrayList<>();
        String tapdId = getTapdProjectId(caseId);

        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andTestCaseIdEqualTo(caseId);

        List<TestCaseIssues> issues = testCaseIssuesMapper.selectByExample(example);
        List<String> issuesIds = issues.stream().map(issue -> issue.getIssuesId()).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            IssuesDTO dto = getTapdIssues(tapdId, issuesId);
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                issuesExample.createCriteria()
                        .andTestCaseIdEqualTo(caseId)
                        .andIssuesIdEqualTo(issuesId);
                testCaseIssuesMapper.deleteByExample(issuesExample);
            } else {
                list.add(dto);
            }
        });
        return list;
    }

    public String getTapdProjectId(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getTapdId();
    }
}
