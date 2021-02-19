package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TapdPlatform extends AbstractIssuePlatform {


    public TapdPlatform(IssuesRequest issueRequest) {
        super(issueRequest);
    }

    @Override
    public List<Issues> getIssue() {
        List<Issues> list = new ArrayList<>();
        String tapdId = getProjectId();

        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andTestCaseIdEqualTo(testCaseId);

        List<Issues> issues = extIssuesMapper.getIssues(testCaseId, IssuesManagePlatform.Tapd.toString());

        List<String> issuesIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            Issues dto = getTapdIssues(tapdId, issuesId);
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                issuesExample.createCriteria()
                        .andTestCaseIdEqualTo(testCaseId)
                        .andIssuesIdEqualTo(issuesId);
                testCaseIssuesMapper.deleteByExample(issuesExample);
                issuesMapper.deleteByPrimaryKey(issuesId);
            } else {
                dto.setPlatform(IssuesManagePlatform.Tapd.toString());
                // 缺陷状态为 关闭，则不显示
                if (!StringUtils.equals("closed", dto.getStatus())) {
                    list.add(dto);
                }
            }
        });
        return list;
    }

    private Issues getTapdIssues(String projectId, String issuesId) {
        String url = "https://api.tapd.cn/bugs?workspace_id=" + projectId + "&id=" + issuesId;
        ResultHolder call = call(url);
        String listJson = JSON.toJSONString(call.getData());
        if (StringUtils.equals(Boolean.FALSE.toString(), listJson)) {
            return new Issues();
        }
        JSONObject jsonObject = JSONObject.parseObject(listJson);
        JSONObject bug = jsonObject.getJSONObject("Bug");
        Long created = bug.getLong("created");
        Issues issues = jsonObject.getObject("Bug", Issues.class);

        // 获取工作流中缺陷状态名称
        String workflow = "https://api.tapd.cn/workflows/status_map?workspace_id=" + projectId + "&system=bug";
        ResultHolder resultHolder = call(workflow);
        String workflowJson = JSON.toJSONString(resultHolder.getData());
        if (!StringUtils.equals(Boolean.FALSE.toString(), workflowJson)) {
            Map map = (Map) JSONObject.parse(workflowJson);
            issues.setStatus((String) map.get(issues.getStatus()));
        }

        issues.setCreateTime(created);
        return issues;
    }

    @Override
    public void addIssue(IssuesRequest issuesRequest) {
        String url = "https://api.tapd.cn/bugs";
        String testCaseId = issuesRequest.getTestCaseId();
        String tapdId = getProjectId();

        if (StringUtils.isBlank(tapdId)) {
            MSException.throwException("未关联Tapd 项目ID");
        }

        List<String> PlatformUsers = issuesRequest.getTapdUsers();
        String usersStr = String.join(";", PlatformUsers);

        String username = SessionUtils.getUser().getName();

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("title", issuesRequest.getTitle());
        paramMap.add("workspace_id", tapdId);
        paramMap.add("description", issuesRequest.getContent());
        paramMap.add("reporter", username);
        paramMap.add("current_owner", usersStr);

        ResultHolder result = call(url, HttpMethod.POST, paramMap);

        String listJson = JSON.toJSONString(result.getData());
        JSONObject jsonObject = JSONObject.parseObject(listJson);
        String issuesId = jsonObject.getObject("Bug", Issues.class).getId();

        // 用例与第三方缺陷平台中的缺陷关联
        TestCaseIssues testCaseIssues = new TestCaseIssues();
        testCaseIssues.setId(UUID.randomUUID().toString());
        testCaseIssues.setIssuesId(issuesId);
        testCaseIssues.setTestCaseId(testCaseId);
        testCaseIssuesMapper.insert(testCaseIssues);

        // 插入缺陷表
        Issues issues = new Issues();
        issues.setId(issuesId);
        issues.setPlatform(IssuesManagePlatform.Tapd.toString());
        issuesMapper.insert(issues);
    }

    @Override
    public void deleteIssue(String id) {}

    @Override
    public void testAuth() {
        try {
            String tapdConfig = getPlatformConfig(IssuesManagePlatform.Tapd.toString());
            JSONObject object = JSON.parseObject(tapdConfig);
            String account = object.getString("account");
            String password = object.getString("password");
            HttpHeaders headers = auth(account, password);
            HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange("https://api.tapd.cn/quickstart/testauth", HttpMethod.GET, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("验证失败！");
        }
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
        List<PlatformUser> users = new ArrayList<>();
        String projectId = getProjectId();
        String url = "https://api.tapd.cn/workspaces/users?workspace_id=" + projectId;
        ResultHolder call = call(url);
        String listJson = JSON.toJSONString(call.getData());
        JSONArray jsonArray = JSON.parseArray(listJson);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            PlatformUser user = o.getObject("UserWorkspace", PlatformUser.class);
            users.add(user);
        }
        return users;
    }

    @Override
    String getProjectId() {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getTapdId();
    }

    private ResultHolder call(String url) {
        return call(url, HttpMethod.GET, null);
    }

    private ResultHolder call(String url, HttpMethod httpMethod, Object params) {
        String responseJson;

        String config = getPlatformConfig(IssuesManagePlatform.Tapd.toString());
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");

        HttpHeaders header = auth(account, password);

        if (httpMethod.equals(HttpMethod.GET)) {
            responseJson = TapdRestUtils.get(url, header);
        } else {
            responseJson = TapdRestUtils.post(url, params, header);
        }

        ResultHolder result = JSON.parseObject(responseJson, ResultHolder.class);

        if (!result.isSuccess()) {
            MSException.throwException(result.getMessage());
        }
        return JSON.parseObject(responseJson, ResultHolder.class);

    }

}
