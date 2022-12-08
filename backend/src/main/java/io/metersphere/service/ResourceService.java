package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.controller.request.MdUploadRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.track.issue.IssueFactory;
import io.metersphere.track.issue.domain.jira.JiraConfig;
import io.metersphere.track.issue.domain.zentao.ZentaoConfig;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceService {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private IntegrationService integrationService;

    public String mdUpload(MdUploadRequest request, MultipartFile file) {
        String fileName = request.getId() + request.getFileName().substring(request.getFileName().lastIndexOf("."));
        FileUtils.uploadFile(file, FileUtils.MD_IMAGE_DIR, fileName);
        return fileName;
    }

    public ResponseEntity<FileSystemResource> getMdImage(String name) {
        if (name.contains("/")) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        return getImage(FileUtils.MD_IMAGE_DIR + "/" + name);
    }

    public ResponseEntity<FileSystemResource> getUiResultImage(String name, String reportId) {
        if (name.contains("/")) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        return getImage(FileUtils.UI_IMAGE_DIR + "/" + reportId +  "/" + name);
    }

    public ResponseEntity<FileSystemResource> getImage(String path) {
        File file = new File(path);
        HttpHeaders headers = new HttpHeaders();
        String fileName = encodeFileName(file.getName());
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(file));
    }

    public String encodeFileName(String fileName) {
        try {
            return URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e);
            return fileName;
        }
    }

    public String decodeFileName(String fileName) {
        try {
            return URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e);
            return fileName;
        }
    }

    public void mdDelete(String fileName) {
        if (fileName.contains("/")) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        FileUtils.deleteFile(FileUtils.MD_IMAGE_DIR + "/" + fileName);
    }

    /**
     * http 代理
     * 如果当前访问地址是 https，直接访问 http 的图片资源
     * 由于浏览器的安全机制，http 会被转成 https
     * @param url
     * @param platform
     * @return
     */
    public ResponseEntity<byte[]> getMdImageByUrl(String url, String platform) {
        if (url.contains("md/get/url")) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        IntegrationRequest request = new IntegrationRequest();
        request.setPlatform(platform);
        if (StringUtils.isBlank(platform)) {
            request.setPlatform(IssuesManagePlatform.Zentao.name());
        }
        ServiceIntegration serviceIntegration = integrationService.get(request);
        if (StringUtils.isNotBlank(platform)) {
            JiraConfig jiraConfig = JSON.parseObject(serviceIntegration.getConfiguration(), JiraConfig.class);
            validateUrl(url, jiraConfig.getUrl(), "/secure/attachment/", "/attachment/content");
            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
            issuesRequest.setWorkspaceId(currentWorkspaceId);
            return IssueFactory.createPlatform(platform, issuesRequest)
                    .proxyForGet(url, byte[].class);
        } else {
            ZentaoConfig zentaoConfig = JSON.parseObject(serviceIntegration.getConfiguration(), ZentaoConfig.class);
            validateUrl(url, zentaoConfig.getUrl(), "/index.php", "/file-read-");
            return restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
        }
    }

    private void validateUrl(String url, String platFormUrl, String ...path) {
        boolean isValidate = true;
        try {
            URI platFormUri = new URI(platFormUrl);
            URI resourceUri = new URI(url);
            // 对比host
            if (!StringUtils.equals(platFormUri.getHost(), resourceUri.getHost())) {
                isValidate = false;
            }
            // 对比白名单
            if (!StringUtils.containsAny(resourceUri.getPath(), path)) {
                isValidate = false;
            }
        } catch (URISyntaxException e) {
            isValidate = false;
        }
        if (!isValidate) {
            MSException.throwException("illegal url");
        }
    }
}
