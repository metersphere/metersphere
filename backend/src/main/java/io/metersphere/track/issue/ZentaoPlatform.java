package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.client.ZentaoClient;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.zentao.AddIssueResponse;
import io.metersphere.track.issue.domain.zentao.GetIssueResponse;
import io.metersphere.track.issue.domain.zentao.ZentaoBuild;
import io.metersphere.track.issue.domain.zentao.ZentaoConfig;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final ZentaoClient zentaoClient;

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
        String type = object.getString("request");
        this.orgId = issuesRequest.getOrganizationId();
        this.zentaoClient = ZentaoFactory.getInstance(this.url, type);
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
        issuesRequest.setPlatform(IssuesManagePlatform.Zentao.toString());
        List<IssuesDao> issues;
        if (StringUtils.isNotBlank(issuesRequest.getProjectId())) {
            issues = extIssuesMapper.getIssuesByProjectId(issuesRequest);
        } else {
            issues = extIssuesMapper.getIssuesByCaseId(issuesRequest);
        }
        return issues;
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        //getTestStories
        List<DemandDTO> list = new ArrayList<>();
        try {
            setConfig();
            String session = zentaoClient.login();
            String key = getProjectId(projectId);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(new HttpHeaders());
            RestTemplate restTemplate = new RestTemplate();
            String storyGet = zentaoClient.requestUrl.getStoryGet();
            ResponseEntity<String> responseEntity = restTemplate.exchange(storyGet + session,
                    HttpMethod.POST, requestEntity, String.class, key);
            String body = responseEntity.getBody();
            JSONObject obj = JSONObject.parseObject(body);

            LogUtil.info("project story" + key + obj);

            if (obj != null) {
                JSONObject data = obj.getJSONObject("data");
                String s = JSON.toJSONString(data);
                Map<String, Object> map = JSONArray.parseObject(s, new TypeReference<Map<String, Object>>(){});
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
        GetIssueResponse.Issue bug = zentaoClient.getBugById(bugId);
        String description = bug.getSteps();
        String steps = description;
        try {
            steps = zentao2MsDescription(description);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        IssuesDao issues = new IssuesDao();
        issues.setId(bug.getId());
        issues.setPlatformStatus(bug.getStatus());
        if (StringUtils.equals(bug.getDeleted(),"1")) {
            issues.setPlatformStatus(IssuesStatus.DELETE.toString());
            issuesMapper.updateByPrimaryKeySelective(issues);
        }
        issues.setTitle(bug.getTitle());
        issues.setDescription(steps);
        issues.setReporter(bug.getOpenedBy());
        return issues;
    }

    @Override
    public void addIssue(IssuesUpdateRequest issuesRequest) {
        issuesRequest.setPlatform(IssuesManagePlatform.Zentao.toString());

        List<CustomFieldItemDTO> customFields = getCustomFields(issuesRequest.getCustomFields());
        zentaoClient.setConfig(getUserConfig());
        String projectId = getProjectId(issuesRequest.getProjectId());
        if (StringUtils.isBlank(projectId)) {
            MSException.throwException("未关联禅道项目ID.");
        }
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("product", projectId);
        paramMap.add("title", issuesRequest.getTitle());

        customFields.forEach(item -> {
            if (StringUtils.isNotBlank(item.getCustomData())) {
                paramMap.add(item.getCustomData(), item.getValue());
            }
        });

        String description = issuesRequest.getDescription();
        String zentaoSteps = description;

        // transfer description
        try {
            zentaoSteps = ms2ZentaoDescription(description);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        LogUtil.info("zentao description transfer: " + zentaoSteps);

        paramMap.add("steps", zentaoSteps);
        if (!CollectionUtils.isEmpty(issuesRequest.getZentaoBuilds())) {
            List<String> builds = issuesRequest.getZentaoBuilds();
            builds.forEach(build -> paramMap.add("openedBuild[]", build));
        } else {
            paramMap.add("openedBuild", "trunk");
        }
        if (StringUtils.isNotBlank(issuesRequest.getZentaoAssigned())) {
            paramMap.add("assignedTo", issuesRequest.getZentaoAssigned());
        }

        AddIssueResponse.Issue issue = zentaoClient.addIssue(paramMap);
        issuesRequest.setPlatformStatus(issue.getStatus());

        String id = issue.getId();
        if (StringUtils.isNotBlank(id)) {
            issuesRequest.setId(id);
            // 用例与第三方缺陷平台中的缺陷关联
            handleTestCaseIssues(issuesRequest);

            IssuesExample issuesExample = new IssuesExample();
            issuesExample.createCriteria().andIdEqualTo(id)
                    .andPlatformEqualTo(IssuesManagePlatform.Zentao.toString());
            if (issuesMapper.selectByExample(issuesExample).size() <= 0) {
                // 插入缺陷表
                insertIssues(id, issuesRequest);
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
        setConfig();
        zentaoClient.login();
    }

    @Override
    public void userAuth(UserDTO.PlatformInfo userInfo) {
        String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
        ZentaoConfig zentaoConfig = JSONObject.parseObject(config, ZentaoConfig.class);
        zentaoConfig.setAccount(userInfo.getZentaoUserName());
        zentaoConfig.setPassword(userInfo.getZentaoPassword());
        zentaoClient.setConfig(zentaoConfig);
        zentaoClient.login();
    }

    public ZentaoConfig setConfig() {
        ZentaoConfig config = getConfig();
        zentaoClient.setConfig(config);
        return config;
    }

    public ZentaoConfig getConfig() {
        ZentaoConfig zentaoConfig = null;
        String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
        zentaoConfig = JSONObject.parseObject(config, ZentaoConfig.class);
//        validateConfig(tapdConfig);
        return zentaoConfig;
    }

    public ZentaoConfig getUserConfig() {
        ZentaoConfig zentaoConfig = null;
        String config = getPlatformConfig(IssuesManagePlatform.Zentao.toString());
        if (StringUtils.isNotBlank(config)) {
            zentaoConfig = JSONObject.parseObject(config, ZentaoConfig.class);
            UserDTO.PlatformInfo userPlatInfo = getUserPlatInfo(this.orgId);
            if (userPlatInfo != null && StringUtils.isNotBlank(userPlatInfo.getZentaoUserName())
                    && StringUtils.isNotBlank(userPlatInfo.getZentaoPassword())) {
                zentaoConfig.setAccount(userPlatInfo.getZentaoUserName());
                zentaoConfig.setPassword(userPlatInfo.getZentaoPassword());
            }
        }
//        validateConfig(jiraConfig);
        return zentaoConfig;
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
        setConfig();
        String session = zentaoClient.login();;
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        String getUser = zentaoClient.requestUrl.getUserGet();
        ResponseEntity<String> responseEntity = restTemplate.exchange(getUser + session,
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

    @Override
    public void syncIssues(Project project, List<IssuesDao> issues) {
        issues.forEach(item -> {
            setConfig();
            IssuesDao issuesDao = getZentaoIssues(item.getId());
            issuesMapper.updateByPrimaryKeySelective(issuesDao);
        });
    }

    public List<ZentaoBuild> getBuilds() {
        setConfig();
        String session = zentaoClient.login();;
        String projectId1 = getProjectId(projectId);
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        String buildGet = zentaoClient.requestUrl.getBuildsGet();
        ResponseEntity<String> responseEntity = restTemplate.exchange(buildGet + session,
                HttpMethod.GET, requestEntity, String.class, projectId1);
        String body = responseEntity.getBody();
        JSONObject obj = JSONObject.parseObject(body);

        LogUtil.info("zentao builds" + obj);

        JSONObject data = obj.getJSONObject("data");
        Map<String,Object> maps = data.getInnerMap();

        List<ZentaoBuild> list = new ArrayList<>();
        for (Map.Entry<String, Object> map : maps.entrySet()) {
            ZentaoBuild build = new ZentaoBuild();
            String id = map.getKey();
            if (StringUtils.isNotBlank(id)) {
                build.setId(map.getKey());
                build.setName((String) map.getValue());
                list.add(build);
            }
        }
        return list;
    }

    private String uploadFile(FileSystemResource resource) {
        String id = "";
        setConfig();
        String session = zentaoClient.login();
        HttpHeaders httpHeaders = new HttpHeaders();
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, httpHeaders);
        paramMap.add("files", resource);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String fileUpload = zentaoClient.requestUrl.getFileUpload();
            ResponseEntity<String> responseEntity = restTemplate.exchange(fileUpload + session,
                    HttpMethod.POST, requestEntity, String.class);
            String body = responseEntity.getBody();
            JSONObject obj = JSONObject.parseObject(body);
            JSONObject data = obj.getJSONObject("data");
            Set<String> set = data.getInnerMap().keySet();
            if (!set.isEmpty()) {
                id = (String) set.toArray()[0];
            }
        } catch (Exception e) {
            LogUtil.error(e, e.getMessage());
        }
        LogUtil.info("upload file id: " + id);
        return id;
    }

    private String ms2ZentaoDescription(String msDescription) {
        String imgUrlRegex = "!\\[.*?]\\(/resource/md/get/(.*?\\..*?)\\)";
        String zentaoSteps = msDescription.replaceAll(imgUrlRegex, zentaoClient.requestUrl.getReplaceImgUrl());
        Matcher matcher = zentaoClient.requestUrl.getImgPattern().matcher(zentaoSteps);
        while (matcher.find()) {
            // get file name
            String fileName = matcher.group(1);
            // get file
            ResponseEntity<FileSystemResource> mdImage = resourceService.getMdImage(fileName);
            // upload zentao
            String id = uploadFile(mdImage.getBody());
            // todo delete local file
            // replace id
            zentaoSteps = zentaoSteps.replaceAll(Pattern.quote(fileName), id);
        }
        // image link
        String netImgRegex = "!\\[(.*?)]\\((http.*?)\\)";
        return zentaoSteps.replaceAll(netImgRegex, "<img src=\"$2\" alt=\"$1\"/>");
    }

    private String zentao2MsDescription(String ztDescription) {
        // todo 图片回显
        String imgRegex = "<img src.*?/>";
        return ztDescription.replaceAll(imgRegex, "");
    }
}
