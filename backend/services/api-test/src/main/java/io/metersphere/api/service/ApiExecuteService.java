package io.metersphere.api.service;

import io.metersphere.api.config.JmeterProperties;
import io.metersphere.api.config.KafkaConfig;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.parser.TestElementParser;
import io.metersphere.api.parser.TestElementParserFactory;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.config.MinioProperties;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.service.SystemParameterService;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.system.utils.TaskRunnerClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.metersphere.api.controller.result.ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiExecuteService {
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private TestResourcePoolService testResourcePoolService;
    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JmeterProperties jmeterProperties;

    @PostConstruct
    private void init() {
        String jmeterHome = getJmeterHome();
        JMeterUtils.loadJMeterProperties(jmeterHome + "/bin/jmeter.properties");
        JMeterUtils.setJMeterHome(jmeterHome);
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

    public String getScriptRedisKey(String reportId, String testId) {
        return reportId + "_" + testId;
    }

    public void debug(ApiResourceRunRequest request) {
        TestResourceDTO resourcePoolDTO = getAvailableResourcePoolDTO(request.getProjectId());
        String reportId = request.getReportId();
         String testId = request.getTestId();

        TaskRequest taskRequest = new TaskRequest();
        BeanUtils.copyBean(taskRequest, request);

        String msUrl = systemParameterService.getBaseInfo().getUrl();
        taskRequest.setKafkaConfig(EncryptUtils.aesEncrypt(JSON.toJSONString(KafkaConfig.getKafkaConfig())));
        taskRequest.setMinioConfig(EncryptUtils.aesEncrypt(JSON.toJSONString(getMinio())));
        taskRequest.setMsUrl(msUrl);
        taskRequest.setReportId(reportId);
        taskRequest.setRealTime(true);

//         todo 环境配置
//         EnvironmentRequest environmentRequest = environmentService.get(request.getEnvironmentId());
         // todo 误报
         // todo 获取接口插件和jar包
         // todo 处理公共脚本

        ParameterConfig parameterConfig = new ParameterConfig();
        parameterConfig.setReportId(reportId);
        String executeScript = parseExecuteScript(request.getRequest(), parameterConfig);

        // 将测试脚本缓存到 redis
        String scriptRedisKey = getScriptRedisKey(reportId, testId);
        stringRedisTemplate.opsForValue().set(scriptRedisKey, executeScript);

        List<TestResourceNodeDTO> nodesList = resourcePoolDTO.getNodesList();
        int index = new SecureRandom().nextInt(nodesList.size());
        TestResourceNodeDTO testResourceNodeDTO = nodesList.get(index);
        String endpoint = TaskRunnerClient.getEndpoint(testResourceNodeDTO.getIp(), testResourceNodeDTO.getPort());
        try {
            LogUtils.info(String.format("开始发送请求【 %s 】到 %s 节点执行", testId, endpoint), reportId);
            TaskRunnerClient.debugApi(endpoint, taskRequest);
        } catch (Exception e) {
            LogUtils.error(e);
            // 调用失败清理脚本
            stringRedisTemplate.delete(scriptRedisKey);
            throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
        }
    }

    /**
     * 生成执行脚本
     * @param testElementStr
     * @param msParameter
     * @return
     */
    private static String parseExecuteScript(String testElementStr, ParameterConfig msParameter) {
        // 解析生成脚本
        TestElementParser defaultParser = TestElementParserFactory.getDefaultParser();
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(testElementStr, AbstractMsTestElement.class);
        return defaultParser.parse(msTestElement, msParameter);
    }


    public static Map<String, String> getMinio() {
        MinioProperties minioProperties = CommonBeanFactory.getBean(MinioProperties.class);
        Map<String, String> minioPros = new HashMap<>();
        minioPros.put("endpoint", minioProperties.getEndpoint());
        minioPros.put("accessKey", minioProperties.getAccessKey());
        minioPros.put("secretKey", minioProperties.getSecretKey());
        return minioPros;
    }

    /**
     * 获取当前项目配置的接口默认资源池
     * @param projectId
     * @param
     */
    public TestResourceDTO getAvailableResourcePoolDTO(String projectId) {
        // 查询接口默认资源池
        ProjectApplication resourcePoolConfig = projectApplicationService.getByType(projectId, ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
        // 没有配置接口默认资源池
        if (resourcePoolConfig == null || StringUtils.isBlank(resourcePoolConfig.getTypeValue())) {
            throw new MSException(ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        }
        String resourcePoolId = StringUtils.isBlank(resourcePoolConfig.getTypeValue()) ? null : resourcePoolConfig.getTypeValue();
        TestResourcePool testResourcePool = testResourcePoolService.getTestResourcePool(resourcePoolId);
        if (testResourcePool == null ||
                // 资源池禁用
                !testResourcePool.getEnable() ||
                // 项目没有资源池权限
                !commonProjectService.validateProjectResourcePool(testResourcePool, projectId)) {
            throw new MSException(ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        }
        return testResourcePoolService.getTestResourceDTO(resourcePoolId);
    }
}