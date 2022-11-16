package io.metersphere.remote.service;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.BasePluginService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class PlatformPluginService {
    @Resource
    BasePluginService basePluginService;

    public void getPluginResource(String pluginId, String name, HttpServletResponse response) {
        getImage(basePluginService.getPluginResource(pluginId, name), response);
    }

    public void getImage(InputStream in, HttpServletResponse response) {
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
            out.write(in.readAllBytes());
            out.flush();
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LogUtil.error(e);
            }
        }
    }

}
