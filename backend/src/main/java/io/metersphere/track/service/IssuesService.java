package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.base.mapper.TestCaseIssuesMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.RestTemplateUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.service.IntegrationService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import javax.annotation.Resource;
import java.util.*;
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
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private ExtIssuesMapper extIssuesMapper;


    public void testAuth(String platform) {
        if (StringUtils.equals(platform, IssuesManagePlatform.Tapd.toString())) {

            try {
                String tapdConfig = platformConfig(IssuesManagePlatform.Tapd.toString());
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

        } else {

            try {
                String config = platformConfig(IssuesManagePlatform.Jira.toString());
                JSONObject object = JSON.parseObject(config);
                String account = object.getString("account");
                String password = object.getString("password");
                String url = object.getString("url");
                HttpHeaders headers = auth(account, password);
                HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(headers);
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.exchange(url + "rest/api/2/issue/createmeta", HttpMethod.GET, requestEntity, String.class);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException("验证失败！");
            }
        }

    }

    private ResultHolder call(String url) {
        return call(url, HttpMethod.GET, null);
    }

    private ResultHolder call(String url, HttpMethod httpMethod, Object params) {
        String responseJson;

        String config = platformConfig(IssuesManagePlatform.Tapd.toString());
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");

        HttpHeaders header = auth(account, password);

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

    private String platformConfig(String platform) {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(orgId)) {
            MSException.throwException("organization id is null");
        }
        request.setOrgId(orgId);
        request.setPlatform(platform);

        ServiceIntegration integration = integrationService.get(request);
        return integration.getConfiguration();
    }

    private HttpHeaders auth(String apiUser, String password) {
        String authKey = EncryptUtils.base64Encoding(apiUser + ":" + password);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + authKey);
        return headers;
    }

    public void addIssues(IssuesRequest issuesRequest) {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        boolean tapd = isIntegratedPlatform(orgId, IssuesManagePlatform.Tapd.toString());
        boolean jira = isIntegratedPlatform(orgId, IssuesManagePlatform.Jira.toString());

        String tapdId = getTapdProjectId(issuesRequest.getTestCaseId());
        String jiraKey = getJiraProjectKey(issuesRequest.getTestCaseId());

        if (tapd) {
            // 是否关联了项目
            if (StringUtils.isNotBlank(tapdId)) {
                addTapdIssues(issuesRequest);
            }
        }

        if (jira) {
            if (StringUtils.isNotBlank(jiraKey)) {
                addJiraIssues(issuesRequest);
            }
        }

        if (StringUtils.isBlank(tapdId) && StringUtils.isBlank(jiraKey)) {
            addLocalIssues(issuesRequest);
        }

    }

    public void addTapdIssues(IssuesRequest issuesRequest) {
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

    public void addJiraIssues(IssuesRequest issuesRequest) {
        String config = platformConfig(IssuesManagePlatform.Jira.toString());
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("jira config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");
        String url = object.getString("url");
        String issuetype = object.getString("issuetype");
        if (StringUtils.isBlank(issuetype)) {
            MSException.throwException("Jira 问题类型为空");
        }
        String auth = EncryptUtils.base64Encoding(account + ":" + password);

        String testCaseId = issuesRequest.getTestCaseId();
        String jiraKey = getJiraProjectKey(testCaseId);


        if (StringUtils.isBlank(jiraKey)) {
            MSException.throwException("未关联Jira 项目Key");
        }

        String content = issuesRequest.getContent();

        Document document = Jsoup.parse(content);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        String desc = Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        desc = desc.replace("&nbsp;", "");

        String json = "{\n" +
                "    \"fields\":{\n" +
                "        \"project\":{\n" +
                "            \"key\":\"" + jiraKey + "\"\n" +
                "        },\n" +
                "        \"summary\":\"" + issuesRequest.getTitle() + "\",\n" +
                "        \"description\": " + JSON.toJSONString(desc) + ",\n" +
                "        \"issuetype\":{\n" +
                "            \"name\":\"" + issuetype + "\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        String result = addJiraIssue(url, auth, json);

        JSONObject jsonObject = JSON.parseObject(result);
        String id = jsonObject.getString("id");

        // 用例与第三方缺陷平台中的缺陷关联
        TestCaseIssues testCaseIssues = new TestCaseIssues();
        testCaseIssues.setId(UUID.randomUUID().toString());
        testCaseIssues.setIssuesId(id);
        testCaseIssues.setTestCaseId(testCaseId);
        testCaseIssuesMapper.insert(testCaseIssues);

        // 插入缺陷表
        Issues issues = new Issues();
        issues.setId(id);
        issues.setPlatform(IssuesManagePlatform.Jira.toString());
        issuesMapper.insert(issues);
    }

    private String addJiraIssue(String url, String auth, String json) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Authorization", "Basic " + auth);
        requestHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        //HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(json, requestHeaders);
        RestTemplate restTemplate = new RestTemplate();
        //post
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(url + "/rest/api/2/issue", HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("调用Jira接口创建缺陷失败");
        }

        return responseEntity.getBody();
    }

    public void addLocalIssues(IssuesRequest request) {
        SessionUser user = SessionUtils.getUser();
        String id = UUID.randomUUID().toString();
        Issues issues = new Issues();
        issues.setId(id);
        issues.setStatus("new");
        issues.setReporter(user.getId());
        issues.setTitle(request.getTitle());
        issues.setDescription(request.getContent());
        issues.setCreateTime(System.currentTimeMillis());
        issues.setUpdateTime(System.currentTimeMillis());
        issues.setPlatform(IssuesManagePlatform.Local.toString());
        issuesMapper.insert(issues);

        TestCaseIssues testCaseIssues = new TestCaseIssues();
        testCaseIssues.setId(UUID.randomUUID().toString());
        testCaseIssues.setIssuesId(id);
        testCaseIssues.setTestCaseId(request.getTestCaseId());
        testCaseIssuesMapper.insert(testCaseIssues);
    }

    public Issues getTapdIssues(String projectId, String issuesId) {
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
        issues.setCreateTime(created);
        return issues;
    }

    public Issues getJiraIssues(HttpHeaders headers, String url, String issuesId) {
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        //post
        ResponseEntity<String> responseEntity;
        Issues issues = new Issues();
        try {
            responseEntity = restTemplate.exchange(url + "/rest/api/2/issue/" + issuesId, HttpMethod.GET, requestEntity, String.class);
            String body = responseEntity.getBody();

            JSONObject obj = JSONObject.parseObject(body);
            JSONObject fields = (JSONObject) obj.get("fields");
            JSONObject statusObj = (JSONObject) fields.get("status");
            JSONObject assignee = (JSONObject) fields.get("assignee");
            JSONObject statusCategory = (JSONObject) statusObj.get("statusCategory");

            String id = obj.getString("id");
            String title = fields.getString("summary");
            String description = fields.getString("description");

            Parser parser = Parser.builder().build();
            Node document = parser.parse(description);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            description = renderer.render(document);

            String status = statusCategory.getString("key");
            Long createTime = fields.getLong("created");
            String lastmodify = "";
            if (assignee != null) {
                lastmodify = assignee.getString("displayName");
            }

            issues.setId(id);
            issues.setTitle(title);
            issues.setCreateTime(createTime);
            issues.setLastmodify(lastmodify);
            issues.setDescription(description);
            issues.setStatus(status);
            issues.setPlatform(IssuesManagePlatform.Jira.toString());
        } catch (HttpClientErrorException.NotFound e) {
            LogUtil.error(e.getStackTrace(), e);
            return new Issues();
        } catch (HttpClientErrorException.Unauthorized e) {
            LogUtil.error(e.getStackTrace(), e);
            MSException.throwException("获取Jira缺陷失败，检查Jira配置信息");
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("调用Jira接口获取缺陷失败");
        }

        return issues;

    }

    public List<Issues> getIssues(String caseId) {
        List<Issues> list = new ArrayList<>();
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        boolean tapd = isIntegratedPlatform(orgId, IssuesManagePlatform.Tapd.toString());
        boolean jira = isIntegratedPlatform(orgId, IssuesManagePlatform.Jira.toString());

        if (tapd) {
            // 是否关联了项目
            String tapdId = getTapdProjectId(caseId);
            if (StringUtils.isNotBlank(tapdId)) {
                list.addAll(getTapdIssues(caseId));
            }

        }

        if (jira) {
            String jiraKey = getJiraProjectKey(caseId);
            if (StringUtils.isNotBlank(jiraKey)) {
                list.addAll(getJiraIssues(caseId));
            }
        }

        list.addAll(getLocalIssues(caseId));
        return list;
    }

    public List<Issues> getTapdIssues(String caseId) {
        List<Issues> list = new ArrayList<>();
        String tapdId = getTapdProjectId(caseId);

        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andTestCaseIdEqualTo(caseId);

        List<Issues> issues = extIssuesMapper.getIssues(caseId, IssuesManagePlatform.Tapd.toString());

        List<String> issuesIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            Issues dto = getTapdIssues(tapdId, issuesId);
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                issuesExample.createCriteria()
                        .andTestCaseIdEqualTo(caseId)
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

    public List<Issues> getJiraIssues(String caseId) {
        List<Issues> list = new ArrayList<>();

        String config = platformConfig(IssuesManagePlatform.Jira.toString());
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");
        String url = object.getString("url");
        HttpHeaders headers = auth(account, password);

        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andTestCaseIdEqualTo(caseId);

        List<Issues> issues = extIssuesMapper.getIssues(caseId, IssuesManagePlatform.Jira.toString());

        List<String> issuesIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            Issues dto = getJiraIssues(headers, url, issuesId);
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                issuesExample.createCriteria()
                        .andTestCaseIdEqualTo(caseId)
                        .andIssuesIdEqualTo(issuesId);
                testCaseIssuesMapper.deleteByExample(issuesExample);
                issuesMapper.deleteByPrimaryKey(issuesId);
            } else {
                // 缺陷状态为 完成，则不显示
                if (!StringUtils.equals("done", dto.getStatus())) {
                    list.add(dto);
                }
            }
        });
        return list;
    }

    public List<Issues> getLocalIssues(String caseId) {
        List<Issues> list = extIssuesMapper.getIssues(caseId, IssuesManagePlatform.Local.toString());
        List<Issues> issues = list.stream()
                .filter(l -> !StringUtils.equals(l.getStatus(), "closed"))
                .collect(Collectors.toList());
        return issues;
    }

    public String getTapdProjectId(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getTapdId();
    }

    public String getJiraProjectKey(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getJiraKey();
    }

    /**
     * 是否关联平台
     */
    public boolean isIntegratedPlatform(String orgId, String platform) {
        IntegrationRequest request = new IntegrationRequest();
        request.setPlatform(platform);
        request.setOrgId(orgId);
        ServiceIntegration integration = integrationService.get(request);
        return StringUtils.isNotBlank(integration.getId());
    }

    public void closeLocalIssue(String issueId) {
        Issues issues = new Issues();
        issues.setId(issueId);
        issues.setStatus("closed");
        issuesMapper.updateByPrimaryKeySelective(issues);
    }

}
