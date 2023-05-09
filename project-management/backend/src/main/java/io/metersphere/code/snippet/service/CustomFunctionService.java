package io.metersphere.code.snippet.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.CustomFunctionMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.base.mapper.ext.ExtCustomFunctionMapper;
import io.metersphere.code.snippet.listener.MsDebugListener;
import io.metersphere.code.snippet.util.ProjectFileUtils;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.dto.ProjectJarConfig;
import io.metersphere.jmeter.ProjectClassLoader;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.CustomFunctionRequest;
import io.metersphere.service.MicroService;
import io.metersphere.utils.JarConfigUtils;
import io.metersphere.utils.LocalPathUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFunctionService {


    @Resource
    private CustomFunctionMapper customFunctionMapper;
    @Resource
    private ExtCustomFunctionMapper extCustomFunctionMapper;
    @Resource
    private MicroService microService;

    public CustomFunctionWithBLOBs save(CustomFunctionRequest request) {
        request.setId(UUID.randomUUID().toString());
        request.setCreateUser(SessionUtils.getUserId());
        request.setProjectId(SessionUtils.getCurrentProjectId());

        checkFuncExist(request);

        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.insert(request);
        return request;
    }

    private void checkFuncExist(CustomFunctionRequest request) {
        String id = request.getId();
        String name = request.getName();
        CustomFunctionExample example = new CustomFunctionExample();
        CustomFunctionExample.Criteria criteria = example
                .createCriteria()
                .andProjectIdEqualTo(request.getProjectId())
                .andNameEqualTo(name);
        if (StringUtils.isNotBlank(id)) {
            criteria.andIdNotEqualTo(id);
        }
        if (customFunctionMapper.countByExample(example) > 0) {
            MSException.throwException("自定义函数名称已存在！");
        }
    }

    public void delete(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        customFunctionMapper.deleteByPrimaryKey(id);
    }

    public List<CustomFunction> query(CustomFunctionRequest request) {
        String projectId = request.getProjectId();
        if (StringUtils.isBlank(projectId)) {
            projectId = SessionUtils.getCurrentProjectId();
            request.setProjectId(projectId);
        }
        return extCustomFunctionMapper.queryAll(request);
    }

    public void update(CustomFunctionRequest request) {
        checkFuncExist(request);
        request.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.updateByPrimaryKeySelective(request);
    }

    public CustomFunctionWithBLOBs copy(String id) {
        CustomFunctionWithBLOBs blob = customFunctionMapper.selectByPrimaryKey(id);
        if (blob == null) {
            MSException.throwException("copy fail, source obj is null.");
        }
        CustomFunctionWithBLOBs copyBlob = new CustomFunctionWithBLOBs();
        BeanUtils.copyBean(copyBlob, blob);
        String copyId = UUID.randomUUID().toString();
        String copyNameId = copyId.substring(0, 3);
        String copyName = blob.getName() + "_" + copyNameId + "_COPY";
        copyBlob.setId(copyId);
        copyBlob.setName(copyName);
        copyBlob.setCreateUser(SessionUtils.getUserId());
        copyBlob.setCreateTime(System.currentTimeMillis());
        copyBlob.setUpdateTime(System.currentTimeMillis());
        customFunctionMapper.insert(copyBlob);
        return copyBlob;
    }

    public CustomFunctionWithBLOBs get(String id) {
        if (StringUtils.isBlank(id)) {
            return new CustomFunctionWithBLOBs();
        }
        return customFunctionMapper.selectByPrimaryKey(id);
    }

    public static Map<String, List<ProjectJarConfig>> getJars() {
        String currentProjectId = SessionUtils.getCurrentProjectId();
        FileMetadataExample fileMetadata = new FileMetadataExample();
        fileMetadata.createCriteria().andProjectIdEqualTo(currentProjectId).andLoadJarEqualTo(true);
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
        List<String> projectIds = new ArrayList<>();
        projectIds.add(currentProjectId);

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
                        FileUtils.createFile(StringUtils.join(LocalPathUtil.JAR_PATH,
                                File.separator,
                                key,
                                File.separator,
                                s.getId(),
                                File.separator,
                                String.valueOf(s.getUpdateTime()), ".jar"), bytes);
                    });
                    List<String> jarIds = value.stream().distinct().filter(s -> !s.isHasFile()).map(ProjectJarConfig::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(jarIds)) {
                        // 下载MinIOjar包
                        List<FileInfoDTO> fileInfoDTOS = fileMetadataService.downloadFileByIds(jarIds);
                        ProjectFileUtils.createFiles(fileInfoDTOS, key, value);
                    }
                }
            });
            // 初始化类加载器
            ProjectClassLoader.initClassLoader(loaderProjectIds);
        }
        return jarConfigMap;
    }

    public void run(Object request) {
        microService.postForData(MicroServiceName.API_TEST, "/api/definition/func/run", request);
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    private void addDebugListener(String reportId, HashTree testPlan) {
        MsDebugListener resultCollector = new MsDebugListener();
        resultCollector.setName(reportId);
        resultCollector.setProperty(TestElement.TEST_CLASS, MsDebugListener.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ViewResultsFullVisualizer"));
        resultCollector.setEnabled(true);

        // 添加DEBUG标示
        HashTree test = ArrayUtils.isNotEmpty(testPlan.getArray()) ? testPlan.getTree(testPlan.getArray()[0]) : null;
        if (test != null && ArrayUtils.isNotEmpty(test.getArray()) && test.getArray()[0] instanceof ThreadGroup) {
            ThreadGroup group = (ThreadGroup) test.getArray()[0];
            group.setName(reportId);
            group.setProperty(BackendListenerConstants.MS_DEBUG.name(), true);
        }
        testPlan.add(testPlan.getArray()[0], resultCollector);
    }
}
