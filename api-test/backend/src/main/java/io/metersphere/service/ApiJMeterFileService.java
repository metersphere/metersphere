package io.metersphere.service;

import io.metersphere.api.dto.BodyFileRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.exec.api.ApiCaseSerialService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.FileInfoDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.BodyFile;
import io.metersphere.commons.utils.GenerateHashTreeUtil;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
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
public class ApiJMeterFileService {
    @Resource
    private ApiCaseSerialService apiCaseSerialService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiExecutionQueueDetailMapper executionQueueDetailMapper;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private PluginService pluginService;
    @Resource
    private FileMetadataService fileMetadataService;

    // 接口测试 用例/接口
    private static final List<String> CASE_MODES = new ArrayList<>() {{
        this.add(ApiRunMode.DEFINITION.name());
        this.add(ApiRunMode.JENKINS_API_PLAN.name());
        this.add(ApiRunMode.API_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_API_PLAN.name());
        this.add(ApiRunMode.MANUAL_PLAN.name());

    }};

    public byte[] downloadJmeterFiles(String runMode, String remoteTestId, String reportId, String reportType, String queueId) {
        Map<String, String> planEnvMap = new HashMap<>();
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(remoteTestId, reportId, runMode);
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
        if (detail != null) {
            runRequest.setRetryEnable(detail.getRetryEnable());
            runRequest.setRetryNum(detail.getRetryNumber());
        }
        Map<String, String> envMap = new LinkedHashMap<>();
        if (detail != null && StringUtils.isNotEmpty(detail.getEvnMap())) {
            envMap = JSON.parseObject(detail.getEvnMap(), Map.class);
        }
        if (MapUtils.isEmpty(envMap)) {
            LoggerUtil.info("测试资源：【" + remoteTestId + "】, 报告【" + reportId + "】未重新选择环境");
        }
        HashTree hashTree = null;
        if (CASE_MODES.contains(runMode)) {
            hashTree = apiCaseSerialService.generateHashTree(remoteTestId, envMap, runRequest);
        } else {
            if (scenario == null) {
                scenario = apiScenarioMapper.selectByPrimaryKey(remoteTestId);
            }
            if (scenario == null) {
                // 清除队列
                executionQueueDetailMapper.deleteByPrimaryKey(queueId);
            }
            if (MapUtils.isNotEmpty(envMap)) {
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
        Map<String, byte[]> jarFiles = this.getJar(null);
        if (!MapUtils.isEmpty(jarFiles)) {
            for (String k : jarFiles.keySet()) {
                byte[] v = jarFiles.get(k);
                files.put(k, v);
            }
        }
        return listBytesToZip(files);
    }

    public byte[] downloadJmeterJar(String projectId) {
        Map<String, byte[]> files = new HashMap<>();
        // 获取JAR
        Map<String, byte[]> jarFiles = this.getJar(projectId);
        if (!MapUtils.isEmpty(jarFiles)) {
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
        if (MapUtils.isNotEmpty(jarFiles)) {
            for (String k : jarFiles.keySet()) {
                byte[] v = jarFiles.get(k);
                files.put(k, v);
            }
        }
        return listBytesToZip(files);
    }

    private Map<String, byte[]> getJar(String projectId) {
        Map<String, byte[]> jarFiles = new LinkedHashMap<>();
        FileMetadataService jarConfigService = CommonBeanFactory.getBean(FileMetadataService.class);
        if (jarConfigService != null) {
            List<String> files = jarConfigService.getJar(new ArrayList<>() {{
                this.add(projectId);
            }});
            files.forEach(path -> {
                File file = new File(path);
                if (file.isDirectory() && !path.endsWith("/")) {
                    file = new File(path + "/");
                }
                byte[] fileByte = FileUtils.fileToByte(file);
                if (fileByte != null) {
                    jarFiles.put(file.getName(), fileByte);
                }
            });
            return jarFiles;
        } else {
            return new HashMap<>();
        }
    }

    private Map<String, byte[]> getPlugJar() {
        Map<String, byte[]> jarFiles = new LinkedHashMap<>();
        List<Plugin> plugins = pluginService.list();
        if (CollectionUtils.isNotEmpty(plugins)) {
            plugins = plugins.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                    new TreeSet<>(Comparator.comparing(Plugin::getPluginId))), ArrayList::new));
            plugins.forEach(item -> {
                File file = new File(item.getSourcePath());
                if (file.exists() && !file.isDirectory()) {
                    byte[] fileByte = FileUtils.fileToByte(file);
                    if (ArrayUtils.isNotEmpty(fileByte)) {
                        jarFiles.put(file.getName(), fileByte);
                    }
                }
            });

        }
        return jarFiles;
    }

    private Map<String, byte[]> getMultipartFiles(String reportId, HashTree hashTree) {
        Map<String, byte[]> multipartFiles = new LinkedHashMap<>();
        // 获取附件
        List<BodyFile> files = new LinkedList<>();
        FileUtils.getExecuteFiles(hashTree, reportId, files);
        if (CollectionUtils.isNotEmpty(files)) {
            Map<String, String> repositoryFileMap = new HashMap<>();
            for (BodyFile bodyFile : files) {
                if (StringUtils.equals(bodyFile.getStorage(), StorageConstants.GIT.name())
                        && StringUtils.isNotBlank(bodyFile.getFileId())) {
                    repositoryFileMap.put(bodyFile.getFileId(), bodyFile.getName());
                } else {
                    File file = new File(bodyFile.getName());
                    if (file != null && file.exists()) {
                        byte[] fileByte = FileUtils.fileToByte(file);
                        if (fileByte != null) {
                            multipartFiles.put(file.getAbsolutePath(), fileByte);
                        }
                    }
                }
            }
            List<FileInfoDTO> fileInfoDTOList = fileMetadataService.downloadFileByIds(repositoryFileMap.keySet());
            fileInfoDTOList.forEach(repositoryFile -> {
                if (repositoryFile.getFileByte() != null) {
                    multipartFiles.put(FileUtils.BODY_FILE_DIR + File.separator + repositoryFileMap.get(repositoryFile.getId()), repositoryFile.getFileByte());
                }
            });

        }
        return multipartFiles;
    }

    private String replaceJmx(String jmx) {
        jmx = StringUtils.replace(jmx, "<DubboSample", "<io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample");
        jmx = StringUtils.replace(jmx, "</DubboSample>", "</io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample>");
        jmx = StringUtils.replace(jmx, " guiclass=\"DubboSampleGui\" ", " guiclass=\"io.github.ningyu.jmeter.plugin.dubbo.gui.DubboSampleGui\" ");
        jmx = StringUtils.replace(jmx, " guiclass=\"DubboDefaultConfigGui\" ", " guiclass=\"io.github.ningyu.jmeter.plugin.dubbo.gui.DubboDefaultConfigGui\" ");
        jmx = StringUtils.replace(jmx, " testclass=\"DubboSample\" ", " testclass=\"io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample\" ");
        return jmx;
    }

    private byte[] zipFilesToByteArray(String testId, HashTree hashTree) {
        String bodyFilePath = FileUtils.BODY_FILE_DIR;
        String fileName = testId + ".jmx";

        // 获取JMX使用到的附件
        Map<String, byte[]> multipartFiles = this.getMultipartFiles(testId, hashTree);

        String jmx = new MsTestPlan().getJmx(hashTree);
        // 处理dubbo请求生成jmx文件
        if (StringUtils.isNotEmpty(jmx)) {
            jmx = replaceJmx(jmx);
        }
        Map<String, byte[]> files = new HashMap<>();
        //  每个测试生成一个文件夹
        files.put(fileName, jmx.getBytes(StandardCharsets.UTF_8));

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

    private byte[] listBytesToZip(Map<String, byte[]> content) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {
            for (String key : content.keySet()) {
                ZipEntry entry = new ZipEntry(key);
                entry.setSize(content.get(key).length);
                zos.putNextEntry(entry);
                zos.write(content.get(key));
            }
            zos.closeEntry();
            zos.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            LogUtil.error(e);
            return new byte[0];
        }
    }

    public byte[] zipFilesToByteArray(BodyFileRequest request) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(request.getBodyFiles())) {
            for (BodyFile bodyFile : request.getBodyFiles()) {
                File file = new File(bodyFile.getName());
                if (file != null && file.exists()) {
                    byte[] fileByte = FileUtils.fileToByte(file);
                    if (fileByte != null) {
                        files.put(file.getAbsolutePath(), fileByte);
                    }
                }
            }
        }
        return listBytesToZip(files);
    }

}
