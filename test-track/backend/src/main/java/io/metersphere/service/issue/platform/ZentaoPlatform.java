package io.metersphere.service.issue.platform;

import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.constants.ZentaoIssuePlatformStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.xpack.track.dto.AttachmentSyncType;
import io.metersphere.constants.AttachmentType;
import io.metersphere.dto.*;
import io.metersphere.xpack.track.dto.AttachmentRequest;
import io.metersphere.xpack.track.dto.DemandDTO;
import io.metersphere.xpack.track.dto.IssuesDao;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import io.metersphere.service.issue.client.ZentaoClient;
import io.metersphere.service.issue.client.ZentaoGetClient;
import io.metersphere.xpack.track.dto.PlatformUser;

import io.metersphere.service.issue.domain.zentao.AddIssueResponse;
import io.metersphere.service.issue.domain.zentao.GetIssueResponse;
import io.metersphere.service.issue.domain.zentao.ZentaoBuild;
import io.metersphere.service.issue.domain.zentao.ZentaoConfig;
import io.metersphere.xpack.track.dto.PlatformStatusDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ZentaoPlatform extends AbstractIssuePlatform {
    protected final ZentaoClient zentaoClient;

    protected final String[] imgArray = {
            "bmp", "jpg", "png", "tif", "gif", "jpeg"
    };

    // xpack 反射调用
    public ZentaoClient getZentaoClient() {
        return zentaoClient;
    }

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

    public IssuesDao getZentaoAssignedAndBuilds(IssuesDao issue) {
        Map zentaoIssue = (Map) zentaoClient.getBugById(issue.getPlatformId());
        String assignedTo = zentaoIssue.get("assignedTo").toString();
        String openedBuild = zentaoIssue.get("openedBuild").toString();
        List<String> zentaoBuilds = new ArrayList<>();
        if (Strings.isNotBlank(openedBuild)) {
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
            Map obj = JSON.parseMap(body);

            LogUtil.info("project story: " + key + obj);

            if (obj != null) {
                String data = obj.get("data").toString();
                if (StringUtils.isBlank(data)) {
                    return list;
                }
                // 兼容处理11.5版本格式 [{obj},{obj}]
                if (data.charAt(0) == '[') {
                    List array = (List) obj.get("data");
                    for (int i = 0; i < array.size(); i++) {
                        Map o = (Map) array.get(i);
                        DemandDTO demandDTO = new DemandDTO();
                        demandDTO.setId(o.get("id").toString());
                        demandDTO.setName(o.get("title").toString());
                        demandDTO.setPlatform(key);
                        list.add(demandDTO);
                    }
                }
                // {"5": {"children": {"51": {}}}, "6": {}}
                else if (data.startsWith("{\"")) {
                    Map<String, Map<String, String>> dataMap = JSON.parseMap(data);
                    Collection<Map<String, String>> values = dataMap.values();
                    values.forEach(v -> {
                        Map jsonObject = JSON.parseMap(JSON.toJSONString(v));
                        DemandDTO demandDTO = new DemandDTO();
                        demandDTO.setId(jsonObject.get("id").toString());
                        demandDTO.setName(jsonObject.get("title").toString());
                        demandDTO.setPlatform(key);
                        list.add(demandDTO);
                        if (jsonObject.get("children") != null) {
                            LinkedHashMap<String, Map<String, String>> children = (LinkedHashMap<String, Map<String, String>>) jsonObject.get("children");
                            Collection<Map<String, String>> childrenMap = children.values();
                            childrenMap.forEach(ch -> {
                                DemandDTO dto = new DemandDTO();
                                dto.setId(ch.get("id"));
                                dto.setName(ch.get("title"));
                                dto.setPlatform(key);
                                list.add(dto);
                            });
                        }
                    });
                }
                // 处理格式 {{"id": {obj}},{"id",{obj}}}
                else if (data.charAt(0) == '{') {
                    Map dataObject = (Map) obj.get("data");
                    String s = JSON.toJSONString(dataObject);
                    Map<String, Object> map = JSON.parseMap(s);
                    Collection<Object> values = map.values();
                    values.forEach(v -> {
                        Map jsonObject = JSON.parseMap(JSON.toJSONString(v));
                        DemandDTO demandDTO = new DemandDTO();
                        demandDTO.setId(jsonObject.get("id").toString());
                        demandDTO.setName(jsonObject.get("title").toString());
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

    public IssuesWithBLOBs getUpdateIssues(Map bug) {
        return getUpdateIssues(null, bug);
    }

    /**
     * 更新缺陷数据
     *
     * @param issue 待更新缺陷数据
     * @param bug   平台缺陷数据
     * @return
     */
    public IssuesWithBLOBs getUpdateIssues(IssuesWithBLOBs issue, Map bug) {

        GetIssueResponse.Issue bugObj = JSON.parseObject(JSON.toJSONString(bug), GetIssueResponse.Issue.class);
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
        if (StringUtils.equals(bugObj.getDeleted(), "1")) {
            issue.setPlatformStatus(IssuesStatus.DELETE.toString());
            issuesMapper.updateByPrimaryKeySelective(issue);
        }
        issue.setTitle(bugObj.getTitle());
        issue.setDescription(steps);
        issue.setReporter(bugObj.getOpenedBy());
        issue.setPlatform(key);
        try {
            String openedDate = bug.get("openedDate").toString();
            String lastEditedDate = bug.get("lastEditedDate").toString();
            if (StringUtils.isNotBlank(openedDate) && !openedDate.startsWith("0000-00-00"))
                issue.setCreateTime(DateUtils.getTime(openedDate).getTime());
            if (StringUtils.isNotBlank(lastEditedDate) && !lastEditedDate.startsWith("0000-00-00"))
                issue.setUpdateTime(DateUtils.getTime(lastEditedDate).getTime());
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
            MSException.throwException("请确认该Zentao账号是否开启超级model调用接口权限");
        }

        // 如果是复制新增, 同步MS附件到Zentao
        if (StringUtils.isNotEmpty(issuesRequest.getCopyIssueId())) {
            AttachmentRequest request = new AttachmentRequest();
            request.setBelongId(issuesRequest.getCopyIssueId());
            request.setBelongType(AttachmentType.ISSUE.type());
            List<String> attachmentIds = attachmentService.getAttachmentIdsByParam(request);
            if (CollectionUtils.isNotEmpty(attachmentIds)) {
                attachmentIds.forEach(attachmentId -> {
                    FileAttachmentMetadata fileAttachmentMetadata = attachmentService.getFileAttachmentMetadataByFileId(attachmentId);
                    File file = new File(fileAttachmentMetadata.getFilePath() + File.separator + fileAttachmentMetadata.getName());
                    zentaoClient.uploadAttachment("bug", issuesRequest.getPlatformId(), file);
                });
            }
        }

        return issues;
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        setUserConfig();
        MultiValueMap<String, Object> param = buildUpdateParam(request);
        if (request.getTransitions() != null) {
            request.setPlatformStatus(request.getTransitions().getValue());
        }
        handleIssueUpdate(request);
        this.handleZentaoBugStatus(param);
        zentaoClient.updateIssue(request.getPlatformId(), param);
    }

    private void handleZentaoBugStatus(MultiValueMap<String, Object> param) {
        if (!param.containsKey("status")) {
            return;
        }
        List<Object> status = param.get("status");
        if (CollectionUtils.isEmpty(status)) {
            return;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String str = (String) status.get(0);
            if (StringUtils.equals(str, "resolved")) {
                param.add("resolvedDate", format.format(new Date()));
            } else if (StringUtils.equals(str, "closed")) {
                param.add("closedDate", format.format(new Date()));
                if (!param.containsKey("resolution")) {
                    // 解决方案默认为已解决
                    param.add("resolution", "fixed");
                }
            }
        } catch (Exception e) {
            //
        }
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
        if (issuesRequest.getTransitions() != null) {
            paramMap.add("status", issuesRequest.getTransitions().getValue());
        }

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
        String session = zentaoClient.login();
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        String getUser = zentaoClient.requestUrl.getUserGet();
        ResponseEntity<String> responseEntity = restTemplate.exchange(getUser + session,
                HttpMethod.GET, requestEntity, String.class);
        String body = responseEntity.getBody();
        Map obj = JSON.parseMap(body);

        LogUtil.info("zentao user " + obj);

        List data = JSON.parseArray(obj.get("data").toString());

        List<PlatformUser> users = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Map o = (Map) data.get(i);
            PlatformUser platformUser = new PlatformUser();
            String account = o.get("account").toString();
            String username = o.get("realname").toString();
            platformUser.setName(username);
            platformUser.setUser(account);
            users.add(platformUser);
        }
        return users;
    }

    @Override
    public void syncIssues(Project project, List<IssuesDao> issues) {
        HashMap<String, List<CustomFieldResourceDTO>> customFieldMap = new HashMap<>();

        issues.forEach(item -> {
            IssuesWithBLOBs issue = issuesMapper.selectByPrimaryKey(item.getId());
            Map bug = zentaoClient.getBugById(item.getPlatformId());
            issue = getUpdateIssues(issue, bug);
            customFieldMap.put(item.getId(), baseCustomFieldService.getCustomFieldResourceDTO(issue.getCustomFields()));
            issue.setId(item.getId());
            issuesMapper.updateByPrimaryKeySelective(issue);
            syncZentaoIssueAttachments(issue);
        });
        customFieldIssuesService.batchEditFields(customFieldMap);
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
        paramMap.add("files", resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        try {
            String fileUpload = zentaoClient.requestUrl.getFileUpload();
            ResponseEntity<String> responseEntity = restTemplate.exchange(fileUpload, HttpMethod.POST, requestEntity,
                    String.class, null, session);
            String body = responseEntity.getBody();
            Map obj = JSON.parseMap(body);
            Map data = (Map) JSON.parseObject(obj.get("data").toString());
            Set<String> set = data.keySet();
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
            if (originSubUrl.contains("/url?url=")) {
                String path = URLDecoder.decode(originSubUrl, StandardCharsets.UTF_8);
                String fileName;
                if (path.indexOf("fileID") > 0) {
                    fileName = path.substring(path.indexOf("fileID") + 7);
                } else {
                    fileName = path.substring(path.indexOf("file-read-") + 10);
                }
                zentaoSteps = zentaoSteps.replaceAll(Pattern.quote(originSubUrl), fileName);
            } else {
                String fileName = originSubUrl.substring(10);
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
        }
        // image link
        String netImgRegex = "!\\[(.*?)]\\((http.*?)\\)";
        return zentaoSteps.replaceAll(netImgRegex, "<img src=\"$2\" alt=\"$1\"/>");
    }

    private String zentao2MsDescription(String ztDescription) {
        String imgRegex = "<img src.*?/>";
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
            if (!path.startsWith("http")) {
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
                    try {
                        URI uri = new URI(zentaoClient.getBaseUrl());
                        path = uri.getScheme() + "://" + uri.getHost() + path.replaceAll("&amp;", "&");
                    } catch (URISyntaxException e) {
                        path = zentaoClient.getBaseUrl() + path.replaceAll("&amp;", "&");
                        LogUtil.error(e);
                    }
                }
                path = "/resource/md/get/url?url=" + URLEncoder.encode(path, StandardCharsets.UTF_8);
            }
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

    @Override
    public void syncIssuesAttachment(IssuesUpdateRequest issuesRequest, File file, AttachmentSyncType syncType) {
        if ("upload".equals(syncType.syncOperateType())) {
            zentaoClient.uploadAttachment("bug", issuesRequest.getPlatformId(), file);
        } else if ("delete".equals(syncType.syncOperateType())) {
            Map bugInfo = zentaoClient.getBugById(issuesRequest.getPlatformId());
            Map<String, Object> zenFiles = (Map) bugInfo.get("files");
            for (String fileId : zenFiles.keySet()) {
                Map fileInfo = (Map) zenFiles.get(fileId);
                if (file.getName().equals(fileInfo.get("title"))) {
                    zentaoClient.deleteAttachment(fileId);
                    break;
                }
            }
        }
    }

    public void syncZentaoIssueAttachments(IssuesWithBLOBs issue) {
        List<String> znetaoAttachmentsName = new ArrayList<String>();
        AttachmentRequest request = new AttachmentRequest();
        request.setBelongType(AttachmentType.ISSUE.type());
        request.setBelongId(issue.getId());
        List<FileAttachmentMetadata> allMsAttachments = attachmentService.listMetadata(request);
        List<String> msAttachmentsName = allMsAttachments.stream().map(FileAttachmentMetadata::getName).collect(Collectors.toList());
        Map bugInfo = zentaoClient.getBugById(issue.getPlatformId());
        Object files = bugInfo.get("files");
        Map<String, Object> zenFiles;
        if (files instanceof List && ((List) files).size() == 0) {
            zenFiles = null;
        } else {
            zenFiles = (Map) files;
        }
        // 同步禅道中新的附件
        if (zenFiles != null) {
            for (String fileId : zenFiles.keySet()) {
                Map fileInfo = (Map) zenFiles.get(fileId);
                String filename = fileInfo.get("title").toString();
                znetaoAttachmentsName.add(filename);
                if (!msAttachmentsName.contains(filename)) {
                    try {
                        byte[] bytes = zentaoClient.getAttachmentBytes(fileId);
                        FileAttachmentMetadata fileAttachmentMetadata = attachmentService.saveAttachmentByBytes(bytes, AttachmentType.ISSUE.type(), issue.getId(), filename);
                        AttachmentModuleRelation attachmentModuleRelation = new AttachmentModuleRelation();
                        attachmentModuleRelation.setAttachmentId(fileAttachmentMetadata.getId());
                        attachmentModuleRelation.setRelationId(issue.getId());
                        attachmentModuleRelation.setRelationType(AttachmentType.ISSUE.type());
                        attachmentModuleRelationMapper.insert(attachmentModuleRelation);
                    } catch (Exception e) {
                        LogUtil.error(e);
                    }
                }
            }
        }

        // 删除禅道中不存在的附件
        if (CollectionUtils.isNotEmpty(allMsAttachments)) {
            List<FileAttachmentMetadata> deleteMsAttachments = allMsAttachments.stream()
                    .filter(msAttachment -> !znetaoAttachmentsName.contains(msAttachment.getName())).collect(Collectors.toList());
            deleteMsAttachments.forEach(fileAttachmentMetadata -> {
                List<String> ids = List.of(fileAttachmentMetadata.getId());
                AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
                example.createCriteria().andAttachmentIdIn(ids).andRelationTypeEqualTo(AttachmentType.ISSUE.type());
                // 删除MS附件及关联数据
                attachmentService.deleteAttachmentByIds(ids);
                attachmentService.deleteFileAttachmentByIds(ids);
                attachmentModuleRelationMapper.deleteByExample(example);
            });
        }
    }


    @Override
    public List<PlatformStatusDTO> getTransitions(String issueKey) {
        List<PlatformStatusDTO> platformStatusDTOS = new ArrayList<>();
        for (ZentaoIssuePlatformStatus status : ZentaoIssuePlatformStatus.values()) {
            PlatformStatusDTO platformStatusDTO = new PlatformStatusDTO();
            platformStatusDTO.setValue(status.name());
            platformStatusDTO.setLabel(status.getName());

            platformStatusDTOS.add(platformStatusDTO);
        }
        return platformStatusDTOS;
    }
}
