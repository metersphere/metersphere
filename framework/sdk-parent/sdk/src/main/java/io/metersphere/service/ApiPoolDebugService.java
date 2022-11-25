package io.metersphere.service;

import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.*;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ApiPoolDebugService {
    public static final String BASE_URL = "http://%s:%d";
    public static final String POOL = "POOL";

    @Resource
    private RestTemplate restTemplate;
    public static final String SAVE_RESULT = "SAVE_RESULT";
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;

    public void run(JmeterRunRequestDTO request, List<TestResource> resources) throws MSException {
        try {
            if (request.isDebug() && !StringUtils.equalsAny(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
                request.getExtendedParameters().put(SAVE_RESULT, true);
            } else if (!request.isDebug()) {
                request.getExtendedParameters().put(SAVE_RESULT, true);
            }
            String uri = null;
            int index = (int) (Math.random() * resources.size());
            String configuration = resources.get(index).getConfiguration();
            if (StringUtils.isNotEmpty(configuration)) {
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                uri = String.format(BASE_URL + "/jmeter/debug", node.getIp(), node.getPort());
            }
            if (StringUtils.isEmpty(uri)) {
                LoggerUtil.info("url为空", request.getReportId());
                MSException.throwException("请在【项目设置-应用管理-接口测试】中选择资源池");
            }
            LoggerUtil.info("开始发送请求【 " + request.getTestId() + " 】到 " + uri + " 节点执行", request.getReportId());
            ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
            if (result == null || !StringUtils.equals("SUCCESS", result.getBody())) {
                LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 到" + uri + " 节点执行失败", request.getReportId());
                LoggerUtil.info(result);
                MSException.throwException("请在【项目设置-应用管理-接口测试】中选择资源池");
            }
        } catch (Exception e) {
            LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 执行失败,进行数据回滚：", request.getReportId(), e);
            MSException.throwException("请在【项目设置-应用管理-接口测试】中选择资源池");
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
            BaseSystemConfigDTO configDTO = systemParameterService.getBaseInfo();
            LoggerUtil.info("校验项目为：【" + projectId + "】", runConfig.getReportId());
            if (StringUtils.equals(configDTO.getRunMode(), POOL)) {
                ProjectConfig config = baseProjectApplicationService.getProjectConfig(projectId);
                if (config == null || !config.getPoolEnable() || StringUtils.isEmpty(config.getResourcePoolId())) {
                    MSException.throwException("请在【项目设置-应用管理-接口测试】中选择资源池");
                }
                runConfig = runConfig == null ? new RunModeConfigDTO() : runConfig;
                runConfig.setResourcePoolId(config.getResourcePoolId());
            }
        }
    }
}
