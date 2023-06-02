package io.metersphere.sdk.config;

import io.metersphere.sdk.dto.UserRoleJson;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.PermissionCache;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Configuration
public class PermissionConfig {
    @Bean
    public PermissionCache permissionCache() throws Exception {
        LogUtils.info("load permission form permission.json file");
        UserRoleJson userRoleJson = null;
        Enumeration<URL> urls = this.getClass().getClassLoader().getResources("permission.json");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String content = IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
            UserRoleJson temp = JSON.parseObject(content, UserRoleJson.class);
            if (userRoleJson == null) {
                userRoleJson = temp;
            } else {
                userRoleJson.getResource().addAll(temp.getResource());
                userRoleJson.getPermissions().addAll(temp.getPermissions());
            }

        }
        PermissionCache permissionCache = new PermissionCache();
        permissionCache.setUserRoleJson(userRoleJson);

        return permissionCache;
    }
}
