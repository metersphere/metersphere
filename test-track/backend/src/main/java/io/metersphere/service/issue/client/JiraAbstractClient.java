package io.metersphere.service.issue.client;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.service.issue.domain.jira.*;
import io.metersphere.xpack.track.dto.JiraIssue;
import io.metersphere.xpack.track.dto.JiraIssueListResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, JiraCreateMetadataResponse.Field> getCreateMetadata(String projectKey, String issueType) {
        String url = getBaseUrl() + "/issue/createmeta?projectKeys={1}&issuetypeIds={2}&expand=projects.issuetypes.fields";
        ResponseEntity<String> response = null;
        Map<String, JiraCreateMetadataResponse.Field> fields = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class, projectKey, issueType);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        try {
            fields = ((JiraCreateMetadataResponse) getResultForObject(JiraCreateMetadataResponse.class, response))
                    .getProjects().get(0).getIssuetypes().get(0).getFields();
        } catch (Exception e) {
            MSException.throwException(Translator.get("issue_jira_info_error"));
        }
        fields.remove("project");
        fields.remove("issuetype");
        return fields;
    }

    public List<JiraIssueType> getIssueType(String projectKey) {
        JiraIssueProject project = getProject(projectKey);
        String url = getUrl("/issuetype/project?projectId={0}");
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class, project.getId());
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 404) { // Sass 的jira才有这个接口，报错则调用其他接口
                return this.getProject(projectKey).getIssueTypes();
            }
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        return (List<JiraIssueType>) getResultForList(JiraIssueType.class, response);
    }

    public JiraIssueProject getProject(String projectKey) {
        String url = getUrl("/project/" + projectKey);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        return (JiraIssueProject) getResultForObject(JiraIssueProject.class, response);
    }

    public List<JiraUser> getAssignableUser(String projectKey) {
        String url = getBaseUrl() + "/user/assignable/search?project={1}&maxResults=" + 1000 + "&startAt=" + 0;
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), String.class, projectKey);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
        return (List<JiraUser>) getResultForList(JiraUser.class, response);
    }

    public List getDemands(String projectKey, String issueType, int startAt, int maxResults) {
        String jql = getBaseUrl() + "/search?jql=project=" + projectKey + "+AND+issuetype=" + issueType
                + "&maxResults=" + maxResults + "&startAt=" + startAt + "&fields=summary,issuetype";
        ResponseEntity<String> responseEntity = restTemplate.exchange(jql,
                HttpMethod.GET, getAuthHttpEntity(), String.class);
        Map jsonObject = JSON.parseMap(responseEntity.getBody());
        return (List) jsonObject.get("issues");
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

    public List<JiraTransitionsResponse.Transitions> getTransitions(String issueKey) {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl() + "/issue/{1}/transitions", HttpMethod.GET, getAuthHttpEntity(), String.class, issueKey);
        return ((JiraTransitionsResponse) getResultForObject(JiraTransitionsResponse.class, response)).getTransitions();
    }

    public void updateIssue(String id, String body) {
        LogUtil.info("addIssue: " + body);
        HttpHeaders headers = getAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        try {
            restTemplate.exchange(getBaseUrl() + "/issue/" + id, HttpMethod.PUT, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    public void deleteIssue(String id) {
        LogUtil.info("deleteIssue: " + id);
        try {
            restTemplate.exchange(getBaseUrl() + "/issue/" + id, HttpMethod.DELETE, getAuthHttpEntity(), String.class);
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() != 404) {// 404说明jira没有，可以直接删
                MSException.throwException(e.getMessage());
            }
        }
    }

    public void deleteAttachment(String id) {
        LogUtil.info("deleteAttachment: " + id);
        try {
            restTemplate.exchange(getBaseUrl() + "/attachment/" + id, HttpMethod.DELETE, getAuthHttpEntity(), String.class);
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() != 404) {// 404说明jira没有，可以直接删
                MSException.throwException(e.getMessage());
            }
        }
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
        }
        System.out.println(response);
    }

    public void auth() {
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(getBaseUrl() + "/myself", HttpMethod.GET, getAuthHttpEntity(), String.class);
            if (StringUtils.isBlank(response.getBody()) || (StringUtils.isNotBlank(response.getBody()) && !response.getBody().startsWith("{\"self\""))) {
                MSException.throwException(Translator.get("jira_auth_url_error"));
            }
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401) {
                MSException.throwException(Translator.get("jira_auth_error"));
            } else {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(e.getMessage());
            }
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

    protected HttpHeaders getAuthJsonHeader() {
        HttpHeaders headers = getAuthHeader();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected String getBaseUrl() {
        return ENDPOINT + PREFIX;
    }

    protected String getUrl(String path) {
        return getBaseUrl() + path;
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

    public JiraIssueListResponse getProjectIssues(Integer startAt, Integer maxResults, String projectKey, String issueType) {
        return getProjectIssues(startAt, maxResults, projectKey, issueType, null);
    }

    public JiraIssueListResponse getProjectIssues(Integer startAt, Integer maxResults, String projectKey, String issueType, String fields) {
        ResponseEntity<String> responseEntity;
        String url = getBaseUrl() + "/search?startAt={1}&maxResults={2}&jql=project={3}+AND+issuetype={4}";
        if (StringUtils.isNotBlank(fields)) {
            url = url + "&fields=" + fields;
        }
        responseEntity = restTemplate.exchange(url,
                HttpMethod.GET, getAuthHttpEntity(), String.class, startAt, maxResults, projectKey, issueType);
        return  (JiraIssueListResponse)getResultForObject(JiraIssueListResponse.class, responseEntity);
    }

    public byte[] getAttachmentContent(String url) {
        ResponseEntity<byte[]> responseEntity;
        responseEntity = restTemplate.exchange(url,
                HttpMethod.GET, getAuthHttpEntity(), byte[].class);
        return responseEntity.getBody();
    }

    public JiraIssueListResponse getProjectIssuesAttachment(Integer startAt, Integer maxResults, String projectKey, String issueType) {
        return getProjectIssues(startAt, maxResults, projectKey, issueType, "attachment");

    }
    public void setTransitions(String jiraKey, JiraTransitionsResponse.Transitions transitions) {
        LogUtil.info("setTransitions: " + transitions);
        Map jsonObject = new LinkedHashMap();
        jsonObject.put("transition", transitions);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSON.toJSONString(jsonObject), getAuthJsonHeader());
        try {
            restTemplate.exchange(getBaseUrl() + "/issue/{1}/transitions", HttpMethod.POST, requestEntity, String.class, jiraKey);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    public ResponseEntity proxyForGet(String url, Class responseEntityClazz) {
        LogUtil.info("jira proxyForGet: " + url);
        return restTemplate.exchange(url, HttpMethod.GET, getAuthHttpEntity(), responseEntityClazz);
    }
}
