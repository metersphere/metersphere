package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.api.exec.scenario.ApiScenarioSerialService;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.service.JarConfigService;
import io.metersphere.service.PluginService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ApiJmeterFileService {

    @Resource
    private ApiScenarioSerialService apiScenarioSerialService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiExecutionQueueDetailMapper executionQueueDetailMapper;

    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;

    public byte[] downloadJmeterFiles(String runMode, String remoteTestId, String reportId, String reportType, String queueId) {
        Map<String, String> planEnvMap = new HashMap<>();
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(remoteTestId, reportId, runMode, null);
        runRequest.setReportType(reportType);
        runRequest.setQueueId(queueId);

        ApiScenarioWithBLOBs scenario = null;
        if (StringUtils.equalsAny(runMode, ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name())) {
            // 获取场景用例单独的执行环境
            TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(remoteTestId);
            if (planApiScenario != null) {
                String envType = planApiScenario.getEnvironmentType();
                String environmentGroupId = planApiScenario.getEnvironmentGroupId();
                String environment = planApiScenario.getEnvironment();
                if (StringUtils.equals(envType, EnvironmentType.JSON.name()) && StringUtils.isNotBlank(environment)) {
                    planEnvMap = JSON.parseObject(environment, Map.class);
                } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
                    planEnvMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                }
                scenario = apiScenarioMapper.selectByPrimaryKey(planApiScenario.getApiScenarioId());
            }
        }

        ApiExecutionQueueDetail detail = executionQueueDetailMapper.selectByPrimaryKey(queueId);
        if (detail == null) {
            ApiExecutionQueueDetailExample example = new ApiExecutionQueueDetailExample();
            example.createCriteria().andReportIdEqualTo(reportId);
            List<ApiExecutionQueueDetail> list = executionQueueDetailMapper.selectByExampleWithBLOBs(example);
            if (CollectionUtils.isNotEmpty(list)) {
                detail = list.get(0);
            }
        }
        Map<String, String> envMap = new LinkedHashMap<>();
        if (detail != null && StringUtils.isNotEmpty(detail.getEvnMap())) {
            envMap = JSON.parseObject(detail.getEvnMap(), Map.class);
        }
        if (MapUtils.isEmpty(envMap)) {
            LoggerUtil.info("测试资源：【" + remoteTestId + "】未找到可执行的环境 >>>>>>> ");
        }
        HashTree hashTree = null;
        if (StringUtils.equalsAnyIgnoreCase(runMode, ApiRunMode.DEFINITION.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
            hashTree = apiScenarioSerialService.generateHashTree(remoteTestId, envMap, runRequest);
        } else {
            if (scenario == null) {
                scenario = apiScenarioMapper.selectByPrimaryKey(remoteTestId);
            }
            if (scenario == null) {
                // 清除队列
                executionQueueDetailMapper.deleteByPrimaryKey(queueId);
            }
            if (envMap != null && !envMap.isEmpty()) {
                planEnvMap = envMap;
            } else if (!StringUtils.equalsAny(runMode, ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name())) {
                String envType = scenario.getEnvironmentType();
                String envJson = scenario.getEnvironmentJson();
                String envGroupId = scenario.getEnvironmentGroupId();
                if (StringUtils.equals(envType, EnvironmentType.JSON.name()) && StringUtils.isNotBlank(envJson)) {
                    planEnvMap = JSON.parseObject(envJson, Map.class);
                } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(envGroupId)) {
                    planEnvMap = environmentGroupProjectService.getEnvMap(envGroupId);
                }
            }
            hashTree = GenerateHashTreeUtil.generateHashTree(scenario, planEnvMap, runRequest);
        }
        return zipFilesToByteArray((reportId + "_" + remoteTestId), hashTree);
    }

    public byte[] downloadJmeterJar() {
        Map<String, byte[]> files = new HashMap<>();
        // 获取JAR
        Map<String, byte[]> jarFiles = this.getJar();
        if (!com.alibaba.excel.util.CollectionUtils.isEmpty(jarFiles)) {
            for (String k : jarFiles.keySet()) {
                byte[] v = jarFiles.get(k);
                files.put(k, v);
            }
        }
        return listBytesToZip(files);
    }

    public byte[] downloadPlugJar() {
        Map<String, byte[]> files = new HashMap<>();
        // 获取JAR
        Map<String, byte[]> jarFiles = this.getPlugJar();
        if (!com.alibaba.excel.util.CollectionUtils.isEmpty(jarFiles)) {
            for (String k : jarFiles.keySet()) {
                byte[] v = jarFiles.get(k);
                files.put(k, v);
            }
        }
        return listBytesToZip(files);
    }

    private Map<String, byte[]> getJar() {
        Map<String, byte[]> jarFiles = new LinkedHashMap<>();
        // jar 包
        JarConfigService jarConfigService = CommonBeanFactory.getBean(JarConfigService.class);
        List<JarConfig> jars = jarConfigService.list();
        jars.forEach(jarConfig -> {
            try {
                String path = jarConfig.getPath();
                File file = new File(path);
                if (file.isDirectory() && !path.endsWith("/")) {
                    file = new File(path + "/");
                }
                byte[] fileByte = FileUtils.fileToByte(file);
                if (fileByte != null) {
                    jarFiles.put(file.getName(), fileByte);
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
        return jarFiles;
    }

    private Map<String, byte[]> getPlugJar() {
        Map<String, byte[]> jarFiles = new LinkedHashMap<>();
        // jar 包
        PluginService pluginService = CommonBeanFactory.getBean(PluginService.class);

        List<Plugin> plugins = pluginService.list();
        if (CollectionUtils.isNotEmpty(plugins)) {
            plugins = plugins.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                    -> new TreeSet<>(Comparator.comparing(Plugin::getPluginId))), ArrayList::new));

            if (CollectionUtils.isNotEmpty(plugins)) {
                plugins.forEach(item -> {
                    String path = item.getSourcePath();
                    File file = new File(path);
                    if (file.isDirectory() && !path.endsWith("/")) {
                        file = new File(path + "/");
                    }
                    byte[] fileByte = FileUtils.fileToByte(file);
                    if (fileByte != null) {
                        jarFiles.put(file.getName(), fileByte);
                    }
                });
            }
        }
        return jarFiles;
    }

    private Map<String, byte[]> getMultipartFiles(HashTree hashTree) {
        Map<String, byte[]> multipartFiles = new LinkedHashMap<>();
        // 获取附件
        List<BodyFile> files = new LinkedList<>();
        FileUtils.getFiles(hashTree, files);
        if (CollectionUtils.isNotEmpty(files)) {
            for (BodyFile bodyFile : files) {
                File file = new File(bodyFile.getName());
                if (file != null && file.exists()) {
                    byte[] fileByte = FileUtils.fileToByte(file);
                    if (fileByte != null) {
                        multipartFiles.put(file.getAbsolutePath(), fileByte);
                    }
                }
            }
        }
        return multipartFiles;
    }

    private byte[] zipFilesToByteArray(String testId, HashTree hashTree) {
        String bodyFilePath = FileUtils.BODY_FILE_DIR;
        String fileName = testId + ".jmx";
        String jmx = new MsTestPlan().getJmx(hashTree);
        Map<String, byte[]> files = new HashMap<>();
        //  每个测试生成一个文件夹
        files.put(fileName, jmx.getBytes(StandardCharsets.UTF_8));
        // 获取JMX使用到的附件
        Map<String, byte[]> multipartFiles = this.getMultipartFiles(hashTree);
        if (multipartFiles != null && !multipartFiles.isEmpty()) {
            for (String k : multipartFiles.keySet()) {
                byte[] v = multipartFiles.get(k);
                if (k.startsWith(bodyFilePath)) {
                    files.put(StringUtils.substringAfter(k, bodyFilePath), v);
                } else {
                    LogUtil.error("WARNING:Attachment path is not in body_file_path: " + k);
                    files.put(k, v);
                }
            }
        }
        return listBytesToZip(files);
    }

    private byte[] listBytesToZip(Map<String, byte[]> mapReport) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            for (Map.Entry<String, byte[]> report : mapReport.entrySet()) {
                ZipEntry entry = new ZipEntry(report.getKey());
                entry.setSize(report.getValue().length);
                zos.putNextEntry(entry);
                zos.write(report.getValue());
            }
            zos.closeEntry();
            zos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
