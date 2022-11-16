package io.metersphere.remote.service;

import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.vo.FileRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class PlatformPluginService {

    @Resource
    FileManagerService fileManagerService;

    public static final String DIR_PATH = "system/plugin";

    public void getPluginResource(String pluginId, String name, HttpServletResponse response) {
        FileRequest request = new FileRequest();
        request.setProjectId(DIR_PATH + "/" + pluginId);
        request.setFileName(name);
        request.setStorage(StorageConstants.MINIO.name());
        InputStream inputStream = fileManagerService.downloadFileAsStream(request);
        getImage(inputStream, response);
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
