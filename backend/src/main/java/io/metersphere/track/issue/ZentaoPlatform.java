package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class ZentaoPlatform extends AbstractIssuePlatform {

    public ZentaoPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
    }

    @Override
    String getProjectId() {
//        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
//        Project project = projectService.getProjectById(testCase.getProjectId());
        // todo return getZentao ID
        return "002";
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
        String projectId = getProjectId();

        String session = login();

        String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
        JSONObject object = JSON.parseObject(config);
        String account = object.getString("account");
        String password = object.getString("password");
        String url = object.getString("url");
        url = ZentaoUtils.getUrl(url, account, password);
        System.out.println(url);
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://xx/zentao/bug-view-"+ bugId +".json?zentaosid=" + session, HttpMethod.POST, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);
        System.out.println(obj);
        if (obj != null) {
            JSONObject data = obj.getJSONObject("data");
            JSONArray bugs = data.getJSONArray("bugs");
            for (int j = 0; j < bugs.size(); j++) {
                JSONObject bug = bugs.getJSONObject(j);
                String id = bug.getString("id");
                String title = bug.getString("title");
                String description = bug.getString("steps");
                Long createTime = bug.getLong("openedDate");
                String status = bug.getString("status");
                String reporter = bug.getString("openedBy");

                Issues issues = new Issues();
                issues.setId(id);
                issues.setTitle(title);
                issues.setDescription(description);
                issues.setCreateTime(createTime);
                issues.setStatus(status);
                issues.setReporter(reporter);
                return issues;
            }
        }
        return new Issues();
    }

    @Override
    public void addIssue(IssuesRequest issuesRequest) {
        String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
        String session = login();
        JSONObject object = JSON.parseObject(config);
//        String account = object.getString("account");
//        String password = object.getString("password");

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("product", "003");
        paramMap.add("title", issuesRequest.getTitle());
        paramMap.add("openedBuild", "123");
        paramMap.add("steps", issuesRequest.getContent());
        paramMap.add("assignedTo", "admin");

        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("zentao add bug");
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://xx/zentao/bug-create-3.json?zentaosid=" + session, HttpMethod.POST, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);
        if (obj != null) {
            String id = obj.getString("data");
            if (StringUtils.isNotBlank(id)) {
                // 用例与第三方缺陷平台中的缺陷关联
                TestCaseIssues testCaseIssues = new TestCaseIssues();
                testCaseIssues.setId(UUID.randomUUID().toString());
                testCaseIssues.setIssuesId(id);
                testCaseIssues.setTestCaseId(testCaseId);
                testCaseIssuesMapper.insert(testCaseIssues);

                // 插入缺陷表
                Issues issues = new Issues();
                issues.setId(id);
                issues.setPlatform(IssuesManagePlatform.Zentao.toString());
                issuesMapper.insert(issues);
            }
        }

        System.out.println(obj);
//        return responseEntity.getBody();
    }

    @Override
    public void deleteIssue(String id) {

    }

    @Override
    public void testAuth() {
        try {
            String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
            JSONObject object = JSON.parseObject(config);
            String account = object.getString("account");
            String password = object.getString("password");
            String url = object.getString("url");
            url = ZentaoUtils.getUrl(url, account, password);
            HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(new HttpHeaders());
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> exchange = restTemplate.exchange(url + "&m=user&f=apilogin", HttpMethod.GET, requestEntity, String.class);

            String body = exchange.getBody();
            JSONObject obj = JSONObject.parseObject(body);
            System.out.println(obj);
            if (obj != null) {
                String errcode = obj.getString("errcode");
                String errmsg = obj.getString("errmsg");
                if (StringUtils.isNotBlank(errcode)) {
                    MSException.throwException(errmsg);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("验证失败！");
        }

    }


    private String login() {
        String session = getSession();
        String loginUrl = "http://xx/zentao/user-login.json?zentaosid=" + session;
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("account", "admin");
        paramMap.add("password", "xx");

        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(paramMap, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(loginUrl, HttpMethod.POST, requestEntity, String.class);
        return session;
    }

    private String getSession() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://xx.xxx.xxx.xxx/zentao/api-getsessionid.json", HttpMethod.GET, requestEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);
        JSONObject data = obj.getJSONObject("data");
        String session = data.getString("sessionID");
        return session;
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
        return null;
    }
}
