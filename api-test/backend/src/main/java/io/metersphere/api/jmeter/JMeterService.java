package io.metersphere.api.jmeter;


import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.exec.engine.EngineFactory;
import io.metersphere.api.jmeter.utils.JmxFileUtil;
import io.metersphere.api.jmeter.utils.ServerConfig;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.config.KafkaConfig;
import io.metersphere.commons.constants.ExtendedParameter;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.config.JmeterProperties;
import io.metersphere.dto.*;
import io.metersphere.engine.Engine;
import io.metersphere.service.ApiPoolDebugService;
import io.metersphere.service.PluginService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.service.RemakeReportService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

@Service
public class JMeterService {
    public static final String BASE_URL = "http://%s:%d";
    @Resource
    private JmeterProperties jmeterProperties;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedisTemplateService redisTemplateService;
    @Resource
    private RemakeReportService remakeReportService;
    @Resource
    private ApiPoolDebugService apiPoolDebugService;
    @Resource
    private PluginService pluginService;
    @Resource
    private ResourcePoolCalculation resourcePoolCalculation;

    @PostConstruct
    private void init() {
        String JMETER_HOME = getJmeterHome();
        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public String getJmeterHome() {
        String home = getClass().getResource("/").getPath() + "jmeter";
        try {
            File file = new File(home);
            if (file.exists()) {
                return home;
            } else {
                return jmeterProperties.getHome();
            }
        } catch (Exception e) {
            return jmeterProperties.getHome();
        }
    }

    private void fileProcessing(JmeterRunRequestDTO request) {
        ElementUtil.coverArguments(request.getHashTree());
        //解析HashTree里的文件信息
        List<AttachmentBodyFile> attachmentBodyFileList = ApiFileUtil.getExecuteFile(request.getHashTree(), request.getReportId(), false);
        if (CollectionUtils.isNotEmpty(attachmentBodyFileList)) {
            redisTemplateService.setIfAbsent(JmxFileUtil.getExecuteFileKeyInRedis(request.getReportId()),
                    JmxFileUtil.getRedisJmxFileString(attachmentBodyFileList));
        }
        redisTemplateService.setIfAbsent(JmxFileUtil.getExecuteScriptKey(request.getReportId(), request.getTestId()),
                new MsTestPlan().getJmx(request.getHashTree()));
    }

    private void runNode(JmeterRunRequestDTO request) {
        request.setKafkaConfig(KafkaConfig.getKafka());
        //获取MinIO配置和系统下的插件jar包
        PluginConfigDTO pluginConfigDTO = pluginService.getPluginConfig();
        if (pluginConfigDTO != null) {
            request.setPluginConfigDTO(pluginConfigDTO);
        }
        // 如果是K8S调用
        if (request.getPool().isK8s()) {
            try {
                // 缓存调试脚本
                if (request.getHashTree() != null) {
                    this.fileProcessing(request);
                }
                request.setCorePoolSize(resourcePoolCalculation.k8sMaxCoreSize(request.getPoolId()));
                LoggerUtil.info("开始发送请求[ " + request.getTestId() + " ] 到K8S节点执行", request.getReportId());
                final Engine engine = EngineFactory.createApiEngine(request);
                engine.start();
            } catch (Exception e) {
                remakeReportService.testEnded(request, e.getMessage());
                redisTemplateService.delFilePathAndScript(request.getReportId(), request.getTestId());
                LoggerUtil.error("调用K8S执行请求[ " + request.getTestId() + " ]失败：", request.getReportId(), e);
            }
        } else if ((MapUtils.isNotEmpty(request.getExtendedParameters())
                && request.getExtendedParameters().containsKey(ExtendedParameter.SYNC_STATUS)
                && (Boolean) request.getExtendedParameters().get(ExtendedParameter.SYNC_STATUS))
                || request.isDebug()) {
            this.nodeDebug(request);
        } else {
            this.send(request);
        }
    }

    private void nodeDebug(JmeterRunRequestDTO request) {
        try {
            LoggerUtil.info("获取调试资源池", request.getReportId());
            List<TestResource> resources = GenerateHashTreeUtil.setPoolResource(request.getPoolId());
            if (request.getHashTree() != null) {
                // 过程变量处理
                this.fileProcessing(request);
                request.setHashTree(null);
            }
            LoggerUtil.info("调用资源池开始执行", request.getReportId());
            apiPoolDebugService.run(request, resources);
        } catch (Exception e) {
            remakeReportService.updateReport(request, e.getMessage());
            redisTemplateService.delFilePathAndScript(request.getReportId(), request.getTestId());
            LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 执行失败,进行数据回滚：", request.getReportId(), e);
            MSException.throwException("调用资源池执行失败，请检查资源池是否配置正常");
        }
    }

    private void send(JmeterRunRequestDTO request) {
        try {
            if (redisTemplate.opsForValue().get(SmoothWeighted.EXEC_INDEX + request.getPoolId()) != null) {
                long index = Long.parseLong(redisTemplate.opsForValue().get(SmoothWeighted.EXEC_INDEX + request.getPoolId()).toString());
                redisTemplate.opsForValue().set(SmoothWeighted.EXEC_INDEX + request.getPoolId(), index + 1);
            }
            ServerConfig config = SmoothWeighted.calculate(request.getPoolId(), redisTemplate);
            if (config == null) {
                config = SmoothWeighted.getResource(request.getPoolId());
            }
            if (config == null) {
                String errorMsg = "未获取到资源池，请检查配置【系统设置-系统-测试资源池】";
                MSException.throwException(errorMsg);
            }
            request.setCorePoolSize(config.getCorePoolSize());
            request.setEnable(config.isEnable());
            LoggerUtil.info("开始发送请求【 " + request.getTestId() + " 】到 " + config.getUrl() + " 节点执行", request.getReportId());
            ResponseEntity<String> result = restTemplate.postForEntity(config.getUrl(), request, String.class);
            if (!StringUtils.equals("SUCCESS", result.getBody())) {
                LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 到" + config.getUrl() + " 节点执行失败", request.getReportId());
            }
        } catch (Exception e) {
            remakeReportService.testEnded(request, e.getMessage());
            redisTemplateService.delFilePath(request.getReportId());
            LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 执行失败,进行数据回滚：", request.getReportId(), e);
        }
    }


    public void run(JmeterRunRequestDTO request) {
        if (request.getPool().isPool() && StringUtils.isNotBlank(request.getRunMode())) {
            this.runNode(request);
        } else if (request.getHashTree() != null) {
            // 过程变量处理
            ElementUtil.coverArguments(request.getHashTree());
            //解析hashTree，是否含有文件库文件
            HashTreeUtil.initRepositoryFiles(request);
        }
    }

    public boolean getRunningQueue(String poolId, String reportId) {
        try {
            List<TestResource> resources = GenerateHashTreeUtil.setPoolResource(poolId);
            if (CollectionUtils.isEmpty(resources)) {
                return false;
            }
            boolean isRunning = false;
            for (TestResource testResource : resources) {
                String configuration = testResource.getConfiguration();
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                String nodeIp = node.getIp();
                Integer port = node.getPort();
                String uri = String.format(BASE_URL + "/jmeter/get/running/queue/" + reportId, nodeIp, port);
                ResponseEntity<Boolean> result = restTemplate.getForEntity(uri, Boolean.class);
                if (BooleanUtils.isTrue(result.getBody())) {
                    isRunning = true;
                    break;
                }
            }
            return isRunning;
        } catch (Exception e) {
            return false;
        }
    }

    public void verifyPool(String projectId, RunModeConfigDTO runModeConfigDTO) {
        apiPoolDebugService.verifyPool(projectId, runModeConfigDTO);
    }
}
