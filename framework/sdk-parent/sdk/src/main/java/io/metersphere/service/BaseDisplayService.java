package io.metersphere.service;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.SystemParameterExample;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.metadata.service.FileMetadataService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseDisplayService {
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private FileMetadataService fileMetadataService;

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public byte[] loadFileAsBytes(String fileId) {
        return fileMetadataService.loadFileAsBytes(fileId);
    }

    public ResponseEntity<byte[]> getImage(String imageName) throws IOException {
        byte[] bytes = null;
        List<SystemParameter> paramList = getParamList("ui." + imageName);
        if (!CollectionUtils.isEmpty(paramList)) {
            SystemParameter sp = paramList.get(0);
            String paramValue = sp.getParamValue();
            if (StringUtils.isNotBlank(paramValue)) {
                bytes = loadFileAsBytes(paramValue);
            }
        }

        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        if (bytes == null) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            switch (imageName) {
                case "logo" -> {
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/assets/logo-light-MeterSphere.svg").getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                }
                case "loginImage" ->
                        bytes = IOUtils.toByteArray(resolver.getResource("/static/assets/info.png").getInputStream());
                case "loginLogo" -> {
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/assets/logo-dark-MeterSphere.svg").getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                }
                case "css" -> bytes = new byte[0];
                default -> {
                    // /display/file/logo-dark-MeterSphere.svg
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/assets/" + imageName).getInputStream());
                    if (StringUtils.endsWithIgnoreCase(imageName, ".svg")) {
                        contentType = MediaType.valueOf("image/svg+xml");
                    }
                }
            }
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .body(bytes);
    }
}
