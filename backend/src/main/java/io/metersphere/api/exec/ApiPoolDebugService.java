package io.metersphere.api.exec;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.service.RedisTemplateService;
import io.metersphere.api.service.RemakeReportService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ExtendedParameter;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.*;
import io.metersphere.service.ProjectApplicationService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApiPoolDebugService {
    public static final String BASE_URL = "http://%s:%d";
    public static final String POOL = "POOL";

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private RedisTemplateService redisTemplateService;

    public void run(JmeterRunRequestDTO request) {
        try {
            if (request.isDebug() && !StringUtils.equalsAny(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
                request.getExtendedParameters().put(ExtendedParameter.SAVE_RESULT, true);
            } else if (!request.isDebug()) {
                request.getExtendedParameters().put(ExtendedParameter.SAVE_RESULT, true);
            }
            List<JvmInfoDTO> resources = GenerateHashTreeUtil.setPoolResource(request.getPoolId());
            String uri = null;
            int index = (int) (Math.random() * resources.size());
            String configuration = resources.get(index).getTestResource().getConfiguration();
            if (StringUtils.isNotEmpty(configuration)) {
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                uri = String.format(BASE_URL + "/jmeter/debug", node.getIp(), node.getPort());
            }
            if (StringUtils.isEmpty(uri)) {
                LoggerUtil.info("未获取到资源池，请检查配置【系统设置-系统-测试资源池】", request.getReportId());
                MSException.throwException("调用资源池执行失败，请检查资源池是否配置正常");
            }
            // 过程变量处理
            if (request.getHashTree() != null) {
                ElementUtil.coverArguments(request.getHashTree());
            }
            redisTemplateService.initDebug(request);
            request.setHashTree(null);
            LoggerUtil.info("开始发送请求【 " + request.getTestId() + " 】到 " + uri + " 节点执行", request.getReportId());
            ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
            if (result == null || !StringUtils.equals("SUCCESS", result.getBody())) {
                LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 到" + uri + " 节点执行失败", request.getReportId());
                LoggerUtil.info(result);
                MSException.throwException("调用资源池执行失败，请检查资源池是否配置正常");
            }
        } catch (Exception e) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.testEnded(request, e.getMessage());
            redisTemplateService.deleteDebug(request);
            LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 执行失败,进行数据回滚：", request.getReportId(), e);
            MSException.throwException("调用资源池执行失败，请检查资源池是否配置正常");
        }
    }

    /**
     * 检查是否禁用了本地执行
     *
     * @param projectId
     * @param runConfig
     */
    public void verifyPool(String projectId, RunModeConfigDTO runConfig) {
        if (runConfig != null && StringUtils.isEmpty(runConfig.getResourcePoolId())) {
            LoggerUtil.info("校验项目为：【" + projectId + "】", runConfig.getReportId());
            ProjectConfig config = projectApplicationService.getProjectConfig(projectId);
            List<TestResourcePoolDTO> poolList = systemParameterService.getTestResourcePool();

            QuotaService baseQuotaService = CommonBeanFactory.getBean(QuotaService.class);
            Set<String> poolSets = baseQuotaService.getQuotaResourcePools();
            if (CollectionUtils.isNotEmpty(poolSets)) {
                poolList = poolList.stream().filter(pool -> poolSets.contains(pool.getId())).collect(Collectors.toList());
            }

            if (CollectionUtils.isEmpty(poolList)) {
                MSException.throwException("请在【项目设置-应用管理-接口测试】中选择资源池");
            }

            boolean contains = poolList.stream().map(TestResourcePoolDTO::getId)
                    .collect(Collectors.toList()).contains(config.getResourcePoolId()) && BooleanUtils.isTrue(config.getPoolEnable());

            if (!contains) {
                List<TestResourcePoolDTO> pools = poolList.stream().filter(pool ->
                        StringUtils.equals(pool.getName(), "LOCAL")).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(pools)) {
                    config.setResourcePoolId(pools.get(0).getId());
                } else {
                    config.setResourcePoolId(poolList.get(0).getId());
                }
            }
            runConfig = runConfig == null ? new RunModeConfigDTO() : runConfig;
            runConfig.setResourcePoolId(config.getResourcePoolId());
        }
    }

}
