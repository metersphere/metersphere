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
import io.metersphere.track.issue.client.JiraClientV2;
import io.metersphere.track.issue.domain.JiraAddIssueResponse;
import io.metersphere.track.issue.domain.JiraConfig;
import io.metersphere.track.issue.domain.JiraIssue;
import io.metersphere.track.issue.domain.PlatformUser;
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
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class JiraPlatform extends AbstractIssuePlatform {

    protected String key = IssuesManagePlatform.Jira.toString();

    private JiraClientV2 jiraClientV2 = new JiraClientV2();

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
        setConfig();
        issues.forEach(item -> {
            String issuesId = item.getId();
            parseIssue(item, jiraClientV2.getIssues(issuesId));
            if (StringUtils.isBlank(item.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                issuesExample.createCriteria()
                        .andTestCaseIdEqualTo(testCaseId)
                        .andIssuesIdEqualTo(issuesId);
                testCaseIssuesMapper.deleteByExample(issuesExample);
                issuesMapper.deleteByPrimaryKey(issuesId);
            } else {
                // 缺陷状态为 完成，则不显示
                if (!StringUtils.equals("done", item.getStatus())) {
                    list.add(item);
                }
            }
        });
        return list;
    }

    public void parseIssue(IssuesWithBLOBs item, JiraIssue jiraIssue) {
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
        item.setId(jiraIssue.getKey());
        item.setTitle(fields.getString("summary"));
        item.setCreateTime(fields.getLong("created"));
        item.setLastmodify(lastmodify);
        item.setDescription(description);
        item.setStatus(status);
        item.setPlatform(IssuesManagePlatform.Jira.toString());
    }

    @Override
    public void filter(List<IssuesDao> issues) {
        setConfig();
        issues.forEach((issuesDao) -> {
            parseIssue(issuesDao, jiraClientV2.getIssues(issuesDao.getId()));
            if (StringUtils.isBlank(issuesDao.getId())) {
                // 标记成删除
                issuesDao.setStatus(IssuesStatus.DELETE.toString());
            } else {
                // 缺陷状态为 完成，则不显示
                if (!StringUtils.equals("done", issuesDao.getStatus())) {
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
        jiraClientV2.setConfig(config);

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

        JSONObject fields = new JSONObject();
        JSONObject project = new JSONObject();


        fields.put("project", project);
        project.put("key", jiraKey);

        JSONObject issuetype = new JSONObject();
        issuetype.put("name", config.getIssuetype());

        fields.put("summary", issuesRequest.getTitle());
//        fields.put("description", new JiraIssueDescription(desc));
        fields.put("description", desc);
        fields.put("issuetype", issuetype);

        JSONObject addJiraIssueParam = new JSONObject();
        addJiraIssueParam.put("fields", fields);

        List<CustomFieldItemDTO> customFields = getCustomFields(issuesRequest.getCustomFields());
        jiraClientV2.setConfig(config);
//        List<JiraField> jiraFields = JiraClientV2.getFields();
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
        JiraAddIssueResponse result = jiraClientV2.addIssue(JSONObject.toJSONString(addJiraIssueParam));

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

    @Override
    public void deleteIssue(String id) {
    }

    @Override
    public void testAuth() {
        setConfig();
        jiraClientV2.getIssueCreateMetadata();
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

    public void setConfig() {
        JiraConfig config = getConfig();
        jiraClientV2.setConfig(config);
    }

    public IssuesWithBLOBs getJiraIssues(IssuesWithBLOBs issuesDao, String issueId) {
        setConfig();
        if (issuesDao == null) {
            issuesDao = new IssuesDao();
        }
        parseIssue(issuesDao, jiraClientV2.getIssues(issueId));
        return issuesDao;
    }

}
