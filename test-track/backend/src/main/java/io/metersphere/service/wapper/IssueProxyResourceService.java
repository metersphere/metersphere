package io.metersphere.service.wapper;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.service.issue.platform.IssueFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssueProxyResourceService {

    @Resource
    private RestTemplate restTemplate;

    /**
     * http 代理
     * 如果当前访问地址是 https，直接访问 http 的图片资源
     * 由于浏览器的安全机制，http 会被转成 https
     * @param url
     * @param platform
     * @return
     */
    public ResponseEntity<byte[]> getMdImageByUrl(String url, String platform, String projectId, String workspaceId) {
        if (url.contains("md/get/url")) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        if (StringUtils.isNotBlank(platform)) {
            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setProjectId(projectId);
            issuesRequest.setWorkspaceId(workspaceId);
            return IssueFactory.createPlatform(platform, issuesRequest)
                    .proxyForGet(url, byte[].class);
        }
        return restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
    }
}
