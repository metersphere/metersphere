package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JiraPlatform extends AbstractIssuePlatform {


    public JiraPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
    }

    @Override
    public List<Issues> getIssue() {
        List<Issues> list = new ArrayList<>();

        String config = getPlatformConfig(IssuesManagePlatform.Jira.toString());
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");
        String url = object.getString("url");
        HttpHeaders headers = auth(account, password);

        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andTestCaseIdEqualTo(testCaseId);

        List<Issues> issues = extIssuesMapper.getIssues(testCaseId, IssuesManagePlatform.Jira.toString());

        List<String> issuesIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            Issues dto = getJiraIssues(headers, url, issuesId);
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                issuesExample.createCriteria()
                        .andTestCaseIdEqualTo(testCaseId)
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

    @Override
    public void addIssue(IssuesRequest issuesRequest) {
        String config = getPlatformConfig(IssuesManagePlatform.Jira.toString());
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
        String jiraKey = getProjectId();


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
        String id = jsonObject.getString("key");

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

    @Override
    public void deleteIssue(String id) {

    }

    @Override
    public void testAuth() {
        try {
            String config = getPlatformConfig(IssuesManagePlatform.Jira.toString());
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

    @Override
    public List<PlatformUser> getPlatformUser() {
        return null;
    }

    @Override
    String getProjectId() {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getJiraKey();
    }

    private Issues getJiraIssues(HttpHeaders headers, String url, String issuesId) {
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        //post
        ResponseEntity<String> responseEntity;
        Issues issues = new Issues();
        try {
            responseEntity = restTemplate.exchange(url + "/rest/api/2/issue/" + issuesId, HttpMethod.GET, requestEntity, String.class);
            String body = responseEntity.getBody();

            JSONObject obj = JSONObject.parseObject(body);
            LogUtil.info(obj);

            String lastmodify = "";
            String status = "";

            JSONObject fields = (JSONObject) obj.get("fields");
            JSONObject statusObj = (JSONObject) fields.get("status");
            JSONObject assignee = (JSONObject) fields.get("assignee");

            if (statusObj != null) {
                JSONObject statusCategory = (JSONObject) statusObj.get("statusCategory");
                status = statusCategory.getString("key");
            }

            String id = obj.getString("key");
            String title = fields.getString("summary");
            String description = fields.getString("description");

            Parser parser = Parser.builder().build();
            Node document = parser.parse(description);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            description = renderer.render(document);

            Long createTime = fields.getLong("created");

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

}
