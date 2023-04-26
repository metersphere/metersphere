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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NewDriverManager {

    public static Map<String, List<ProjectJarConfig>> getJars(List<String> projectIds, BooleanPool pool) {
        FileMetadataExample fileMetadata = new FileMetadataExample();
        fileMetadata.createCriteria().andProjectIdIn(projectIds).andLoadJarEqualTo(true);
        FileMetadataMapper fileMetadataMapper = CommonBeanFactory.getBean(FileMetadataMapper.class);
        List<FileMetadataWithBLOBs> files = fileMetadataMapper.selectByExampleWithBLOBs(fileMetadata);

        Map<String, List<ProjectJarConfig>> jarConfigMap = files.stream()
                .collect(Collectors.groupingBy(FileMetadata::getProjectId, Collectors.mapping(
                        item -> {
                            ProjectJarConfig configs = new ProjectJarConfig();
                            configs.setId(item.getId());
                            configs.setName(item.getName());
                            configs.setStorage(item.getStorage());
                            //历史数据(存在数据库)
                            if (StringUtils.isEmpty(item.getStorage()) && StringUtils.isEmpty(item.getResourceType())) {
                                configs.setHasFile(true);
                            } else {
                                configs.setHasFile(false);
                            }
                            configs.setUpdateTime(item.getUpdateTime());
                            if (StringUtils.isNotEmpty(item.getStorage()) && StringUtils.equals(item.getStorage(), StorageConstants.GIT.name())) {
                                configs.setAttachInfo(item.getAttachInfo());
                            }
                            return configs;
                        }, Collectors.toList())));
        if (!pool.isPool() && !pool.isK8s()) {
            //获取本地
            //获取需要下载的jar的map
            Map<String, List<ProjectJarConfig>> map = JarConfigUtils.getJarConfigs(projectIds, jarConfigMap);
            if (MapUtils.isNotEmpty(map)) {
                //获取文件内容
                List<String> loaderProjectIds = new ArrayList<>();

                FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
                map.forEach((key, value) -> {
                    loaderProjectIds.add(key);
                    if (CollectionUtils.isNotEmpty(value)) {
                        //历史数据
                        value.stream().distinct().filter(s -> s.isHasFile()).forEach(s -> {
                            //获取文件内容
                            byte[] bytes = new byte[0];
                            // 兼容历史数据
                            bytes = fileMetadataService.getContent(s.getId());
                            ApiFileUtil.createFile(StringUtils.join(LocalPathUtil.JAR_PATH,
                                    File.separator,
                                    key,
                                    File.separator,
                                    s.getId(),
                                    File.separator,
                                    String.valueOf(s.getUpdateTime()), ".jar"), bytes);
                        });
                        List<String> jarIds = value.stream().distinct().filter(s -> !s.isHasFile()).map(ProjectJarConfig::getId).collect(Collectors.toList());
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

    public static Map<String, List<ProjectJarConfig>> loadJar(RunDefinitionRequest request, BooleanPool pool) {
        // 加载自定义JAR
        MsTestPlan testPlan = (MsTestPlan) request.getTestElement();
        List<String> projectIds = getProjectIds(request);
        Map<String, List<ProjectJarConfig>> jars = getJars(projectIds, pool);
        testPlan.setProjectJarIds(projectIds);

        return jars;
    }

    public static List<String> getProjectIds(RunDefinitionRequest request) {
        List<String> projectIds = new ArrayList<>();
        projectIds.add(request.getProjectId());
        if (MapUtils.isNotEmpty(request.getEnvironmentMap())) {
            request.getEnvironmentMap().forEach((k, v) -> {
                if (!projectIds.contains(k)) {
                    projectIds.add(k);
                }
            });
        }
        return projectIds;
    }
}
