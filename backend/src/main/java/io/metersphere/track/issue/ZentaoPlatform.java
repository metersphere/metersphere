package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.ZentaoBuild;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    protected String key = IssuesManagePlatform.Zentao.toString();

    public ZentaoPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
        String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
        // todo
        if (StringUtils.isBlank(config)) {
            MSException.throwException("未集成禅道平台!");
        }
        JSONObject object = JSON.parseObject(config);
        this.account = object.getString("account");
        this.password = object.getString("password");
        this.url = object.getString("url");
    }

    public ZentaoPlatform(String account, String password, String url) {
        super(new IssuesRequest());
        this.account = account;
        this.password = password;
        this.url = url;
    }

    @Override
    String getProjectId(String projectId) {
        if (StringUtils.isNotBlank(projectId)) {
            return projectService.getProjectById(projectId).getZentaoId();
        }
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getZentaoId();
    }

    @Override
    public List<IssuesDao> getIssue(IssuesRequest issuesRequest) {
        List<IssuesDao> list = new ArrayList<>();

        issuesRequest.setPlatform(IssuesManagePlatform.Zentao.toString());


        List<IssuesDao> issues;
        if (StringUtils.isNotBlank(issuesRequest.getProjectId())) {
            issues = extIssuesMapper.getIssuesByProjectId(issuesRequest);
        } else {
            issues = extIssuesMapper.getIssuesByCaseId(issuesRequest);
        }

        issues.forEach(item -> {
            String issuesId = item.getId();
            IssuesDao dto = getZentaoIssues(issuesId);
            dto.setNum(item.getNum());
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                TestCaseIssuesExample.Criteria criteria = issuesExample.createCriteria();
                if (StringUtils.isNotBlank(testCaseId)) {
                    criteria.andTestCaseIdEqualTo(testCaseId);
                }
                criteria.andIssuesIdEqualTo(issuesId);
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

    @Override
    public void filter(List<IssuesDao> issues) {
        issues.forEach((issuesDao) -> {
            IssuesDao dto = getZentaoIssues(issuesDao.getId());
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
        //getTestStories
        List<DemandDTO> list = new ArrayList<>();
        try {
            String session = login();
            String key = getProjectId(projectId);
            HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(new HttpHeaders());
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(url + "api-getModel-story-getProductStories-productID={key}?zentaosid=" + session,
                    HttpMethod.POST, requestEntity, String.class, key);
            String body = responseEntity.getBody();
            JSONObject obj = JSONObject.parseObject(body);

            LogUtil.info("project story" + key + obj);

            if (obj != null) {
                JSONObject data = obj.getJSONObject("data");
                String s = JSON.toJSONString(data);
                Map<String, Object> map = JSONArray.parseObject(s, Map.class);
                Collection<Object> values = map.values();
                values.forEach(v -> {
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(v));
                    DemandDTO demandDTO = new DemandDTO();
                    demandDTO.setId(jsonObject.getString("id"));
                    demandDTO.setName(jsonObject.getString("title"));
                    demandDTO.setPlatform(IssuesManagePlatform.Zentao.name());
                    list.add(demandDTO);
                });

            }
        } catch (Exception e) {
            LogUtil.error("get zentao bug fail " + e.getMessage());
        }
        return list;
    }

    public IssuesDao getZentaoIssues(String bugId) {
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
                    return new IssuesDao();
                }
                IssuesDao issues = new IssuesDao();
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

        return new IssuesDao();
    }

    @Override
    public void addIssue(IssuesUpdateRequest issuesRequest) {
        issuesRequest.setPlatform(IssuesManagePlatform.Zentao.toString());

        String session = login();
        String projectId = getProjectId(issuesRequest.getProjectId());

        if (StringUtils.isBlank(projectId)) {
            MSException.throwException("未关联禅道项目ID.");
        }

        if (StringUtils.isBlank(session)) {
            MSException.throwException("session is null");
        }

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("product", projectId);
        paramMap.add("title", issuesRequest.getTitle());
        paramMap.add("steps", issuesRequest.getDescription());
        if (!CollectionUtils.isEmpty(issuesRequest.getZentaoBuilds())) {
            List<String> builds = issuesRequest.getZentaoBuilds();
            builds.forEach(build -> {
                paramMap.add("openedBuild[]", build);
            });
        } else {
            paramMap.add("openedBuild", "trunk");
        }
        if (StringUtils.isNotBlank(issuesRequest.getZentaoAssigned())) {
            paramMap.add("assignedTo", issuesRequest.getZentaoAssigned());
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
                issuesRequest.setId(id);
                // 用例与第三方缺陷平台中的缺陷关联
                handleTestCaseIssues(issuesRequest);

                IssuesExample issuesExample = new IssuesExample();
                issuesExample.createCriteria().andIdEqualTo(id)
                        .andPlatformEqualTo(IssuesManagePlatform.Zentao.toString());
                if (issuesMapper.selectByExample(issuesExample).size() <= 0) {
                    // 插入缺陷表
                    insertIssuesWithoutContext(id, issuesRequest);
                }
            }
        }
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
        String projectId1 = getProjectId(projectId);
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(url + "api-getModel-build-getProductBuildPairs-productID={projectId}?zentaosid=" + session,
                HttpMethod.GET, requestEntity, String.class, projectId1);
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
