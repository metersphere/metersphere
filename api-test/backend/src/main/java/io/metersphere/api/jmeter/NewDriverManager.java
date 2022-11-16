package io.metersphere.api.jmeter;

import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataExample;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.commons.utils.ApiFileUtil;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.metadata.service.FileMetadataService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewDriverManager {

    public static List<String> getJars(List<String> projectIds) {
        FileMetadataExample fileMetadata = new FileMetadataExample();
        fileMetadata.createCriteria().andProjectIdIn(projectIds).andLoadJarEqualTo(true);
        FileMetadataMapper fileMetadataMapper = CommonBeanFactory.getBean(FileMetadataMapper.class);
        List<FileMetadata> files = fileMetadataMapper.selectByExample(fileMetadata);
        files = files.stream().filter(s -> StringUtils.isNotEmpty(s.getPath())).collect(Collectors.toList());
        // 获取文件内容
        FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
        List<FileInfoDTO> fileInfoDTOS = fileMetadataService.downloadFileByIds(files.stream().map(FileMetadata::getId).collect(Collectors.toList()));
        ApiFileUtil.createFiles(fileInfoDTOS);
        return files.stream().map(FileMetadata::getPath).collect(Collectors.toList());
    }

    public static List<String> loadJar(RunDefinitionRequest request) {
        // 加载自定义JAR
        MsTestPlan testPlan = (MsTestPlan) request.getTestElement();
        List<String> projectIds = new ArrayList<>();
        projectIds.add(request.getProjectId());
        if (MapUtils.isNotEmpty(request.getEnvironmentMap())) {
            request.getEnvironmentMap().forEach((k, v) -> {
                if (!projectIds.contains(k)) {
                    projectIds.add(k);
                }
            });
        }
        testPlan.setJarPaths(getJars(projectIds));
        return projectIds;
    }
}