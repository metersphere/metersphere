package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.service.BaseDisplayService;
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
public class DisplayImageService implements BaseDisplayService {

    @Resource
    private MinioRepository repository;
    @Resource
    private SystemParameterMapper systemParameterMapper;

    @Override
    public ResponseEntity<byte[]> getFile(String fileName) throws IOException {
        byte[] bytes = null;
        SystemParameter systemParameter = systemParameterMapper.selectByPrimaryKey("ui." + fileName);
        if (systemParameter != null) {
            FileRequest request = new FileRequest();
            request.setFileName(systemParameter.getParamValue());
            request.setProjectId("system");
            request.setResourceId(OperationLogModule.SYSTEM_PARAMETER_SETTING);
            try {
                bytes = repository.getFile(request);
            } catch (Exception e) {
                throw new MSException("get file error");
            }
        }

        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        if (bytes == null) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            switch (fileName) {
                case "icon":
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/favicon.ico").getInputStream());
                    break;
                case "logoPlatform":
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/svg/MS-full-logo.svg").getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                    break;
                case "loginImage":
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/images/login-banner.jpg").getInputStream());
                    contentType = MediaType.valueOf("image/jpeg");
                    break;
                default:
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/svg/login-logo.svg").getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                    break;
            }
        }
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

}
