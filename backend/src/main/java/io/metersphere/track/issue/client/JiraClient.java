package io.metersphere.track.issue.client;

import io.metersphere.commons.exception.MSException;
import io.metersphere.track.issue.domain.JiraAddIssueResponse;
import io.metersphere.track.issue.domain.JiraConfig;
import io.metersphere.track.issue.domain.JiraField;
import io.metersphere.track.issue.domain.JiraIssue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;

import java.util.List;

public class JiraClient extends BaseClient {

    private static String ENDPOINT;

    private static String PREFIX = "/rest/api/3";

    private static String USER_NAME;

    private static String PASSWD;

    public static List<JiraField> getFields() {
        String url = getBaseUrl() + "/field";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class);
        return (List<JiraField>) getResultForList(JiraField.class, response);
    }

    public static JiraIssue getIssues(String issuesId) {
        HttpEntity<MultiValueMap> requestEntity = getAuthHttpEntity();
        ResponseEntity<String> responseEntity;
        responseEntity = restTemplate.exchange(ENDPOINT + "/rest/api/2/issue/" + issuesId, HttpMethod.GET, requestEntity, String.class);
        return  (JiraIssue) getResultForObject(JiraIssue.class, responseEntity);
    }

    public static JiraAddIssueResponse addIssue(String body) {
        HttpHeaders headers = getAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> entity = restTemplate.exchange(getBaseUrl() + "/issue", HttpMethod.POST, requestEntity, String.class);
        return (JiraAddIssueResponse) getResultForObject(JiraAddIssueResponse.class, entity);
    }

    public static void setConfig(JiraConfig config) {
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

    private static HttpEntity<MultiValueMap> getAuthHttpEntity() {
        return new HttpEntity<>(getAuthHeader());
    }

    private static HttpHeaders getAuthHeader() {
        return getBasicHttpHeaders(USER_NAME, PASSWD);
    }

    private static String getBaseUrl() {
        return ENDPOINT + PREFIX;
    }
}
