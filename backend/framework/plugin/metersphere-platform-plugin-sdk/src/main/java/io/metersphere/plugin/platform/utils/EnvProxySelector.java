package io.metersphere.plugin.platform.utils;

import io.metersphere.plugin.sdk.util.PluginLogUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class EnvProxySelector extends ProxySelector {
    ProxySelector defaultProxySelector = ProxySelector.getDefault();

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        PluginLogUtils.error("connectFailed : " + uri);
    }

    /**
     * 获取环境变量http代理配置，
     *
     * @param uri
     * @return
     */
    @Override
    public List<Proxy> select(URI uri) {
        String httpProxy = System.getenv("http_proxy");
        String httpsProxy = System.getenv("https_proxy");
        URI proxy = null;
        try {
            if (StringUtils.equalsIgnoreCase(uri.getScheme(), "https")
                    && StringUtils.isNotBlank(httpsProxy)) {
                proxy = new URI(httpsProxy);
            } else if (StringUtils.isNotBlank(httpProxy)) {
                proxy = new URI(httpProxy);
            }
            if (proxy != null) {
                return Arrays.asList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort())));
            }
        } catch (URISyntaxException e) {
            PluginLogUtils.error(e);
        }
        return defaultProxySelector.select(uri);
    }
}
