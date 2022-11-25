package io.metersphere.service.wapper;

import io.metersphere.service.PlatformPluginService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssueProxyResourceService {

    @Resource
    private PlatformPluginService platformPluginService;

    /**
     * http 代理
     * 如果当前访问地址是 https，直接访问 http 的图片资源
     * 由于浏览器的安全机制，http 会被转成 https
     *
     * @param path
     * @param platform
     * @return
     */
    public ResponseEntity<byte[]> getMdImageByPath(String path, String platform, String workspaceId) {
        return platformPluginService.getPlatform(platform, workspaceId)
                .proxyForGet(path, byte[].class);
    }
}
