package io.metersphere.track.issue;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestCaseIssuesMapper;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.IntegrationService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.ResourceService;
import io.metersphere.service.UserService;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
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
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractIssuePlatform implements IssuesPlatform {

    private static RestTemplate restTemplate;

    protected IntegrationService integrationService;
    protected TestCaseIssuesMapper testCaseIssuesMapper;
    protected ProjectService projectService;
    protected TestCaseService testCaseService;
    protected IssuesMapper issuesMapper;
    protected ExtIssuesMapper extIssuesMapper;
    protected ResourceService resourceService;
    protected RestTemplate restTemplateIgnoreSSL;
    protected UserService userService;
    protected WorkspaceMapper workspaceMapper;
    protected ProjectMapper projectMapper;
    protected String testCaseId;
    protected String projectId;
    protected String key;
    protected String orgId;
    protected String userId;


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
        this.integrationService = CommonBeanFactory.getBean(IntegrationService.class);
        this.testCaseIssuesMapper = CommonBeanFactory.getBean(TestCaseIssuesMapper.class);
        this.projectService = CommonBeanFactory.getBean(ProjectService.class);
        this.testCaseService = CommonBeanFactory.getBean(TestCaseService.class);
        this.userService = CommonBeanFactory.getBean(UserService.class);
        this.issuesMapper = CommonBeanFactory.getBean(IssuesMapper.class);
        this.extIssuesMapper = CommonBeanFactory.getBean(ExtIssuesMapper.class);
        this.resourceService = CommonBeanFactory.getBean(ResourceService.class);
        this.testCaseId = issuesRequest.getTestCaseId();
        this.projectId = issuesRequest.getProjectId();
        this.orgId = issuesRequest.getOrganizationId();
        this.userId = issuesRequest.getUserId();
        this.restTemplateIgnoreSSL = restTemplate;
    }

    protected String getPlatformConfig(String platform) {
        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(orgId)) {
            MSException.throwException("organization id is null");
        }
        request.setOrgId(orgId);
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
    abstract String getProjectId(String projectId);

    protected boolean isIntegratedPlatform(String orgId, String platform) {
        IntegrationRequest request = new IntegrationRequest();
        request.setPlatform(platform);
        request.setOrgId(orgId);
        ServiceIntegration integration = integrationService.get(request);
        return StringUtils.isNotBlank(integration.getId());
    }

    protected void insertTestCaseIssues(String issuesId, String caseId) {
        if (StringUtils.isNotBlank(caseId)) {
            TestCaseIssues testCaseIssues = new TestCaseIssues();
            testCaseIssues.setId(UUID.randomUUID().toString());
            testCaseIssues.setIssuesId(issuesId);
            testCaseIssues.setTestCaseId(caseId);
            testCaseIssuesMapper.insert(testCaseIssues);
        }
    }

    protected void handleIssueUpdate(IssuesUpdateRequest request) {
        request.setUpdateTime(System.currentTimeMillis());
        issuesMapper.updateByPrimaryKeySelective(request);
        handleTestCaseIssues(request);
    }

    protected void handleTestCaseIssues(IssuesUpdateRequest issuesRequest) {
        String issuesId = issuesRequest.getId();
        if (StringUtils.isNotBlank(issuesRequest.getTestCaseId())) {
          insertTestCaseIssues(issuesId, issuesRequest.getTestCaseId());
      } else {
          List<String> testCaseIds = issuesRequest.getTestCaseIds();
          TestCaseIssuesExample example = new TestCaseIssuesExample();
          example.createCriteria().andIssuesIdEqualTo(issuesId);
          testCaseIssuesMapper.deleteByExample(example);
          if (!CollectionUtils.isEmpty(testCaseIds)) {
              testCaseIds.forEach(caseId -> {
                  insertTestCaseIssues(issuesId, caseId);
              });
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

    protected void insertIssues(String id, IssuesUpdateRequest issuesRequest) {
        IssuesWithBLOBs issues = new IssuesWithBLOBs();
        BeanUtils.copyBean(issues, issuesRequest);
        issues.setId(id);
        issues.setCreateTime(System.currentTimeMillis());
        issues.setUpdateTime(System.currentTimeMillis());
        issues.setNum(getNextNum(issuesRequest.getProjectId()));
        issues.setPlatformStatus(issuesRequest.getPlatformStatus());
        issuesMapper.insert(issues);
    }

    protected int getNextNum(String projectId) {
        Issues issue = extIssuesMapper.getNextNum(projectId);
        if (issue == null || issue.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(issue.getNum() + 1).orElse(100001);
        }
    }

    protected List<CustomFieldItemDTO> getCustomFields(String customFieldsStr) {
        if (StringUtils.isNotBlank(customFieldsStr)) {
            return JSONArray.parseArray(customFieldsStr, CustomFieldItemDTO.class);
        }
        return new ArrayList<>();
    }

    /**
     * 将html格式的缺陷描述转成ms平台的格式
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
        String result = "";
        while (matcher.find()) {
            String path = matcher.group(2);
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() -1);
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
            if (url.contains("/resource/md/get/")) {
                String path = url.substring(url.indexOf("/resource/md/get/"));
                String name = path.substring(path.indexOf("/resource/md/get/") + 26);
                String mdLink = "![" + name + "](" + path +  ")";
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
                if (path.contains("/resource/md/get/")) {
                    String name = path.substring(path.indexOf("/resource/md/get/") + 17);
                    files.add(new File(FileUtils.MD_IMAGE_DIR + "/" + name));
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
        return files;
    }

    protected UserDTO.PlatformInfo getUserPlatInfo(String orgId) {
        return userService.getCurrentPlatformInfo(orgId);
    }
}
