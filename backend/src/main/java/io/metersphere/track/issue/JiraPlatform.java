package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.client.JiraClient;
import io.metersphere.track.issue.domain.*;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
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
import java.util.stream.Collectors;

public class JiraPlatform extends AbstractIssuePlatform {

    protected String key = IssuesManagePlatform.Jira.toString();

    public JiraPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
    }

    public JiraConfig getConfig() {
        String config = getPlatformConfig(IssuesManagePlatform.Jira.toString());
        if (StringUtils.isNotBlank(config)) {
            return JSONObject.parseObject(config, JiraConfig.class);
        } else {
            return null;
        }
    }

    @Override
    public List<IssuesDao> getIssue(IssuesRequest issuesRequest) {
        List<IssuesDao> list = new ArrayList<>();

        issuesRequest.setPlatform(IssuesManagePlatform.Jira.toString());
        List<IssuesDao> issues;
        if (StringUtils.isNotBlank(issuesRequest.getProjectId())) {
            issues = extIssuesMapper.getIssuesByProjectId(issuesRequest);
        } else {
            issues = extIssuesMapper.getIssuesByCaseId(issuesRequest);
        }
        JiraConfig config = getConfig();
        JiraClient.setConfig(config);
        List<String> issuesIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            IssuesDao dto = parseIssue(JiraClient.getIssues(issuesId));
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

    public IssuesDao parseIssue(JiraIssue jiraIssue) {
        String lastmodify = "";
        String status = "";
        JSONObject fields = jiraIssue.getFields();
        JSONObject statusObj = (JSONObject) fields.get("status");
        JSONObject assignee = (JSONObject) fields.get("assignee");
        if (statusObj != null) {
            JSONObject statusCategory = (JSONObject) statusObj.get("statusCategory");
            status = statusCategory.getString("key");
        }

        String description = fields.getString("description");

        Parser parser = Parser.builder().build();
        Node document = parser.parse(description);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        description = renderer.render(document);

        if (assignee != null) {
            lastmodify = assignee.getString("displayName");
        }
        IssuesDao issues = new IssuesDao();
        issues.setId(jiraIssue.getKey());
        issues.setTitle(fields.getString("summary"));
        issues.setCreateTime(fields.getLong("created"));
        issues.setLastmodify(lastmodify);
        issues.setDescription(description);
        issues.setStatus(status);
        issues.setPlatform(IssuesManagePlatform.Jira.toString());
        return issues;
    }

    public HttpHeaders getAuthHeader(JSONObject object) {
        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");
        return auth(account, password);
    }

    @Override
    public void filter(List<IssuesDao> issues) {
        String config = getPlatformConfig(IssuesManagePlatform.Jira.toString());
        JSONObject object = JSON.parseObject(config);
        HttpHeaders headers = getAuthHeader(object);
        String url = object.getString("url");

        issues.forEach((issuesDao) -> {
            IssuesDao dto = getJiraIssues(headers, url, issuesDao.getId());
            if (StringUtils.isBlank(dto.getId())) {
                // 标记成删除
                issuesDao.setStatus(IssuesStatus.DELETE.toString());
            } else {
                // 缺陷状态为 完成，则不显示
                if (!StringUtils.equals("done", dto.getStatus())) {
                    issuesDao.setStatus(IssuesStatus.RESOLVED.toString());
                }
            }
        });
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        List<DemandDTO> list = new ArrayList<>();

        try {
            String key = this.getProjectId(projectId);
            if (StringUtils.isBlank(key)) {
                MSException.throwException("未关联Jira 项目Key");
            }
            String config = getPlatformConfig(IssuesManagePlatform.Jira.toString());
            JSONObject object = JSON.parseObject(config);

            if (object == null) {
                MSException.throwException("jira config is null");
            }

            String account = object.getString("account");
            String password = object.getString("password");
            String url = object.getString("url");
            String type = object.getString("storytype");
            String auth = EncryptUtils.base64Encoding(account + ":" + password);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Authorization", "Basic " + auth);
            requestHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            //HttpEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            //post
            ResponseEntity<String> responseEntity = null;
            responseEntity = restTemplate.exchange(url + "/rest/api/2/search?jql=project="+key+"+AND+issuetype="+type+"&fields=summary,issuetype",
                    HttpMethod.GET, requestEntity, String.class);
            String body = responseEntity.getBody();
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONArray jsonArray = jsonObject.getJSONArray("issues");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                String issueKey = o.getString("key");
                JSONObject fields = o.getJSONObject("fields");
                String summary = fields.getString("summary");
                DemandDTO demandDTO = new DemandDTO();
                demandDTO.setName(summary);
                demandDTO.setId(issueKey);
                demandDTO.setPlatform(IssuesManagePlatform.Jira.name());
                list.add(demandDTO);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("调用Jira查询需求失败");
        }

        return list;
    }

    @Override
    public void addIssue(IssuesUpdateRequest issuesRequest) {
        issuesRequest.setPlatform(IssuesManagePlatform.Jira.toString());

        JiraConfig config = getConfig();
        if (config == null) {
            MSException.throwException("jira config is null");
        }
        if (StringUtils.isBlank(config.getIssuetype())) {
            MSException.throwException("Jira 问题类型为空");
        }

        String jiraKey = getProjectId(issuesRequest.getProjectId());

        if (StringUtils.isBlank(jiraKey)) {
            MSException.throwException("未关联Jira 项目Key");
        }

        String content = issuesRequest.getDescription();

        Document document = Jsoup.parse(content);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        String desc = Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        desc = desc.replace("&nbsp;", "");

        JSONObject addJiraIssueParam = new JSONObject();
        JSONObject fields = new JSONObject();
        JSONObject project = new JSONObject();


        fields.put("project", project);
        project.put("key", jiraKey);

        JSONObject issuetype = new JSONObject();
        issuetype.put("name", config.getIssuetype());

        fields.put("summary", issuesRequest.getTitle());
        fields.put("description", new JiraIssueDescription(desc));
        fields.put("issuetype", issuetype);
        addJiraIssueParam.put("fields", fields);

        List<CustomFieldItemDTO> customFields = getCustomFields(issuesRequest.getCustomFields());
        JiraClient.setConfig(config);
//        List<JiraField> jiraFields = JiraClient.getFields();
//        Map<String, Boolean> isCustomMap = jiraFields.stream().
//                collect(Collectors.toMap(JiraField::getId, JiraField::isCustom));

        customFields.forEach(item -> {
            if (StringUtils.isNotBlank(item.getCustomData())) {
//                if (isCustomMap.get(item.getCustomData())) {
//                    fields.put(item.getCustomData(), item.getValue());
//                } else {
                  // Jira文档说明中自定义字段和系统字段参数格式有区别，实测是一样的
                    JSONObject param = new JSONObject();
                    param.put("id", item.getValue());
                    fields.put(item.getCustomData(), param);
//                }
            }
        });
        JiraAddIssueResponse result = JiraClient.addIssue(JSONObject.toJSONString(addJiraIssueParam));

        issuesRequest.setId(result.getKey());
        // 用例与第三方缺陷平台中的缺陷关联
        handleTestCaseIssues(issuesRequest);

        // 插入缺陷表
        insertIssuesWithoutContext(result.getKey(), issuesRequest);
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        // todo 调用接口
        request.setDescription(null);
        handleIssueUpdate(request);
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
            // 忽略ssl
            restTemplateIgnoreSSL.exchange(url + "rest/api/2/issue/createmeta", HttpMethod.GET, requestEntity, String.class);
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
    String getProjectId(String projectId) {
        if (StringUtils.isNotBlank(projectId)) {
            return projectService.getProjectById(projectId).getJiraKey();
        }
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getJiraKey();
    }

    public IssuesDao getJiraIssues(HttpHeaders headers, String url, String issuesId) {
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        //post
        ResponseEntity<String> responseEntity;
        IssuesDao issues = new IssuesDao();
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
            return new IssuesDao();
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
