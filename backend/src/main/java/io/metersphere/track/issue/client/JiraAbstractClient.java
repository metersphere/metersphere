package io.metersphere.track.issue.client;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.issue.domain.Jira.JiraAddIssueResponse;
import io.metersphere.track.issue.domain.Jira.JiraConfig;
import io.metersphere.track.issue.domain.Jira.JiraField;
import io.metersphere.track.issue.domain.Jira.JiraIssue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.List;

public abstract class JiraAbstractClient extends BaseClient {

    protected  String ENDPOINT;

    protected  String PREFIX;

    protected  String USER_NAME;

    protected  String PASSWD;

    public JiraIssue getIssues(String issuesId) {
        LogUtil.info("getIssues: " + issuesId);
        ResponseEntity<String> responseEntity;
        responseEntity = restTemplate.exchange(getBaseUrl() + "/issue/" + issuesId, HttpMethod.GET, getAuthHttpEntity(), String.class);
        return  (JiraIssue) getResultForObject(JiraIssue.class, responseEntity);
    }

    public List<JiraField> getFields() {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/field", HttpMethod.GET, getAuthHttpEntity(), String.class);
        return (List<JiraField>) getResultForList(JiraField.class, response);
    }

    public JiraAddIssueResponse addIssue(String body) {
        LogUtil.info("addIssue: " + body);
        HttpHeaders headers = getAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(getBaseUrl() + "/issue", HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        return (JiraAddIssueResponse) getResultForObject(JiraAddIssueResponse.class, response);
    }

    public void uploadAttachment(String issueKey, File file) {
        HttpHeaders authHeader = getAuthHeader();
        authHeader.add("X-Atlassian-Token", "no-check");
        authHeader.setContentType(MediaType.parseMediaType("multipart/form-data; charset=UTF-8"));

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        FileSystemResource fileResource = new FileSystemResource(file);
        paramMap.add("file", fileResource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramMap, authHeader);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(getBaseUrl() + "/issue/" + issueKey + "/attachments", HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        System.out.println(response);
    }

    public void auth() {
        try {
            restTemplate.exchange(getBaseUrl() + "/myself", HttpMethod.GET, getAuthHttpEntity(), String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    protected HttpEntity<MultiValueMap> getAuthHttpEntity() {
        return new HttpEntity<>(getAuthHeader());
    }

    protected HttpHeaders getAuthHeader() {
        return getBasicHttpHeaders(USER_NAME, PASSWD);
    }

    protected String getBaseUrl() {
        return ENDPOINT + PREFIX;
    }

    public void setConfig(JiraConfig config) {
        if (config == null) {
            MSException.throwException("config is null");
        }
        String url = config.getUrl();

        if (StringUtils.isNotBlank(url) && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        ENDPOINT = url;
        USER_NAME = config.getAccount();
        PASSWD = config.getPassword();
    }
}
