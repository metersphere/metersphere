package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.ZentaoBuild;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class ZentaoPlatform extends AbstractIssuePlatform {
    /**
     * zentao account
     */
    private final String account;
    /**
     * zentao password
     */
    private final String password;
    /**
     * zentao url eg:http://x.x.x.x/zentao
     */
    private final String url;

    public ZentaoPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
        String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
        JSONObject object = JSON.parseObject(config);
        this.account = object.getString("account");
        this.password = object.getString("password");
        this.url = object.getString("url");
    }

    @Override
    String getProjectId() {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getZentaoId();
    }

    @Override
    public List<Issues> getIssue() {
        List<Issues> list = new ArrayList<>();

        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andTestCaseIdEqualTo(testCaseId);

        List<Issues> issues = extIssuesMapper.getIssues(testCaseId, IssuesManagePlatform.Zentao.toString());

        List<String> issuesIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            Issues dto = getZentaoIssues(issuesId);
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                issuesExample.createCriteria()
                        .andTestCaseIdEqualTo(testCaseId)
                        .andIssuesIdEqualTo(issuesId);
                testCaseIssuesMapper.deleteByExample(issuesExample);
                issuesMapper.deleteByPrimaryKey(issuesId);
            } else {
                dto.setPlatform(IssuesManagePlatform.Zentao.toString());
                // 缺陷状态为 关闭，则不显示
                if (!StringUtils.equals("closed", dto.getStatus())) {
                    list.add(dto);
                }
            }
        });
        return list;

    }

    private Issues getZentaoIssues(String bugId) {
        String session = login();
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url + "api-getModel-bug-getById-bugID={bugId}?zentaosid=" + session,
                    HttpMethod.POST, requestEntity, String.class, bugId);
            String body = responseEntity.getBody();
            JSONObject obj = JSONObject.parseObject(body);

            LogUtil.info("bug id is " + bugId + obj);

            if (obj != null) {
                JSONObject bug = obj.getJSONObject("data");
                String id = bug.getString("id");
                String title = bug.getString("title");
                String description = bug.getString("steps");
                Long createTime = bug.getLong("openedDate");
                String status = bug.getString("status");
                String reporter = bug.getString("openedBy");
                int deleted = bug.getInteger("deleted");
                if (deleted == 1) {
                    return new Issues();
                }
                Issues issues = new Issues();
                issues.setId(id);
                issues.setTitle(title);
                issues.setDescription(description);
                issues.setCreateTime(createTime);
                issues.setStatus(status);
                issues.setReporter(reporter);
                return issues;
            }
        } catch (Exception e) {
            LogUtil.error("get zentao bug fail " + e.getMessage());
        }

        return new Issues();
    }

    @Override
    public void addIssue(IssuesRequest issuesRequest) {

        String session = login();
        String projectId = getProjectId();

        if (StringUtils.isBlank(projectId)) {
            MSException.throwException("add zentao bug fail, project zentao id is null");
        }

        if (StringUtils.isBlank(session)) {
            MSException.throwException("session is null");
        }

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("product", projectId);
        paramMap.add("title", issuesRequest.getTitle());
        paramMap.add("steps", issuesRequest.getContent());
        if (!CollectionUtils.isEmpty(issuesRequest.getZentaoBuilds())) {
            List<String> builds = issuesRequest.getZentaoBuilds();
            builds.forEach(build -> {
                paramMap.add("openedBuild[]", build);
            });
        } else {
            paramMap.add("openedBuild", "trunk");
        }
        if (StringUtils.isNotBlank(issuesRequest.getZentaoUser())) {
            paramMap.add("assignedTo", issuesRequest.getZentaoUser());
        }

        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(url + "api-getModel-bug-create.json?zentaosid=" + session, HttpMethod.POST, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);

        LogUtil.info("add zentao bug " + obj);

        if (obj != null) {
            JSONObject data = obj.getJSONObject("data");
            String id = data.getString("id");
            if (StringUtils.isNotBlank(id)) {
                // 用例与第三方缺陷平台中的缺陷关联
                TestCaseIssues testCaseIssues = new TestCaseIssues();
                testCaseIssues.setId(UUID.randomUUID().toString());
                testCaseIssues.setIssuesId(id);
                testCaseIssues.setTestCaseId(testCaseId);
                testCaseIssuesMapper.insert(testCaseIssues);

                IssuesExample issuesExample = new IssuesExample();
                issuesExample.createCriteria().andIdEqualTo(id)
                        .andPlatformEqualTo(IssuesManagePlatform.Zentao.toString());
                if (issuesMapper.selectByExample(issuesExample).size() <= 0) {
                    // 插入缺陷表
                    Issues issues = new Issues();
                    issues.setId(id);
                    issues.setPlatform(IssuesManagePlatform.Zentao.toString());
                    issuesMapper.insert(issues);
                }
            }
        }
    }

    @Override
    public void deleteIssue(String id) {

    }

    @Override
    public void testAuth() {
        try {
            login();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("验证失败！");
        }

    }


    private String login() {
        String session = getSession();
        String loginUrl = url + "user-login.json?zentaosid=" + session;
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("account", account);
        paramMap.add("password", password);
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(loginUrl, HttpMethod.POST, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);
        JSONObject user = obj.getJSONObject("user");
        if (user == null) {
            LogUtil.error("login fail");
            LogUtil.error(obj);
            // 登录失败，获取的session无效，置空session
            MSException.throwException("zentao login fail");
        }
        String username = user.getString("account");
        if (!StringUtils.equals(username, account)) {
            LogUtil.error("login fail，inconsistent users");
            MSException.throwException("zentao login fail");
        }
        return session;
    }

    private String getSession() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> responseEntity = restTemplate.exchange(url + "api-getsessionid.json", HttpMethod.GET, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);
        JSONObject data = obj.getJSONObject("data");
        String session = data.getString("sessionID");
        return session;
    }

    @Override
    public List<PlatformUser> getPlatformUser() {

        String session = login();
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(url + "api-getModel-user-getList?zentaosid=" + session,
                HttpMethod.GET, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);

        LogUtil.info("zentao user " + obj);

        JSONArray data = obj.getJSONArray("data");

        List<PlatformUser> users = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject o = data.getJSONObject(i);
            PlatformUser platformUser = new PlatformUser();
            String account = o.getString("account");
            String username = o.getString("realname");
            platformUser.setName(username);
            platformUser.setUser(account);
            users.add(platformUser);
        }
        return users;
    }

    public List<ZentaoBuild> getBuilds() {
        String session = login();
        String projectId = getProjectId();
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(url + "api-getModel-build-getProductBuildPairs-productID={projectId}?zentaosid=" + session,
                HttpMethod.GET, requestEntity, String.class, projectId);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);

        LogUtil.info("zentao builds" + obj);

        JSONObject data = obj.getJSONObject("data");
        Map<String,Object> maps = data.getInnerMap();

        List<ZentaoBuild> list = new ArrayList<>();
        for (Map.Entry map : maps.entrySet()) {
            ZentaoBuild build = new ZentaoBuild();
            String id = (String) map.getKey();
            if (StringUtils.isNotBlank(id)) {
                build.setId((String) map.getKey());
                build.setName((String) map.getValue());
                list.add(build);
            }
        }
        return list;
    }
}
