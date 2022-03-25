package io.metersphere.track.issue;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestCaseIssuesMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.constants.IssueRefType;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.IssueTemplateDao;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.*;
import io.metersphere.track.issue.domain.ProjectIssueConfig;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import io.metersphere.track.service.IssuesService;
import io.metersphere.track.service.TestCaseIssueService;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractIssuePlatform implements IssuesPlatform {

    private static RestTemplate restTemplate;

    protected IntegrationService integrationService;
    protected TestCaseIssueService testCaseIssueService;
    protected TestCaseIssuesMapper testCaseIssuesMapper;
    protected ProjectService projectService;
    protected TestCaseService testCaseService;
    protected IssuesMapper issuesMapper;
    protected ExtIssuesMapper extIssuesMapper;
    protected ResourceService resourceService;
    protected RestTemplate restTemplateIgnoreSSL;
    protected UserService userService;
    protected ProjectMapper projectMapper;
    protected String testCaseId;
    protected String projectId;
    protected String key;
    protected String workspaceId;
    protected String userId;
    protected String defaultCustomFields;
    protected boolean isThirdPartTemplate;


    public String getKey() {
        return key;
    }

    static {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

            restTemplate = new RestTemplate(requestFactory);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public AbstractIssuePlatform(IssuesRequest issuesRequest) {
        this();
        this.testCaseId = issuesRequest.getTestCaseId();
        this.projectId = issuesRequest.getProjectId();
        this.workspaceId = issuesRequest.getWorkspaceId();
        this.userId = issuesRequest.getUserId();
        this.defaultCustomFields = issuesRequest.getDefaultCustomFields();
    }

    public AbstractIssuePlatform() {
        this.integrationService = CommonBeanFactory.getBean(IntegrationService.class);
        this.testCaseIssuesMapper = CommonBeanFactory.getBean(TestCaseIssuesMapper.class);
        this.projectService = CommonBeanFactory.getBean(ProjectService.class);
        this.testCaseService = CommonBeanFactory.getBean(TestCaseService.class);
        this.userService = CommonBeanFactory.getBean(UserService.class);
        this.issuesMapper = CommonBeanFactory.getBean(IssuesMapper.class);
        this.extIssuesMapper = CommonBeanFactory.getBean(ExtIssuesMapper.class);
        this.resourceService = CommonBeanFactory.getBean(ResourceService.class);
        this.testCaseIssueService = CommonBeanFactory.getBean(TestCaseIssueService.class);
        this.restTemplateIgnoreSSL = restTemplate;
    }

    protected String getPlatformConfig(String platform) {
        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(workspaceId)) {
            MSException.throwException("workspace id is null");
        }
        request.setWorkspaceId(workspaceId);
        request.setPlatform(platform);

        ServiceIntegration integration = integrationService.get(request);
        return integration.getConfiguration();
    }

    protected HttpHeaders auth(String apiUser, String password) {
        String authKey = EncryptUtils.base64Encoding(apiUser + ":" + password);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + authKey);
        return headers;
    }

    /**
     * 获取平台与项目相关的属性
     *
     * @return 其他平台和本地项目绑定的属性值
     */
    public abstract String getProjectId(String projectId);

    public String getProjectId(String projectId, Function<Project, String> getProjectKeyFuc) {
        return getProjectKeyFuc.apply(getProject(projectId, getProjectKeyFuc));
    }

    public Project getProject(String projectId,  Function<Project, String> getProjectKeyFuc) {
        Project project;
        if (StringUtils.isNotBlank(projectId)) {
            project = projectService.getProjectById(projectId);
        } else {
            TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
            project = projectService.getProjectById(testCase.getProjectId());
        }
        String projectKey = getProjectKeyFuc.apply(project);
        if (StringUtils.isBlank(projectKey)) {
            MSException.throwException("请在项目设置配置 " + key + "项目ID");
        }
        return project;
    }

    public ProjectIssueConfig getProjectConfig(String configStr) {
        ProjectIssueConfig issueConfig;
        if (StringUtils.isNotBlank(configStr)) {
            issueConfig = JSONObject.parseObject(configStr, ProjectIssueConfig.class);
        } else {
            issueConfig = new ProjectIssueConfig();
        }
        return issueConfig;
    }

    protected void handleIssueUpdate(IssuesUpdateRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        issuesMapper.updateByPrimaryKeySelective(request);
        handleTestCaseIssues(request);
    }

    protected void handleTestCaseIssues(IssuesUpdateRequest issuesRequest) {
        String issuesId = issuesRequest.getId();
        List<String> deleteCaseIds = issuesRequest.getDeleteResourceIds();

        if (!CollectionUtils.isEmpty(deleteCaseIds)) {
            TestCaseIssuesExample example = new TestCaseIssuesExample();
            example.createCriteria().andResourceIdIn(deleteCaseIds);
            // 测试计划的用例 deleteCaseIds 是空的， 不会进到这里
            example.or(example.createCriteria().andRefIdIn(deleteCaseIds));
            testCaseIssuesMapper.deleteByExample(example);
        }

        List<String> addCaseIds = issuesRequest.getAddResourceIds();
        TestCaseIssueService testCaseIssueService = CommonBeanFactory.getBean(TestCaseIssueService.class);

        if (!CollectionUtils.isEmpty(addCaseIds)) {
            if (issuesRequest.getIsPlanEdit()) {
                addCaseIds.forEach(caseId -> {
                    testCaseIssueService.add(issuesId, caseId, issuesRequest.getRefId(), IssueRefType.PLAN_FUNCTIONAL.name());
                    testCaseIssueService.updateIssuesCount(caseId);
                });
            } else {
                addCaseIds.forEach(caseId -> testCaseIssueService.add(issuesId, caseId, null, IssueRefType.FUNCTIONAL.name()));
            }
        }
    }

    protected void insertIssuesWithoutContext(String id, IssuesUpdateRequest issuesRequest) {
        IssuesWithBLOBs issues = new IssuesWithBLOBs();
        issues.setId(id);
        issues.setPlatform(issuesRequest.getPlatform());
        issues.setProjectId(issuesRequest.getProjectId());
        issues.setCustomFields(issuesRequest.getCustomFields());
        issues.setCreator(issuesRequest.getCreator());
        issues.setCreateTime(System.currentTimeMillis());
        issues.setUpdateTime(System.currentTimeMillis());
        issues.setNum(getNextNum(issuesRequest.getProjectId()));
        issues.setResourceId(issuesRequest.getResourceId());
        issuesMapper.insert(issues);
    }

    protected IssuesWithBLOBs insertIssues(IssuesUpdateRequest issuesRequest) {
        IssuesWithBLOBs issues = new IssuesWithBLOBs();
        BeanUtils.copyBean(issues, issuesRequest);
        issues.setId(issuesRequest.getId());
        issues.setPlatformId(issuesRequest.getPlatformId());
        issues.setCreateTime(System.currentTimeMillis());
        issues.setUpdateTime(System.currentTimeMillis());
        issues.setNum(getNextNum(issuesRequest.getProjectId()));
        issues.setPlatformStatus(issuesRequest.getPlatformStatus());
        issues.setCreator(SessionUtils.getUserId());
        issuesMapper.insert(issues);
        return issues;
    }

    protected int getNextNum(String projectId) {
        Issues issue = extIssuesMapper.getNextNum(projectId);
        if (issue == null || issue.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(issue.getNum() + 1).orElse(100001);
        }
    }

    /**
     * 将html格式的缺陷描述转成ms平台的格式
     *
     * @param htmlDesc
     * @return
     */
    protected String htmlDesc2MsDesc(String htmlDesc) {
        String desc = htmlImg2MsImg(htmlDesc);
        Document document = Jsoup.parse(desc);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        desc = document.html().replaceAll("\\\\n", "\n");
        desc = Jsoup.clean(desc, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        return desc.replace("&nbsp;", "");
    }

    protected String msImg2HtmlImg(String input, String endpoint) {
        // ![中心主题.png](/resource/md/get/a0b19136_中心主题.png) -> <img src="xxx/resource/md/get/a0b19136_中心主题.png"/>
        String regex = "(\\!\\[.*?\\]\\((.*?)\\))";
        Pattern pattern = Pattern.compile(regex);
        if (StringUtils.isBlank(input)) {
            return "";
        }
        Matcher matcher = pattern.matcher(input);
        String result = input;
        while (matcher.find()) {
            String path = matcher.group(2);
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            path = " <img src=\"" + endpoint + path + "\"/>";
            result = matcher.replaceFirst(path);
            matcher = pattern.matcher(result);
        }
        return result;
    }

    protected String removeImage(String input) {
        String regex = "(\\!\\[.*?\\]\\((.*?)\\))";
        if (StringUtils.isBlank(input)) {
            return "";
        }
        Matcher matcher = Pattern.compile(regex).matcher(input);
        while (matcher.find()) {
            matcher.group();
            return matcher.replaceAll("");
        }
        return input;
    }

    protected String getImages(String input) {
        String result = "";
        String regex = "(\\!\\[.*?\\]\\((.*?)\\))";
        if (StringUtils.isBlank(input)) {
            return result;
        }
        Matcher matcher = Pattern.compile(regex).matcher(input);
        while (matcher.find()) {
            result += matcher.group();
        }
        return result;
    }

    protected String htmlImg2MsImg(String input) {
        // <img src="xxx/resource/md/get/a0b19136_中心主题.png"/> ->  ![中心主题.png](/resource/md/get/a0b19136_中心主题.png)
        String regex = "(<img\\s*src=\\\"(.*?)\\\".*?>)";
        Pattern pattern = Pattern.compile(regex);
        if (StringUtils.isBlank(input)) {
            return "";
        }
        Matcher matcher = pattern.matcher(input);
        String result = input;
        while (matcher.find()) {
            String url = matcher.group(2);
            if (url.contains("/resource/md/get/")) { // 兼容旧数据
                String path = url.substring(url.indexOf("/resource/md/get/"));
                String name = path.substring(path.indexOf("/resource/md/get/") + 26);
                String mdLink = "![" + name + "](" + path + ")";
                result = matcher.replaceFirst(mdLink);
                matcher = pattern.matcher(result);
            } else if(url.contains("/resource/md/get")) { //新数据走这里
                String path = url.substring(url.indexOf("/resource/md/get"));
                String name = path.substring(path.indexOf("/resource/md/get") + 35);
                String mdLink = "![" + name + "](" + path + ")";
                result = matcher.replaceFirst(mdLink);
                matcher = pattern.matcher(result);
            }
        }
        return result;
    }

    public List<File> getImageFiles(String input) {
        List<File> files = new ArrayList<>();
        String regex = "(\\!\\[.*?\\]\\((.*?)\\))";
        Pattern pattern = Pattern.compile(regex);
        if (StringUtils.isBlank(input)) {
            return new ArrayList<>();
        }
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            try {
                String path = matcher.group(2);
                if (path.contains("/resource/md/get/")) { // 兼容旧数据
                    String name = path.substring(path.indexOf("/resource/md/get/") + 17);
                    files.add(new File(FileUtils.MD_IMAGE_DIR + "/" + name));
                } else if (path.contains("/resource/md/get")) { // 新数据走这里
                    String name = path.substring(path.indexOf("/resource/md/get") + 26);
                    files.add(new File(FileUtils.MD_IMAGE_DIR + "/" + URLDecoder.decode(name, "UTF-8")));
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
        return files;
    }

    protected UserDTO.PlatformInfo getUserPlatInfo(String workspaceId) {
        return userService.getCurrentPlatformInfo(workspaceId);
    }

    @Override
    public void deleteIssue(String id) {
        IssuesService issuesService = CommonBeanFactory.getBean(IssuesService.class);
        issuesService.deleteIssue(id);
    }

    protected void addCustomFields(IssuesUpdateRequest issuesRequest, MultiValueMap<String, Object> paramMap) {
        List<CustomFieldItemDTO> customFields = CustomFieldService.getCustomFields(issuesRequest.getCustomFields());
        customFields.forEach(item -> {
            if (StringUtils.isNotBlank(item.getCustomData())) {
                paramMap.add(item.getCustomData(), item.getValue());
            }
        });
    }

    protected Object getSyncJsonParamValue(Object value) {
        JSONObject valObj = ((JSONObject) value);
        String accountId = valObj.getString("accountId");
        JSONObject child = valObj.getJSONObject("child");
        if (child != null) {// 级联框
            List<Object> values = new ArrayList<>();
            if (StringUtils.isNotBlank(valObj.getString("id")))  {
                values.add(valObj.getString("id"));
            }
            if (StringUtils.isNotBlank(child.getString("id")))  {
                values.add(child.getString("id"));
            }
            return values;
        } else if (StringUtils.isNotBlank(accountId) && isThirdPartTemplate) {
            // 用户选择框
            return accountId;
        } else {
            String id = valObj.getString("id");
            if (StringUtils.isNotBlank(id)) {
                return valObj.getString("id");
            } else {
                return valObj.getString("key");
            }
        }
    }

    protected String syncIssueCustomField(String customFieldsStr, JSONObject issue) {
        List<CustomFieldItemDTO> customFields = CustomFieldService.getCustomFields(customFieldsStr);
        Set<String> names = issue.keySet();
        customFields.forEach(item -> {
            String fieldName = item.getCustomData();
            Object value = issue.get(fieldName);
            if (value != null) {
               if (value instanceof JSONObject) {
                   item.setValue(getSyncJsonParamValue(value));
                } else if (value instanceof JSONArray) {
                    List<Object> values = new ArrayList<>();
                    ((JSONArray)value).forEach(attr -> {
                        if (attr instanceof JSONObject) {
                            values.add(getSyncJsonParamValue(attr));
                        } else {
                            values.add(attr);
                        }
                    });
                    item.setValue(values);
                } else {
                    item.setValue(value);
                }
            } else if (names.contains(fieldName)) {
                if (item.getType().equals(CustomFieldType.CHECKBOX.getValue())) {
                    item.setValue(new ArrayList<>());
                } else {
                    item.setValue(null);
                }
            }
        });
        return JSONObject.toJSONString(customFields);
    }

    @Override
    public void syncAllIssues(Project project) {}

    @Override
    public IssueTemplateDao getThirdPartTemplate() {return null;}

    protected List<IssuesWithBLOBs> getIssuesByPlatformIds(List<String> platformIds) {
        IssuesService issuesService = CommonBeanFactory.getBean(IssuesService.class);
        return issuesService.getIssuesByPlatformIds(platformIds, projectId);
    }

    protected Map<String, IssuesWithBLOBs> getUuIdMap(List<IssuesWithBLOBs> issues) {
        HashMap<String, IssuesWithBLOBs> issueMap = new HashMap<>();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(issues)) {
            issues.forEach(item -> issueMap.put(item.getPlatformId(), item));
        }
        return issueMap;
    }

    protected void deleteSyncIssue(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) return;
        IssuesExample example = new IssuesExample();
        IssuesWithBLOBs issue = new IssuesWithBLOBs();
        issue.setPlatformStatus(IssuesStatus.DELETE.toString());
        example.createCriteria().andIdIn(ids);
        issuesMapper.updateByExampleSelective(issue, example);
    }

    protected List<String> updateSyncDeleteIds(List<String> uuIds, List<String> syncDeleteIds, String platform) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(uuIds)) {
            // 每次获取不在当前查询的缺陷里的 id
            List<String> notInIds = extIssuesMapper.selectIdNotInUuIds(projectId, platform, uuIds);
            if (syncDeleteIds == null) {
                syncDeleteIds = notInIds;
            } else {
                // 求交集，即不在所有查询里的缺陷，即要删除的缺陷
                syncDeleteIds.retainAll(notInIds);
            }
        }
        return syncDeleteIds;
    }

    protected void mergeCustomField(IssuesWithBLOBs issues, String defaultCustomField) {
        if (StringUtils.isNotBlank(defaultCustomField)) {
            String issuesCustomFields = issues.getCustomFields();
            if (StringUtils.isBlank(issuesCustomFields) || issuesCustomFields.startsWith("{")) issuesCustomFields = "[]";
            JSONArray issueFields = JSONArray.parseArray(issuesCustomFields);
            Set<String> ids = issueFields.stream().map(i -> ((JSONObject) i).getString("id")).collect(Collectors.toSet());
            JSONArray defaultFields = JSONArray.parseArray(defaultCustomField);
            defaultFields.forEach(item -> { // 如果自定义字段里没有模板新加的字段，就把新字段加上
                String id = ((JSONObject) item).getString("id");
                if (StringUtils.isBlank(id)) {
                    id = ((JSONObject) item).getString("key");
                    ((JSONObject) item).put("id", id);
                }
                if (!ids.contains(id)) {
                    issueFields.add(item);
                }
            });
            issues.setCustomFields(issueFields.toJSONString());
        }
    }

    public <T> T getConfig(String platform, Class<T> clazz) {
        String config = getPlatformConfig(platform);
        if (StringUtils.isBlank(config)) {
            MSException.throwException("配置为空");
        }
        return JSONObject.parseObject(config, clazz);
    }

    public void buildSyncCreate(IssuesWithBLOBs issue, String platformId, Integer nextNum) {
        issue.setProjectId(projectId);
        issue.setId(UUID.randomUUID().toString());
        issue.setPlatformId(platformId);
        issue.setCreator(SessionUtils.getUserId());
        issue.setNum(nextNum);
    }

    public boolean isThirdPartTemplate() {
        Project project = projectService.getProjectById(projectId);
        if (project.getThirdPartTemplate() != null && project.getThirdPartTemplate()) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean checkProjectExist(String relateId) {
        return null;
    }
}
