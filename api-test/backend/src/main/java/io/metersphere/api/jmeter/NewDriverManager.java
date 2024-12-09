package io.metersphere.api.jmeter;

import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataExample;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.ApiFileUtil;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.dto.ProjectJarConfig;
import io.metersphere.jmeter.ProjectClassLoader;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.utils.JarConfigUtils;
import io.metersphere.utils.LocalPathUtil;
import io.metersphere.vo.BooleanPool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class NewDriverManager {

    // 获取项目的 Jar 配置
    public static Map<String, List<ProjectJarConfig>> getJars(List<String> projectIds, BooleanPool pool) {
        // 获取 FileMetadataMapper 和 FileMetadataService Bean
        FileMetadataMapper fileMetadataMapper = CommonBeanFactory.getBean(FileMetadataMapper.class);
        FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);

        // 查询文件元数据
        FileMetadataExample fileMetadataExample = new FileMetadataExample();
        fileMetadataExample.createCriteria().andProjectIdIn(projectIds).andLoadJarEqualTo(true);
        List<FileMetadataWithBLOBs> files = fileMetadataMapper.selectByExampleWithBLOBs(fileMetadataExample);

        // 分组并映射为 ProjectJarConfig
        Map<String, List<ProjectJarConfig>> jarConfigMap = files.stream()
                .collect(Collectors.groupingBy(FileMetadata::getProjectId, Collectors.mapping(item -> {
                    ProjectJarConfig config = new ProjectJarConfig();
                    config.setId(item.getId());
                    config.setName(item.getName());
                    config.setStorage(item.getStorage());
                    config.setHasFile(StringUtils.isEmpty(item.getStorage()) && StringUtils.isEmpty(item.getResourceType()));
                    config.setUpdateTime(item.getUpdateTime());

                    if (StringUtils.equals(item.getStorage(), StorageConstants.GIT.name())) {
                        config.setAttachInfo(item.getAttachInfo());
                    }
                    return config;
                }, Collectors.toList())));

        // 如果不使用池或 K8s，下载需要的 JAR 文件
        if (!pool.isPool() && !pool.isK8s()) {
            // 获取本地需要下载的 JAR 文件配置
            Map<String, List<ProjectJarConfig>> map = JarConfigUtils.getJarConfigs(projectIds, jarConfigMap);

            if (MapUtils.isNotEmpty(map)) {
                List<String> loaderProjectIds = new ArrayList<>();

                map.forEach((key, value) -> {
                    loaderProjectIds.add(key);
                    if (CollectionUtils.isNotEmpty(value)) {
                        // 获取历史数据
                        value.stream()
                                .distinct()
                                .filter(ProjectJarConfig::isHasFile)
                                .forEach(s -> {
                                    // 获取文件内容并保存
                                    byte[] bytes = fileMetadataService.getContent(s.getId());
                                    ApiFileUtil.createFile(
                                            StringUtils.join(LocalPathUtil.JAR_PATH, File.separator, key, File.separator,
                                                    s.getId(), File.separator, s.getUpdateTime(), ".jar"), bytes);
                                });

                        // 获取需要下载的文件
                        List<String> jarIds = value.stream()
                                .distinct()
                                .filter(config -> !config.isHasFile())
                                .map(ProjectJarConfig::getId)
                                .collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(jarIds)) {
                            List<FileInfoDTO> fileInfoDTOS = fileMetadataService.downloadFileByIds(jarIds);
                            ApiFileUtil.createFiles(fileInfoDTOS, key, value);
                        }
                    }
                });

                // 初始化类加载器
                ProjectClassLoader.initClassLoader(loaderProjectIds);
            }
        }

        return jarConfigMap;
    }

    // 加载自定义 JAR
    public static Map<String, List<ProjectJarConfig>> loadJar(RunDefinitionRequest request, BooleanPool pool) {
        MsTestPlan testPlan = (MsTestPlan) request.getTestElement();
        List<String> projectIds = getProjectIds(request);
        Map<String, List<ProjectJarConfig>> jars = getJars(projectIds, pool);
        testPlan.setProjectJarIds(projectIds);
        return jars;
    }

    // 获取项目 ID 列表
    public static List<String> getProjectIds(RunDefinitionRequest request) {
        Set<String> projectIds = new HashSet<>();
        projectIds.add(request.getProjectId());

        if (MapUtils.isNotEmpty(request.getEnvironmentMap())) {
            projectIds.addAll(request.getEnvironmentMap().keySet());
        }

        return new ArrayList<>(projectIds);
    }
}
