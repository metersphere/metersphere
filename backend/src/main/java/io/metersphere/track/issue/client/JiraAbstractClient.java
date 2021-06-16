package io.metersphere.track.issue.client;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.track.issue.domain.JiraAddIssueResponse;
import io.metersphere.track.issue.domain.JiraConfig;
import io.metersphere.track.issue.domain.JiraField;
import io.metersphere.track.issue.domain.JiraIssue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;

import java.util.List;

public abstract class JiraAbstractClient extends BaseClient {

    protected  String ENDPOINT;

    protected  String PREFIX;

    protected  String USER_NAME;

    protected  String PASSWD;

    public JiraIssue getIssues(String issuesId) {
        LogUtil.debug("getIssues: " + issuesId);
        ResponseEntity<String> responseEntity;
        responseEntity = restTemplate.exchange(getBaseUrl() + "/issue/" + issuesId, HttpMethod.GET, getAuthHttpEntity(), String.class);
        return  (JiraIssue) getResultForObject(JiraIssue.class, responseEntity);
    }

    public List<JiraField> getFields() {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/field", HttpMethod.GET, getAuthHttpEntity(), String.class);
        return (List<JiraField>) getResultForList(JiraField.class, response);
    }

    public JiraAddIssueResponse addIssue(String body) {
        LogUtil.debug("addIssue: " + body);
        HttpHeaders headers = getAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/issue", HttpMethod.POST, requestEntity, String.class);
        return (JiraAddIssueResponse) getResultForObject(JiraAddIssueResponse.class, response);
    }

    public String getIssueCreateMetadata() {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/issue/createmeta", HttpMethod.GET, getAuthHttpEntity(), String.class);
        return (String) getResultForObject(String.class, response);
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
