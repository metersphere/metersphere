package io.metersphere.api.dubbo;

import io.metersphere.api.dubbo.utils.MD5Util;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.constants.RegistryConstants;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.registry.Constants;
import org.apache.dubbo.registry.NotifyListener;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RegistryServerSync
 */
public class RegistryServerSync implements NotifyListener, Serializable {

    private static final long serialVersionUID = -1744756264793278229L;
    private static ConcurrentMap<String, RegistryServerSync> cache = new ConcurrentHashMap<>();

    public static RegistryServerSync get(String key) {
        RegistryServerSync sync = cache.get(key);
        if (sync == null) {
            cache.putIfAbsent(key, new RegistryServerSync());
            sync = cache.get(key);
        }
        return sync;
    }

    public static final URL SUBSCRIBE = new URL(Constants.ADMIN_PROTOCOL, NetUtils.getLocalHost(), 0, "",
            CommonConstants.INTERFACE_KEY, CommonConstants.ANY_VALUE,
            CommonConstants.GROUP_KEY, CommonConstants.ANY_VALUE,
            CommonConstants.VERSION_KEY, CommonConstants.ANY_VALUE,
            CommonConstants.CLASSIFIER_KEY, CommonConstants.ANY_VALUE,
            RegistryConstants.CATEGORY_KEY, RegistryConstants.PROVIDERS_CATEGORY,
//            Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY + ","
//            + Constants.CONSUMERS_CATEGORY + ","
//            + Constants.ROUTERS_CATEGORY + ","
//            + Constants.CONFIGURATORS_CATEGORY,
            CommonConstants.ENABLED_KEY, CommonConstants.ANY_VALUE,
            CommonConstants.CHECK_KEY, String.valueOf(false));

    // ConcurrentMap<category, ConcurrentMap<servicename, Map<MD5, URL>>>
    private final ConcurrentMap<String, ConcurrentMap<String, Map<String, URL>>>
            registryCache = new ConcurrentHashMap<>();
    /**
     * Make sure ID never changed when the same url notified many times
     */
    private final ConcurrentHashMap<String, String> URL_IDS_MAPPER = new ConcurrentHashMap<>();

    public RegistryServerSync() {
    }

    public ConcurrentMap<String, ConcurrentMap<String, Map<String, URL>>> getRegistryCache() {
        return registryCache;
    }

    @Override
    public void notify(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        // Map<category, Map<servicename, Map<Long, URL>>>
        final Map<String, Map<String, Map<String, URL>>> categories = new HashMap<>();
        String interfaceName = null;
        for (URL url : urls) {
            String category = url.getParameter(RegistryConstants.CATEGORY_KEY, RegistryConstants.PROVIDERS_CATEGORY);
            if (RegistryConstants.EMPTY_PROTOCOL.equalsIgnoreCase(url.getProtocol())) { // NOTE: group and version in empty protocol is *
                ConcurrentMap<String, Map<String, URL>> services = registryCache.get(category);
                if (services != null) {
                    String group = url.getParameter(CommonConstants.GROUP_KEY);
                    String version = url.getParameter(CommonConstants.VERSION_KEY);
                    // NOTE: group and version in empty protocol is *
                    if (!CommonConstants.ANY_VALUE.equals(group) && !CommonConstants.ANY_VALUE.equals(version)) {
                        services.remove(url.getServiceKey());
                    } else {
                        for (Map.Entry<String, Map<String, URL>> serviceEntry : services.entrySet()) {
                            String service = serviceEntry.getKey();
                            if (this.getInterface(service).equals(url.getServiceInterface())
                                    && (CommonConstants.ANY_VALUE.equals(group) || StringUtils.isEquals(group, this.getGroup(service)))
                                    && (CommonConstants.ANY_VALUE.equals(version) || StringUtils.isEquals(version, this.getVersion(service)))) {
                                services.remove(service);
                            }
                        }
                    }
                }
            } else {
                if (StringUtils.isEmpty(interfaceName)) {
                    interfaceName = url.getServiceInterface();
                }
                Map<String, Map<String, URL>> services = categories.get(category);
                if (services == null) {
                    services = new HashMap<>();
                    categories.put(category, services);
                }
                String service = url.getServiceKey();
                Map<String, URL> ids = services.get(service);
                if (ids == null) {
                    ids = new HashMap<>();
                    services.put(service, ids);
                }

                // Make sure we use the same ID for the same URL
                if (URL_IDS_MAPPER.containsKey(url.toFullString())) {
                    ids.put(URL_IDS_MAPPER.get(url.toFullString()), url);
                } else {
                    String md5 = MD5Util.MD5_16bit(url.toFullString());
                    ids.put(md5, url);
                    URL_IDS_MAPPER.putIfAbsent(url.toFullString(), md5);
                }
            }
        }
        if (categories.size() == 0) {
            return;
        }
        for (Map.Entry<String, Map<String, Map<String, URL>>> categoryEntry : categories.entrySet()) {
            String category = categoryEntry.getKey();
            ConcurrentMap<String, Map<String, URL>> services = registryCache.get(category);
            if (services == null) {
                services = new ConcurrentHashMap<String, Map<String, URL>>();
                registryCache.put(category, services);
            } else {// Fix map can not be cleared when service is unregistered: when a unique “group/service:version” service is unregistered, but we still have the same services with different version or group, so empty protocols can not be invoked.
                Set<String> keys = new HashSet<String>(services.keySet());
                for (String key : keys) {
                    if (this.getInterface(key).equals(interfaceName) && !categoryEntry.getValue().entrySet().contains(key)) {
                        services.remove(key);
                    }
                }
            }
            services.putAll(categoryEntry.getValue());
        }
    }

    public String getInterface(String service) {
        if (service != null && service.length() > 0) {
            int i = service.indexOf('/');
            if (i >= 0) {
                service = service.substring(i + 1);
            }
            i = service.lastIndexOf(':');
            if (i >= 0) {
                service = service.substring(0, i);
            }
        }
        return service;
    }

    public String getGroup(String service) {
        if (service != null && service.length() > 0) {
            int i = service.indexOf('/');
            if (i >= 0) {
                return service.substring(0, i);
            }
        }
        return null;
    }

    public String getVersion(String service) {
        if (service != null && service.length() > 0) {
            int i = service.lastIndexOf(':');
            if (i >= 0) {
                return service.substring(i + 1);
            }
        }
        return null;
    }
}
