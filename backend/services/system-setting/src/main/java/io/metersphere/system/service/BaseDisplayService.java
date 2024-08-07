package io.metersphere.system.service;

import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.mapper.SystemParameterMapper;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseDisplayService {

    @Resource
    private MinioRepository repository;
    @Resource
    private SystemParameterMapper systemParameterMapper;


    public ResponseEntity<byte[]> getFile(String fileName) throws IOException {
        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        byte[] bytes = null;
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey("ui." + fileName);
        if (systemParameter != null) {
            FileRequest request = new FileRequest();
            request.setFileName(systemParameter.getParamValue());
            request.setFolder(DefaultRepositoryDir.getSystemRootDir());
            try {
                bytes = repository.getFile(request);
            } catch (Exception e) {
                throw new MSException("get file error");
            }
            if (systemParameter.getParamValue().endsWith(".svg")) {
                contentType = MediaType.valueOf("image/svg+xml");
            }
        }

        if (bytes == null) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            switch (fileName) {
                case "icon" ->
                        bytes = IOUtils.toByteArray(resolver.getResource("/static/favicon.ico").getInputStream());
                case "logoPlatform" -> {
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/images/MeterSphere-logo.svg").getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                }
                case "loginImage" ->
                        bytes = IOUtils.toByteArray(resolver.getResource("/static/images/login-banner.jpg").getInputStream());
                default -> {
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/images/login-logo.svg").getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                }
            }
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

}
