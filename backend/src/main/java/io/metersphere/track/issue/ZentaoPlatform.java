package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.IssuesExample;
import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.UserDTO;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.client.ZentaoClient;
import io.metersphere.track.issue.client.ZentaoGetClient;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.zentao.AddIssueResponse;
import io.metersphere.track.issue.domain.zentao.GetIssueResponse;
import io.metersphere.track.issue.domain.zentao.ZentaoBuild;
import io.metersphere.track.issue.domain.zentao.ZentaoConfig;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZentaoPlatform extends AbstractIssuePlatform {
    protected final ZentaoClient zentaoClient;

    protected final String[] imgArray = {
        "bmp", "jpg", "png", "tif", "gif", "jpeg"
    };


    public ZentaoPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
        this.key = IssuesManagePlatform.Zentao.name();
        ZentaoConfig zentaoConfig = getConfig();
        this.workspaceId = issuesRequest.getWorkspaceId();
        this.zentaoClient = ZentaoFactory.getInstance(zentaoConfig.getUrl(), zentaoConfig.getRequest());
        this.zentaoClient.setConfig(zentaoConfig);
    }

    @Override
    public String getProjectId(String projectId) {
        return getProjectId(projectId, Project::getZentaoId);
    }

    @Override
    public List<IssuesDao> getIssue(IssuesRequest issuesRequest) {
        issuesRequest.setPlatform(key);
        List<IssuesDao> issues;
        if (StringUtils.isNotBlank(issuesRequest.getProjectId())) {
            issues = extIssuesMapper.getIssues(issuesRequest);
        } else {
            issues = extIssuesMapper.getIssuesByCaseId(issuesRequest);
        }
        return issues;
    }

    public IssuesDao getZentaoAssignedAndBuilds(IssuesDao issue){
        JSONObject zentaoIssue = zentaoClient.getBugById(issue.getPlatformId());
        String assignedTo = zentaoIssue.getString("assignedTo");
        String openedBuild = zentaoIssue.getString("openedBuild");
        List<String>zentaoBuilds = new ArrayList<>();
        if(Strings.isNotBlank(openedBuild)){
            zentaoBuilds = Arrays.asList(openedBuild.split(","));
        }
        issue.setZentaoAssigned(assignedTo);
        issue.setZentaoBuilds(zentaoBuilds);
        return issue;
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        //getTestStories
        List<DemandDTO> list = new ArrayList<>();
        try {
            String session = zentaoClient.login();
            String key = getProjectId(projectId);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(new HttpHeaders());
            RestTemplate restTemplate = new RestTemplate();
            String storyGet = zentaoClient.requestUrl.getStoryGet();
            ResponseEntity<String> responseEntity = restTemplate.exchange(storyGet + session,
                    HttpMethod.POST, requestEntity, String.class, key);
            String body = responseEntity.getBody();
            JSONObject obj = JSONObject.parseObject(body);

            LogUtil.info("project story: " + key + obj);

            if (obj != null) {
                String data = obj.getString("data");
                if (StringUtils.isBlank(data)) {
                    return list;
                }
                // 兼容处理11.5版本格式 [{obj},{obj}]
                if (data.charAt(0) == '[') {
                    JSONArray array = obj.getJSONArray("data");
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        DemandDTO demandDTO = new DemandDTO();
                        demandDTO.setId(o.getString("id"));
                        demandDTO.setName(o.getString("title"));
                        demandDTO.setPlatform(key);
                        list.add(demandDTO);
                    }
                }
                // 处理格式 {{"id": {obj}},{"id",{obj}}}
                else if (data.charAt(0) == '{') {
                    JSONObject dataObject = obj.getJSONObject("data");
                    String s = JSON.toJSONString(dataObject);
                    Map<String, Object> map = JSONArray.parseObject(s, new TypeReference<Map<String, Object>>(){});
                    Collection<Object> values = map.values();
                    values.forEach(v -> {
                        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(v));
                        DemandDTO demandDTO = new DemandDTO();
                        demandDTO.setId(jsonObject.getString("id"));
                        demandDTO.setName(jsonObject.getString("title"));
                        demandDTO.setPlatform(key);
                        list.add(demandDTO);
                    });
                }
            }
        } catch (Exception e) {
            LogUtil.error("get zentao demand fail " + e.getMessage());
        }
        return list;
    }

    /**
     * 更新缺陷数据
     * @param issue 待更新缺陷数据
     * @param bug 平台缺陷数据
     * @return
     */
    public IssuesWithBLOBs getUpdateIssues(IssuesWithBLOBs issue, JSONObject bug) {
        GetIssueResponse.Issue bugObj = JSONObject.parseObject(bug.toJSONString(), GetIssueResponse.Issue.class);
        String description = bugObj.getSteps();
        String steps = description;
        try {
            steps = htmlDesc2MsDesc(zentao2MsDescription(description));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        if (issue == null) {
            issue = new IssuesWithBLOBs();
            issue.setCustomFields(defaultCustomFields);
        } else {
            mergeCustomField(issue, defaultCustomFields);
        }
        issue.setPlatformStatus(bugObj.getStatus());
        if (StringUtils.equals(bugObj.getDeleted(),"1")) {
            issue.setPlatformStatus(IssuesStatus.DELETE.toString());
            issuesMapper.updateByPrimaryKeySelective(issue);
        }
        issue.setTitle(bugObj.getTitle());
        issue.setDescription(steps);
        issue.setReporter(bugObj.getOpenedBy());
        issue.setPlatform(key);
        try {
            String openedDate = bug.getString("openedDate");
            String lastEditedDate = bug.getString("lastEditedDate");
            if (StringUtils.isNotBlank(openedDate) && !openedDate.startsWith("0000-00-00"))
                issue.setCreateTime(bug.getLong("openedDate"));
            if (StringUtils.isNotBlank(lastEditedDate)  && !lastEditedDate.startsWith("0000-00-00"))
                issue.setUpdateTime(bug.getLong("lastEditedDate"));
        } catch (Exception e) {
            LogUtil.error("update zentao time" + e.getMessage());
        }
        if (issue.getUpdateTime() == null) {
            issue.setUpdateTime(System.currentTimeMillis());
        }
        issue.setCustomFields(syncIssueCustomField(issue.getCustomFields(), bug));
        return issue;
    }

    @Override
    public IssuesWithBLOBs addIssue(IssuesUpdateRequest issuesRequest) {
        setUserConfig();

        MultiValueMap<String, Object> param = buildUpdateParam(issuesRequest);
        AddIssueResponse.Issue issue = zentaoClient.addIssue(param);
        issuesRequest.setPlatformStatus(issue.getStatus());

        IssuesWithBLOBs issues = null;

        String id = issue.getId();
        if (StringUtils.isNotBlank(id)) {
            issuesRequest.setPlatformId(id);
            issuesRequest.setId(UUID.randomUUID().toString());

            IssuesExample issuesExample = new IssuesExample();
            issuesExample.createCriteria().andIdEqualTo(id)
                    .andPlatformEqualTo(key);
            if (issuesMapper.selectByExample(issuesExample).size() <= 0) {
                // 插入缺陷表
                issues = insertIssues(issuesRequest);
            }

            // 用例与第三方缺陷平台中的缺陷关联
            handleTestCaseIssues(issuesRequest);
        } else {
            MSException.throwException("请确认该Zentao账号是否开启超级modle调用接口权限");
        }
        return issues;
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        setUserConfig();
        MultiValueMap<String, Object> param = buildUpdateParam(request);
        handleIssueUpdate(request);
        zentaoClient.updateIssue(request.getPlatformId(), param);
    }

    private MultiValueMap<String, Object> buildUpdateParam(IssuesUpdateRequest issuesRequest) {
        issuesRequest.setPlatform(key);
        String projectId = getProjectId(issuesRequest.getProjectId());
        if (StringUtils.isBlank(projectId)) {
            MSException.throwException("未关联禅道项目ID.");
        }
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("product", projectId);
        paramMap.add("title", issuesRequest.getTitle());

        addCustomFields(issuesRequest, paramMap);

        String description = issuesRequest.getDescription();
        String zentaoSteps = description;

        // transfer description
        try {
            zentaoSteps = ms2ZentaoDescription(description);
            zentaoSteps = zentaoSteps.replaceAll("\\n", "<br/>");
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
        return paramMap;
    }

    @Override
    public void deleteIssue(String id) {
        IssuesWithBLOBs issuesWithBLOBs = issuesMapper.selectByPrimaryKey(id);
        super.deleteIssue(id);
        zentaoClient.deleteIssue(issuesWithBLOBs.getPlatformId());
    }

    @Override
    public void testAuth() {
        zentaoClient.login();
    }

    @Override
    public void userAuth(UserDTO.PlatformInfo userInfo) {
        setUserConfig(userInfo);
        zentaoClient.login();
    }

    public ZentaoConfig getConfig() {
        return getConfig(key, ZentaoConfig.class);
    }

    public ZentaoConfig setConfig() {
        ZentaoConfig config = getConfig();
        zentaoClient.setConfig(config);
        return config;
    }

    public ZentaoConfig setUserConfig() {
        return setUserConfig(getUserPlatInfo(this.workspaceId));
    }

    public ZentaoConfig setUserConfig(UserDTO.PlatformInfo userPlatInfo) {
        ZentaoConfig zentaoConfig = getConfig();
        if (userPlatInfo != null && StringUtils.isNotBlank(userPlatInfo.getZentaoUserName())
                && StringUtils.isNotBlank(userPlatInfo.getZentaoPassword())) {
            zentaoConfig.setAccount(userPlatInfo.getZentaoUserName());
            zentaoConfig.setPassword(userPlatInfo.getZentaoPassword());
        }
        zentaoClient.setConfig(zentaoConfig);
        return zentaoConfig;
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
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
            IssuesWithBLOBs issue = issuesMapper.selectByPrimaryKey(item.getId());
            JSONObject bug = zentaoClient.getBugById(item.getPlatformId());
            issue = getUpdateIssues(issue, bug);
            customFieldIssuesService.addFields(item.getId(), customFieldService.getCustomFieldResource(issue.getCustomFields()));
            issue.setId(item.getId());
            issuesMapper.updateByPrimaryKeySelective(issue);
        });
    }

    public List<ZentaoBuild> getBuilds() {
        Map<String, Object> builds = zentaoClient.getBuildsByCreateMetaData(getProjectId(projectId));
        if (builds == null || builds.isEmpty()) {
            builds = zentaoClient.getBuilds(getProjectId(projectId));
        }
        List<ZentaoBuild> res = new ArrayList<>();
        builds.forEach((k, v) -> {
            if (StringUtils.isNotBlank(k)) {
                res.add(new ZentaoBuild(k, v.toString()));
            }
        });
        return res;
    }

    private String uploadFile(FileSystemResource resource) {
        String id = "";
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
        String imgUrlRegex = "!\\[.*?]\\(/resource/md/get(.*?\\..*?)\\)";
        String zentaoSteps = msDescription.replaceAll(imgUrlRegex, zentaoClient.requestUrl.getReplaceImgUrl());
        Matcher matcher = zentaoClient.requestUrl.getImgPattern().matcher(zentaoSteps);
        while (matcher.find()) {
            // get file name
            String originSubUrl = matcher.group(1);
            String fileName = originSubUrl.substring(10);
            fileName = resourceService.decodeFileName(fileName);
            // get file
            ResponseEntity<FileSystemResource> mdImage = resourceService.getMdImage(fileName);
            // upload zentao
            String id = uploadFile(mdImage.getBody());
            // todo delete local file
            int index = fileName.lastIndexOf(".");
            String suffix = "";
            if (index != -1) {
                suffix = fileName.substring(index);
            }
            // replace id
            zentaoSteps = zentaoSteps.replaceAll(Pattern.quote(originSubUrl), id + suffix);
        }
        // image link
        String netImgRegex = "!\\[(.*?)]\\((http.*?)\\)";
        return zentaoSteps.replaceAll(netImgRegex, "<img src=\"$2\" alt=\"$1\"/>");
    }

    private String zentao2MsDescription(String ztDescription) {
        String imgRegex ="<img src.*?/>";
        Pattern pattern = Pattern.compile(imgRegex);
        Matcher matcher = pattern.matcher(ztDescription);
        while (matcher.find()) {
            if (StringUtils.isNotEmpty(matcher.group())) {
                // img标签内容
                String imgPath = matcher.group();
                // 解析标签内容为图片超链接格式，进行替换，
                String src = getMatcherResultForImg("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)", imgPath);
                String alt = getMatcherResultForImg("alt\\s*=\\s*\"?(.*?)(\"|>|\\s+)", imgPath);
                String hyperLinkPath = packageDescriptionByPathAndName(src, alt);
                imgPath = transferSpecialCharacter(imgPath);
                ztDescription = ztDescription.replaceAll(imgPath, hyperLinkPath);
            }
        }

        return ztDescription;
    }

    private String packageDescriptionByPathAndName(String path, String name) {
        String result = "";

        if (StringUtils.isNotEmpty(path)) {
            if (path.startsWith("{") && path.endsWith("}")) {
                String srcContent = path.substring(1, path.length() - 1);
                if (StringUtils.isEmpty(name)) {
                    name = srcContent;
                }

                if (Arrays.stream(imgArray).anyMatch(imgType -> StringUtils.equals(imgType, srcContent.substring(srcContent.indexOf('.') + 1)))) {
                    if (zentaoClient instanceof ZentaoGetClient) {
                        path = zentaoClient.getBaseUrl() + "/index.php?m=file&f=read&fileID=" + srcContent;
                    } else {
                        // 禅道开源版
                        path = zentaoClient.getBaseUrl() + "/file-read-" + srcContent;
                    }
                } else {
                    return result;
                }
            } else {
                name = name.replaceAll("&amp;", "&");
                path = zentaoClient.getBaseUrl() + path.replaceAll("&amp;", "&");
            }
            path = "/resource/md/get/url?url=" + URLEncoder.encode(path, StandardCharsets.UTF_8);
            // 图片与描述信息之间需换行，否则无法预览图片
            result = "\n\n![" + name + "](" + path + ")";
        }

        return result;
    }

    private String getMatcherResultForImg(String regex, String targetStr) {
        String result = "";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(targetStr);
        while (matcher.find()) {
            result = matcher.group(1);
        }

        return result;
    }

    @Override
    public Boolean checkProjectExist(String relateId) {
        return zentaoClient.checkProjectExist(relateId);
    }
}
