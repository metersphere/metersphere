package io.metersphere.gateway.service;

import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.SystemParameterExample;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.gateway.dto.DisplayDTO;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseDisplayService {
    @Resource
    private SystemParameterMapper systemParameterMapper;
    @Resource
    private FileService fileService;

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public byte[] loadFileAsBytes(String fileId) {
        return fileService.loadFileAsBytes(fileId);
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
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/assets/favicon.ico").getInputStream());
                    contentType = MediaType.valueOf("image/vnd.microsoft.icon");
                }
                case "loginImage" ->
                        bytes = IOUtils.toByteArray(resolver.getResource("/static/assets/info.png").getInputStream());
                case "loginLogo" -> {
                    bytes = IOUtils.toByteArray(resolver.getResource("/static/assets/logo-dark-MeterSphere.svg").getInputStream());
                    contentType = MediaType.valueOf("image/svg+xml");
                }
                case "css" -> bytes = new byte[0];
                default -> {
                }
            }
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .body(bytes);
    }

    public List<DisplayDTO> uiInfo(String type) {
        List<SystemParameter> paramList = this.getParamList(type);
        List<DisplayDTO> dtoList = new ArrayList<>();
        for (SystemParameter systemParameter : paramList) {
            DisplayDTO displayDTO = new DisplayDTO();
            BeanUtils.copyBean(displayDTO, systemParameter);
            if (systemParameter.getType().equalsIgnoreCase("file")) {
                FileMetadata fileMetadata = fileService.getFileMetadataById(displayDTO.getParamValue());
                if (fileMetadata != null) {
                    String[] split = Objects.requireNonNull(fileMetadata.getName()).split(",");
                    displayDTO.setFileName(split[1]);
                }
            }
            dtoList.add(displayDTO);
        }
        dtoList.sort(Comparator.comparingInt(SystemParameter::getSort));
        return dtoList;
    }
}
