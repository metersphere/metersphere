package io.metersphere.service.issue.platform;

import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.xpack.track.dto.AttachmentSyncType;
import io.metersphere.constants.AttachmentType;
import io.metersphere.dto.*;
import io.metersphere.request.attachment.AttachmentRequest;
import io.metersphere.xpack.track.dto.*;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import io.metersphere.service.issue.client.JiraClientV2;
import io.metersphere.service.IssuesService;
import io.metersphere.service.issue.domain.ProjectIssueConfig;
import io.metersphere.service.issue.domain.jira.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JiraPlatform extends AbstractIssuePlatform {

    protected JiraClientV2 jiraClientV2;
    protected SimpleDateFormat sdfWithZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public JiraPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
        this.key = IssuesManagePlatform.Jira.name();
        jiraClientV2 = new JiraClientV2();
        setConfig();
    }

    // xpack 反射调用
    public JiraClientV2 getJiraClientV2() {
        return jiraClientV2;
    }
    
    public void setThirdPartTemplate() {
        this.isThirdPartTemplate = true;
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

    public IssuesWithBLOBs getUpdateIssue(JiraIssue jiraIssue) {
        return getUpdateIssue(null, jiraIssue);
    }

    public IssuesWithBLOBs getUpdateIssue(IssuesWithBLOBs issue, JiraIssue jiraIssue) {
        try {
            if (issue == null) {
                issue = new IssuesWithBLOBs();
                issue.setCustomFields(defaultCustomFields);
            } else {
                mergeCustomField(issue, defaultCustomFields);
            }

            Map fields = jiraIssue.getFields();
            String status = getStatus(fields);

            Map<String, String> fileContentMap = getContextMap((List) fields.get("attachment"));

            // 先转换下desc的图片
            String description = dealWithDescription(Optional.ofNullable(fields.get("description")).orElse("").toString(), fileContentMap);
            fields.put("description", description);
            CustomFieldItemDTO descItem = null;
            List<CustomFieldItemDTO> customFieldItems = syncIssueCustomFieldList(issue.getCustomFields(), jiraIssue.getFields());

            // 其他自定义里有富文本框的也转换下图片
            for (CustomFieldItemDTO item : customFieldItems) {
                if (StringUtils.equals("description", item.getId())) {
                    // desc转过了，跳过
                    descItem = item;
                } else {
                    if (StringUtils.equals(CustomFieldType.RICH_TEXT.getValue(), item.getType())) {
                        item.setValue(dealWithDescription((String) item.getValue(), fileContentMap));
                    }
                }
            }

            Map assignee = (Map) fields.get("assignee");
            issue.setTitle(fields.get("summary").toString());
            issue.setLastmodify(assignee == null ? "" : assignee.get("displayName").toString());
            issue.setDescription(description);
            issue.setPlatformStatus(status);
            issue.setPlatform(key);
            issue.setCustomFields(JSON.toJSONString(customFieldItems));
            try {
                issue.setCreateTime(sdfWithZone.parse((String) fields.get("created")).getTime());
                issue.setUpdateTime(sdfWithZone.parse((String) fields.get("updated")).getTime());
            } catch (Exception e) {
                LogUtil.error(e);
            }
            return issue;
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e);
            return null;
        }
    }

    private String dealWithDescription(String description, Map<String, String> fileContentMap) {
        if (StringUtils.isBlank(description)) {
            return description;
        }

        description = description.replaceAll("!image", "\n!image");
        String[] splitStrs = description.split("\\n");
        for (int j = 0; j < splitStrs.length; j++) {
            String splitStr = splitStrs[j];
            if (StringUtils.isNotEmpty(splitStr)) {
                List<String> keys = fileContentMap.keySet().stream().filter(key -> splitStr.contains(key)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(keys)) {
                    description = description.replace(splitStr, fileContentMap.get(keys.get(0)));
                    fileContentMap.remove(keys.get(0));
                } else {
                    if (splitStr.contains("MS附件：")) {
                        // 解析标签内容
                        String name = getHyperLinkPathForImg("\\!\\[(.*?)\\]", StringEscapeUtils.unescapeJava(splitStr));
                        String path = getHyperLinkPathForImg("\\|(.*?)\\)", splitStr);
                        path = "/resource/md/get/url?platform=Jira&url=" + URLEncoder.encode(path, StandardCharsets.UTF_8);

                        // 解析标签内容为图片超链接格式，进行替换
                        description = description.replace(splitStr, "\n\n![" + name + "](" + path + ")");
                    }
                    description = description.replace(splitStr, StringEscapeUtils.unescapeJava(splitStr.replace("MS附件：", "")));
                }
            }
        }
        return description;
    }

    private String appendMoreImage(String description, Map<String, String> fileContentMap) {
        for (String key: fileContentMap.keySet()) {
            // 同步jira上传的附件
            description += StringUtils.LF + fileContentMap.get(key);
        }
        return description;
    }

    private Map<String, String> getContextMap(List attachments) {
        // 附件处理
        Map<String, String> fileContentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(attachments)) {
            for (int i = 0; i < attachments.size(); i++) {
                Map attachment = (Map) attachments.get(i);
                String filename = attachment.get("filename").toString();
                String content = attachment.get("content").toString();
                content = "/resource/md/get/url?platform=Jira&url=" + URLEncoder.encode(content, StandardCharsets.UTF_8);

                if (StringUtils.contains(attachment.get("mimeType").toString(), "image")) {
                    String contentUrl = "![" + filename + "](" + content + ")";
                    fileContentMap.put(filename, contentUrl);
                } else {
                    String contentUrl = "附件[" + filename + "]下载地址:" + content;
                    fileContentMap.put(filename, contentUrl);
                }
            }
        }
        return fileContentMap;
    }

    private String getStatus(Map fields) {
        Map statusObj = (Map) fields.get("status");
        if (statusObj != null) {
            Map statusCategory = (Map) statusObj.get("statusCategory");
            return statusObj.get("name").toString() == null ? statusCategory.get("name").toString() : statusObj.get("name").toString();
        }
        return "";
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        List<DemandDTO> list = new ArrayList<>();
        Project project = getProject();
        int maxResults = 50, startAt = 0;
        List demands;
        do {
            demands = jiraClientV2.getDemands(project.getJiraKey(), getStoryType(project.getIssueConfig()), startAt, maxResults);
            for (int i = 0; i < demands.size(); i++) {
                Map o = (Map) demands.get(i);
                String issueKey = o.get("key").toString();
                Map fields = (Map) o.get("fields");
                String summary = fields.get("summary").toString();
                DemandDTO demandDTO = new DemandDTO();
                demandDTO.setName(summary);
                demandDTO.setId(issueKey);
                demandDTO.setPlatform(key);
                list.add(demandDTO);
            }
            startAt += maxResults;
        } while (demands.size() >= maxResults);
        return list;
    }

    private void validateConfig(JiraConfig config) {
        jiraClientV2.setConfig(config);
        if (config == null) {
            MSException.throwException("jira config is null");
        }
    }

    public List<JiraIssueType> getIssueTypes(String jiraKey) {
        return jiraClientV2.getIssueType(jiraKey);
    }

    @Override
    public IssuesWithBLOBs addIssue(IssuesUpdateRequest issuesRequest) {

        setUserConfig();
        Project project = getProject();

        Map addJiraIssueParam = buildUpdateParam(issuesRequest, getIssueType(project.getIssueConfig()), project.getJiraKey());
        JiraAddIssueResponse result = jiraClientV2.addIssue(JSON.toJSONString(addJiraIssueParam));
        JiraIssue issues = jiraClientV2.getIssues(result.getId());

        // 上传富文本中的图片作为附件
        List<File> imageFiles = getImageFiles(issuesRequest);
        imageFiles.forEach(img -> jiraClientV2.uploadAttachment(result.getKey(), img));

        String status = getStatus(issues.getFields());
        issuesRequest.setPlatformStatus(status);

        issuesRequest.setPlatformId(result.getKey());
        issuesRequest.setId(UUID.randomUUID().toString());

        // 插入缺陷表
        IssuesWithBLOBs res = insertIssues(issuesRequest);

        // 用例与第三方缺陷平台中的缺陷关联
        handleTestCaseIssues(issuesRequest);

        // 如果是复制新增, 同步MS附件到Jira
        if (StringUtils.isNotEmpty(issuesRequest.getCopyIssueId())) {
            AttachmentRequest request = new AttachmentRequest();
            request.setBelongId(issuesRequest.getCopyIssueId());
            request.setBelongType(AttachmentType.ISSUE.type());
            List<String> attachmentIds = attachmentService.getAttachmentIdsByParam(request);
            if (CollectionUtils.isNotEmpty(attachmentIds)) {
                attachmentIds.forEach(attachmentId -> {
                    FileAttachmentMetadata fileAttachmentMetadata = attachmentService.getFileAttachmentMetadataByFileId(attachmentId);
                    File file = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
                    jiraClientV2.uploadAttachment(result.getKey(), file);
                });
            }
        }

        return res;
    }

    public Project getProject() {
        return super.getProject(this.projectId, Project::getJiraKey);
    }

    private List<File> getImageFiles(IssuesUpdateRequest issuesRequest) {
        List<File> files = new ArrayList<>();
        List<CustomFieldItemDTO> customFields = issuesRequest.getRequestFields();
        customFields.forEach(item -> {
            String fieldName = item.getCustomData();
            if (StringUtils.isNotBlank(fieldName)) {
                if (item.getValue() != null) {
                    if (StringUtils.isNotBlank(item.getType())) {
                        if (StringUtils.equalsAny(item.getType(),  "richText")) {
                            files.addAll(getImageFiles(item.getValue().toString()));
                        }
                    }
                }
            }
        });
        return files;
    }

    /**
     * 参数比较特殊，需要特别处理
     * @param fields
     */
    private void setSpecialParam(Map fields) {
        Project project = getProject();
        try {
            Map<String, JiraCreateMetadataResponse.Field> createMetadata = jiraClientV2.getCreateMetadata(project.getJiraKey(), getIssueType(project.getIssueConfig()));
            List<JiraUser> userOptions = jiraClientV2.getAssignableUser(project.getJiraKey());

            Boolean isUserKey = false;
            if (CollectionUtils.isNotEmpty(userOptions) && StringUtils.isBlank(userOptions.get(0).getAccountId())) {
                isUserKey = true;
            }

            for (String key : createMetadata.keySet()) {
                JiraCreateMetadataResponse.Field item = createMetadata.get(key);
                JiraCreateMetadataResponse.Schema schema = item.getSchema();
                if (schema == null) {
                    continue;
                }
                if (schema.getCustom() != null && schema.getCustom().endsWith("sprint")) {
                    try {
                        Map field = (Map) fields.get(key);
                        // sprint 传参数比较特殊，需要要传数值
                        fields.put(key, (Integer) field.get("id"));
                    } catch (Exception e) {}
                }
                if (isUserKey) {
                    if (schema.getType() != null && schema.getType().endsWith("user")) {
                        Map field = (Map) fields.get(key);
                        // 如果不是用户ID，则是用户的key，参数调整为key
                        Map newField = new LinkedHashMap<>();
                        newField.put("name", field.get("id").toString());
                        fields.put(key, newField);
                    }
                    if (schema.getCustom() != null && schema.getCustom().endsWith("multiuserpicker")) { // 多选用户列表
                        try {
                            List<Map> userItems = (List) fields.get(key);
                            userItems.forEach(i -> i.put("name", i.get("id")));
                        } catch (Exception e) {LogUtil.error(e);}
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private Map<String, Object> buildUpdateParam(IssuesUpdateRequest issuesRequest, String issuetypeStr, String jiraKey) {
        issuesRequest.setPlatform(key);
        Map fields = new LinkedHashMap<>();
        Map project = new LinkedHashMap<>();

        String desc = "";
        // 附件描述信息处理
        if (StringUtils.isNotBlank(issuesRequest.getDescription())) {
            desc = dealWithImage(issuesRequest.getDescription());
        }


        fields.put("project", project);
        project.put("key", jiraKey);

        Map issuetype = new LinkedHashMap<>();
        issuetype.put("id", issuetypeStr);
        fields.put("issuetype", issuetype);

        Map addJiraIssueParam = new LinkedHashMap();
        addJiraIssueParam.put("fields", fields);

        if (issuesRequest.isThirdPartPlatform()) {
            parseCustomFiled(issuesRequest, fields);
            issuesRequest.setTitle(fields.get("summary").toString());
        } else {
            fields.put("summary", issuesRequest.getTitle());
            fields.put("description", desc);
            // 添加后，解析图片会用到
            issuesRequest.getRequestFields().add(getRichTextCustomField("description", desc));
            parseCustomFiled(issuesRequest, fields);
        }
        setSpecialParam(fields);

        return addJiraIssueParam;
    }

    private CustomFieldItemDTO getRichTextCustomField(String name, String value) {
        CustomFieldItemDTO customField = new CustomFieldItemDTO();
        customField.setId(name);
        customField.setType(CustomFieldType.RICH_TEXT.getValue());
        customField.setCustomData(name);
        customField.setValue(value);
        return customField;
    }

    private String dealWithImage(String description) {
        String regex = "(\\!\\[.*?\\]\\((.*?)\\))";
        Matcher matcher = Pattern.compile(regex).matcher(description);

        try {
            while (matcher.find()) {
                if (StringUtils.isNotEmpty(matcher.group())) {
                    // img标签内容
                    String imgPath = matcher.group();
                    // 解析标签内容为图片超链接格式，进行替换
                    description = description.replace(imgPath, "\nMS附件：" + imgPath);
                }
            }
        } catch (Exception exception) {
        }

        return description;
    }

    private String getHyperLinkPathForImg(String regex, String targetStr) {
        String result = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(targetStr);

        try {
            while (matcher.find()) {
                String url = matcher.group(1);
                result = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
            }
        } catch (Exception exception) {
            return targetStr;
        }

        return result;
    }

    private void parseCustomFiled(IssuesUpdateRequest issuesRequest, Map fields) {
        List<CustomFieldItemDTO> customFields = issuesRequest.getRequestFields();

        customFields.forEach(item -> {
            String fieldName = item.getCustomData();
            String name = item.getName();
            if (StringUtils.isNotBlank(fieldName)) {
                if (ObjectUtils.isNotEmpty(item.getValue())) {
                    if (StringUtils.isNotBlank(item.getType())) {
                        if (StringUtils.equalsAny(item.getType(), "select", "radio", "member")) {
                            if (StringUtils.equalsAnyIgnoreCase(name, "PML", "PMLinkTest", "PMLink")) {
                                fields.put(fieldName, item.getValue());
                            } else {
                                Map param = new LinkedHashMap<>();
                                if (fieldName.equals("assignee") || fieldName.equals("reporter")) {
                                    if (issuesRequest.isThirdPartPlatform()) {
                                        param.put("id", item.getValue());
                                    } else {
                                        param.put("name", item.getValue());
                                    }
                                } else {
                                    param.put("id", item.getValue());
                                }
                                fields.put(fieldName, param);
                            }
                        } else if (StringUtils.equalsAny(item.getType(),  "multipleSelect", "checkbox", "multipleMember")) {
                            List attrs = new ArrayList();
                            if (item.getValue() != null) {
                                List values = JSON.parseArray((String) item.getValue());
                                values.forEach(v -> {
                                    Map param = new LinkedHashMap<>();
                                    param.put("id", v);
                                    attrs.add(param);
                                });
                            }
                            fields.put(fieldName, attrs);
                        } else if (StringUtils.equalsAny(item.getType(),  "cascadingSelect")) {
                            if (item.getValue() != null) {
                                Map attr = new LinkedHashMap<>();
                                List values = JSON.parseArray((String) item.getValue());
                                if (CollectionUtils.isNotEmpty(values)) {
                                    if (values.size() > 0) {
                                        attr.put("id", values.get(0));
                                    }
                                    if (values.size() > 1) {
                                        Map param = new LinkedHashMap<>();
                                        param.put("id", values.get(1));
                                        attr.put("child", param);
                                    }
                                } else {
                                    attr.put("id", item.getValue());
                                }
                                fields.put(fieldName, attr);
                            }
                        } else if (StringUtils.equalsAny(item.getType(), "richText")) {
                            fields.put(fieldName, parseRichTextImageUrlToJira(item.getValue().toString()));
                            if (fieldName.equals("description")) {
                                issuesRequest.setDescription(item.getValue().toString());
                            }
                        } else {
                            fields.put(fieldName, item.getValue());
                        }
                    }

                }
            }
        });
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        setUserConfig();
        Project project = getProject();
        Map param = buildUpdateParam(request, getIssueType(project.getIssueConfig()), project.getJiraKey());
        jiraClientV2.updateIssue(request.getPlatformId(), JSON.toJSONString(param));

        // 同步Jira富文本有关的附件
        syncJiraRichTextAttachment(request);

        if (request.getTransitions() != null) {
            try {
                List<JiraTransitionsResponse.Transitions> transitions = jiraClientV2.getTransitions(request.getPlatformId());
                transitions.forEach(transition -> {
                    if (Objects.equals(request.getPlatformStatus(), transition.getTo().getName())) {
                        jiraClientV2.setTransitions(request.getPlatformId(), transition);
                    }
                });
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        handleIssueUpdate(request);
    }

    @Override
    public void deleteIssue(String id) {
        IssuesWithBLOBs issuesWithBLOBs = issuesMapper.selectByPrimaryKey(id);
        super.deleteIssue(id);
        jiraClientV2.deleteIssue(issuesWithBLOBs.getPlatformId());
    }

    @Override
    public void testAuth() {
        jiraClientV2.auth();
    }

    @Override
    public void userAuth(UserDTO.PlatformInfo userInfo) {
        setUserConfig(userInfo);
        jiraClientV2.auth();
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
        return null;
    }

    @Override
    public void syncIssues(Project project, List<IssuesDao> issues) {
        super.isThirdPartTemplate = isThirdPartTemplate();
        HashMap<String, List<CustomFieldResourceDTO>> customFieldMap = new HashMap<>();

        prepareSync(project);

        issues.forEach(item -> {
            try {
                JiraIssue jiraIssue = jiraClientV2.getIssues(item.getPlatformId());
                getUpdateIssue(item, jiraIssue);
                String customFields = item.getCustomFields();
                // 把自定义字段存入新表
                List<CustomFieldResourceDTO> customFieldResource = baseCustomFieldService.getCustomFieldResourceDTO(customFields);
                customFieldMap.put(item.getId(), customFieldResource);
                issuesMapper.updateByPrimaryKeySelective(item);
                // 同步第三方平台附件
                syncJiraIssueAttachments(item, jiraIssue);
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode() == 404) {
                    // 标记成删除
                    item.setPlatformStatus(IssuesStatus.DELETE.toString());
                    issuesMapper.deleteByPrimaryKey(item.getId());
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
        customFieldIssuesService.batchEditFields(customFieldMap);
    }

    public void prepareSync(Project project) {
        IssuesService issuesService = CommonBeanFactory.getBean(IssuesService.class);
        if (project.getThirdPartTemplate()) {
            super.defaultCustomFields =  issuesService.getCustomFieldsValuesString(getThirdPartTemplate().getCustomFields());
        }
    }

    @Override
    public String getProjectId(String projectId) {
        return getProjectId(projectId, Project::getJiraKey);
    }

    public JiraConfig getConfig() {
        return getConfig(key, JiraConfig.class);
    }

    public JiraConfig setConfig() {
        JiraConfig config = getConfig();
        validateConfig(config);
        jiraClientV2.setConfig(config);
        return config;
    }

    public JiraConfig setUserConfig(UserDTO.PlatformInfo userInfo) {
        JiraConfig config = getConfig();
        if (userInfo != null && StringUtils.isNotBlank(userInfo.getJiraAccount())
                && StringUtils.isNotBlank(userInfo.getJiraPassword())) {
            config.setAccount(userInfo.getJiraAccount());
            config.setPassword(userInfo.getJiraPassword());
        }
        validateConfig(config);
        jiraClientV2.setConfig(config);
        return config;
    }

    public JiraConfig setUserConfig() {
        return setUserConfig(getUserPlatInfo(this.workspaceId));
    }

    @Override
    public List<PlatformStatusDTO> getTransitions(String issueKey) {
        List<PlatformStatusDTO> platformStatusDTOS = new ArrayList<>();
        List<JiraTransitionsResponse.Transitions> transitions = jiraClientV2.getTransitions(issueKey);
        if (CollectionUtils.isNotEmpty(transitions)) {
            transitions.forEach(item -> {
                PlatformStatusDTO platformStatusDTO = new PlatformStatusDTO();
                platformStatusDTO.setLabel(item.getTo().getName());
                platformStatusDTO.setValue(item.getTo().getName());
                platformStatusDTOS.add(platformStatusDTO);
            });
        }
        return platformStatusDTOS;
    }

    public IssueTemplateDao getThirdPartTemplate() {
        setUserConfig();
        Set<String> ignoreSet = new HashSet() {{
            add("timetracking");
            add("attachment");
        }};
        String projectKey = getProjectId(this.projectId);
        Project project = getProject();

        Map<String, JiraCreateMetadataResponse.Field> createMetadata = jiraClientV2.getCreateMetadata(projectKey, getIssueType(project.getIssueConfig()));

        String userOptions = getUserOptions(projectKey);
        List<CustomFieldDao> fields = new ArrayList<>();
        char filedKey = 'A';
        for (String name : createMetadata.keySet()) {
            JiraCreateMetadataResponse.Field item = createMetadata.get(name);
            if (ignoreSet.contains(name)) {
                continue;  // timetracking, attachment todo
            }
            JiraCreateMetadataResponse.Schema schema = item.getSchema();
            CustomFieldDao customFieldDao = new CustomFieldDao();
            customFieldDao.setKey(String.valueOf(filedKey++));
            customFieldDao.setId(name);
            customFieldDao.setCustomData(name);
            customFieldDao.setName(item.getName());
            customFieldDao.setRequired(item.isRequired());
            setCustomFiledType(schema, customFieldDao, userOptions);
            setCustomFiledDefaultValue(customFieldDao, item);
            List options = getAllowedValuesOptions(item.getAllowedValues());
            if (options != null)
                customFieldDao.setOptions(JSON.toJSONString(options));
            fields.add(customFieldDao);
        }

        fields = fields.stream().filter(i -> StringUtils.isNotBlank(i.getType()))
                .collect(Collectors.toList());

        // 按类型排序，富文本排最后，input 排最前面，summary 排第一个
        fields.sort((a, b) -> {
            if (a.getType().equals(CustomFieldType.RICH_TEXT.getValue())) return 1;
            if (b.getType().equals(CustomFieldType.RICH_TEXT.getValue())) return -1;
            if (a.getId().equals("summary")) return -1;
            if (b.getId().equals("summary")) return 1;
            if (a.getType().equals(CustomFieldType.INPUT.getValue())) return -1;
            if (b.getType().equals(CustomFieldType.INPUT.getValue())) return 1;
            return a.getType().compareTo(b.getType());
        });
        IssueTemplateDao issueTemplateDao = new IssueTemplateDao();
        issueTemplateDao.setCustomFields(fields);
        issueTemplateDao.setPlatform(this.key);
        return issueTemplateDao;
    }

    public String getIssueType(String configStr) {
        ProjectIssueConfig projectConfig = super.getProjectConfig(configStr);
        String jiraIssueType = projectConfig.getJiraIssueTypeId();
        if (StringUtils.isBlank(jiraIssueType)) {
            MSException.throwException("请在项目中配置 Jira 问题类型！");
        }
        return jiraIssueType;
    }

    public String getStoryType(String configStr) {
        ProjectIssueConfig projectConfig = super.getProjectConfig(configStr);
        String jiraStoryType = projectConfig.getJiraStoryTypeId();
        if (StringUtils.isBlank(jiraStoryType)) {
            MSException.throwException("请在项目中配置 Jira 需求类型！");
        }
        return jiraStoryType;
    }

    private void setCustomFiledType(JiraCreateMetadataResponse.Schema schema, CustomFieldDao customFieldDao, String userOptions) {
        Map<String, String> fieldTypeMap = new HashMap() {{
            put("summary", CustomFieldType.INPUT.getValue());
            put("description", CustomFieldType.RICH_TEXT.getValue());
            put("components", CustomFieldType.MULTIPLE_SELECT.getValue());
            put("fixVersions", CustomFieldType.MULTIPLE_SELECT.getValue());
            put("versions", CustomFieldType.MULTIPLE_SELECT.getValue());
            put("priority", CustomFieldType.SELECT.getValue());
            put("environment", CustomFieldType.RICH_TEXT.getValue());
            put("labels", CustomFieldType.MULTIPLE_INPUT.getValue());
        }};
        String customType = schema.getCustom();
        String value = null;
        if (StringUtils.isNotBlank(customType)) {
            // 自定义字段
            if (customType.contains("multiselect")) {
                value = CustomFieldType.MULTIPLE_SELECT.getValue();
            } else if (customType.contains("cascadingselect")) {
                value = "cascadingSelect";
            } else if (customType.contains("multiuserpicker")) {
                value = CustomFieldType.MULTIPLE_SELECT.getValue();
                customFieldDao.setOptions(userOptions);
            } else if (customType.contains("userpicker")) {
                value = CustomFieldType.SELECT.getValue();
                customFieldDao.setOptions(userOptions);
            } else if (customType.contains("people")) {
                if (StringUtils.isNotBlank(schema.getType()) && StringUtils.equals(schema.getType(), "array")) {
                    value = CustomFieldType.MULTIPLE_SELECT.getValue();
                } else {
                    value = CustomFieldType.SELECT.getValue();
                }
                customFieldDao.setOptions(userOptions);
            } else if (customType.contains("multicheckboxes")) {
                value = CustomFieldType.CHECKBOX.getValue();
                customFieldDao.setDefaultValue(JSON.toJSONString(new ArrayList()));
            } else if (customType.contains("radiobuttons")) {
                value = CustomFieldType.RADIO.getValue();
            } else if (customType.contains("textfield")) {
                value = CustomFieldType.INPUT.getValue();
            } else if (customType.contains("datetime")) {
                value = CustomFieldType.DATETIME.getValue();
            } else if (customType.contains("datepicker")) {
                value = CustomFieldType.DATE.getValue();
            } else if (customType.contains("float")) {
                value = CustomFieldType.FLOAT.getValue();
            } else if (customType.contains("select")) {
                value = CustomFieldType.SELECT.getValue();
            } else if (customType.contains("url")) {
                value = CustomFieldType.INPUT.getValue();
            } else if (customType.contains("textarea")) {
                value = CustomFieldType.TEXTAREA.getValue();
            } else if (customType.contains("labels")) {
                value = CustomFieldType.MULTIPLE_INPUT.getValue();
            } else if (customType.contains("multiversion")) {
                value = CustomFieldType.MULTIPLE_SELECT.getValue();
            } else if (customType.contains("version")) {
                value = CustomFieldType.SELECT.getValue();
            }
        } else {
            // 系统字段
            value = fieldTypeMap.get(customFieldDao.getId());
            String type = schema.getType();
            if ("user".equals(type)) {
                value = CustomFieldType.SELECT.getValue();
                customFieldDao.setOptions(userOptions);
            } else if ("date".equals(type)) {
                value = CustomFieldType.DATE.getValue();
            } else if ("datetime".equals(type)) {
                value = CustomFieldType.DATETIME.getValue();
            }
        }
        customFieldDao.setType(value);
    }

    private void setCustomFiledDefaultValue(CustomFieldDao customFieldDao, JiraCreateMetadataResponse.Field item) {
        if (item.isHasDefaultValue()) {
            Object defaultValue = item.getDefaultValue();
            if (defaultValue != null) {
                Object msDefaultValue;
                if (defaultValue instanceof Map) {
                    msDefaultValue = ((Map) defaultValue).get("id");
                } else if (defaultValue instanceof List) {
                    List defaultList = new ArrayList();
                    ((List) defaultValue).forEach(i -> {
                        if (i instanceof Map) {
                            Map obj = (Map) i;
                            defaultList.add(obj.get("id"));
                        } else {
                            defaultList.add(i);
                        }
                    });

                    msDefaultValue = defaultList;
                } else {
                    if (customFieldDao.getType().equals(CustomFieldType.DATE.getValue())) {
                        if (defaultValue instanceof String) {
                            msDefaultValue = defaultValue;
                        } else {
                            msDefaultValue = Instant.ofEpochMilli((Long) defaultValue).atZone(ZoneId.systemDefault()).toLocalDate().toString();
                        }
                    } else if (customFieldDao.getType().equals(CustomFieldType.DATETIME.getValue())) {
                        if (defaultValue instanceof String) {
                            msDefaultValue = defaultValue;
                        } else {
                            msDefaultValue = LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) defaultValue), ZoneId.systemDefault()).toString();
                        }
                    } else {
                        msDefaultValue = defaultValue;
                    }
                }
                customFieldDao.setDefaultValue(JSON.toJSONString(msDefaultValue));
            }
        }
    }

    private List getAllowedValuesOptions(List<JiraCreateMetadataResponse.AllowedValues> allowedValues) {
        if (allowedValues != null) {
            List options = new ArrayList<>();
            allowedValues.forEach(val -> {
                Map jsonObject = new LinkedHashMap();
                jsonObject.put("value", val.getId());
                if (StringUtils.isNotBlank(val.getName())) {
                    jsonObject.put("text", val.getName());
                } else {
                    jsonObject.put("text", val.getValue());
                }
                List children = getAllowedValuesOptions(val.getChildren());
                if (children != null) {
                    jsonObject.put("children", children);
                }
                options.add(jsonObject);
            });
            return options;
        }
        return null;
    }

    private String getUserOptions(String projectKey) {
        List<JiraUser> userOptions = jiraClientV2.getAssignableUser(projectKey);
        List options = new ArrayList();
        userOptions.forEach(val -> {
            Map jsonObject = new LinkedHashMap<>();
            if (StringUtils.isNotBlank(val.getAccountId())) {
                jsonObject.put("value", val.getAccountId());
            } else {
                jsonObject.put("value", val.getName());
            }
            jsonObject.put("text", val.getDisplayName());
            options.add(jsonObject);
        });
        return JSON.toJSONString(options);
    }

    @Override
    public Boolean checkProjectExist(String relateId) {
        try {
            JiraIssueProject project = jiraClientV2.getProject(relateId);
            if (project != null && StringUtils.isNotBlank(project.getId())) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public ResponseEntity proxyForGet(String url, Class responseEntityClazz) {
        return jiraClientV2.proxyForGet(url, responseEntityClazz);
    }

    @Override
    public void syncIssuesAttachment(IssuesUpdateRequest issuesRequest, File file, AttachmentSyncType syncType) {
        // 同步缺陷MS附件到Jira
        if ("upload".equals(syncType.syncOperateType())) {
            // 上传附件
            jiraClientV2.uploadAttachment(issuesRequest.getPlatformId(), file);
        } else if ("delete".equals(syncType.syncOperateType())) {
            // 删除附件
            JiraIssue jiraIssue = jiraClientV2.getIssues(issuesRequest.getPlatformId());
            Map fields = jiraIssue.getFields();
            List attachments = (List) fields.get("attachment");
            if (!attachments.isEmpty() && attachments.size() > 0) {
                for (int i = 0; i < attachments.size(); i++) {
                    Map attachment = (Map) attachments.get(i);
                    String filename = attachment.get("filename").toString();
                    if (filename.equals(file.getName())) {
                        String fileId = attachment.get("id").toString();
                        jiraClientV2.deleteAttachment(fileId);
                    }
                }
            }
        }
    }

    public void syncJiraIssueAttachments(IssuesWithBLOBs issue, JiraIssue jiraIssue) {
        try {
            List<String> jiraAttachmentsName = new ArrayList<String>();
            AttachmentRequest request = new AttachmentRequest();
            request.setBelongType(AttachmentType.ISSUE.type());
            request.setBelongId(issue.getId());
            List<FileAttachmentMetadata> allMsAttachments = attachmentService.listMetadata(request);
            List<String> msAttachmentsName = allMsAttachments.stream().map(FileAttachmentMetadata::getName).collect(Collectors.toList());
            List attachments = (List) jiraIssue.getFields().get("attachment");
            // 同步Jira中新的附件
            if (CollectionUtils.isNotEmpty(attachments)) {
                for (int i = 0; i < attachments.size(); i++) {
                    Map attachment = (Map) attachments.get(i);
                    String filename = attachment.get("filename").toString();
                    jiraAttachmentsName.add(filename);
                    if ((issue.getDescription() == null || !issue.getDescription().contains(filename))
                            && (issue.getCustomFields() == null || !issue.getCustomFields().contains(filename))
                            && !msAttachmentsName.contains(filename)) {
                        try {
                            byte[] content = jiraClientV2.getAttachmentContent(attachment.get("content").toString());
                            if (content == null) {
                                continue;
                            }
                            FileAttachmentMetadata fileAttachmentMetadata = attachmentService.saveAttachmentByBytes(content, AttachmentType.ISSUE.type(), issue.getId(), filename);
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

            // 删除Jira中不存在的附件
            if (CollectionUtils.isNotEmpty(allMsAttachments)) {
                List<FileAttachmentMetadata> deleteMsAttachments = allMsAttachments.stream()
                        .filter(msAttachment -> !jiraAttachmentsName.contains(msAttachment.getName())).collect(Collectors.toList());
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
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e);
        }
    }

    public void syncJiraRichTextAttachment(IssuesUpdateRequest request) {
        List<String> msFileNames = new ArrayList<String>();
        List<String> jiraFileNames = new ArrayList<String>();
        // 获得所有MS附件名称
        AttachmentRequest attachmentRequest = new AttachmentRequest();
        attachmentRequest.setBelongId(request.getId());
        attachmentRequest.setBelongType(AttachmentType.ISSUE.type());
        List<FileAttachmentMetadata> fileAttachmentMetadata = attachmentService.listMetadata(attachmentRequest);
        List<String> attachmentNames = fileAttachmentMetadata.stream().map(FileAttachmentMetadata::getName).collect(Collectors.toList());
        msFileNames.addAll(attachmentNames);
        // 获取富文本图片附件名称
        List<CustomFieldItemDTO> richTexts = request.getRequestFields().stream().filter(item -> item.getType().equals("richText")).collect(Collectors.toList());
        richTexts.forEach(richText -> {
            if (richText.getValue() != null) {
                String url = richText.getValue().toString();
                if (url.contains("fileName")) {
                    // 本地上传的图片URL
                    msFileNames.add(url.substring(url.indexOf("=") + 1, url.lastIndexOf(")")));
                } else if (url.contains("platform=Jira")) {
                    // Jira同步的图片URL
                    msFileNames.add(url.substring(url.indexOf("[") + 1, url.indexOf("]")));
                }
            }
        });

        // 获得所有Jira附件, 遍历删除MS中不存在的
        JiraIssue jiraIssue = jiraClientV2.getIssues(request.getPlatformId());
        Map fields = jiraIssue.getFields();
        List attachments = (List) fields.get("attachment");
        if (!attachments.isEmpty() && attachments.size() > 0) {
            for (int i = 0; i < attachments.size(); i++) {
                Map attachment = (Map) attachments.get(i);
                String filename = attachment.get("filename").toString();
                jiraFileNames.add(filename);
                if (!msFileNames.contains(filename)) {
                    String fileId = attachment.get("id").toString();
                    jiraClientV2.deleteAttachment(fileId);
                }
            }
        }

        // 上传富文本有关的新附件
        List<File> imageFiles = getImageFiles(request);
        imageFiles.forEach(img -> {
            if (!jiraFileNames.contains(img.getName())) {
                jiraClientV2.uploadAttachment(request.getPlatformId(), img);
            }
        });
    }

    private String parseRichTextImageUrlToJira(String parseRichText) {
        String regex = "(\\!\\[.*?\\]\\((.*?)\\))";
        if (StringUtils.isBlank(parseRichText)) {
            return "";
        }
        Matcher matcher = Pattern.compile(regex).matcher(parseRichText);
        while (matcher.find()) {
            String msRichAttachmentUrl = matcher.group();
            String filename = "";
            if (msRichAttachmentUrl.contains("fileName")) {
                // 本地上传的图片URL
                filename = msRichAttachmentUrl.substring(msRichAttachmentUrl.indexOf("=") + 1, msRichAttachmentUrl.lastIndexOf(")"));
            } else if (msRichAttachmentUrl.contains("platform=Jira")) {
                // Jira同步的图片URL
                filename = msRichAttachmentUrl.substring(msRichAttachmentUrl.indexOf("[") + 1, msRichAttachmentUrl.indexOf("]"));
            }
            parseRichText = parseRichText.replace(msRichAttachmentUrl, "\n!" + filename + "|width=1360,height=876!\n");
        }
        return parseRichText;
    }
}
