package io.metersphere.service.definition;


import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.TriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.GenerateHashTreeUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.jmeter.ProjectClassLoader;
import io.metersphere.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionRunService {
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private SystemParameterService systemParameterService;

    public void run(Object req) {
        MsJSR223Processor request = JSON.parseObject(JSON.toJSONString(req), MsJSR223Processor.class);
        // 验证是否本地执行
        RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
        jMeterService.verifyPool(request.getProjectId(), runModeConfigDTO);
        try {
            HashTree hashTree = apiExecuteService.getHashTree(request);
            JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(request.getId(),
                    request.getId(), ApiRunMode.DEBUG.name(), TriggerMode.MANUAL.name(), hashTree);
            runRequest.setDebug(true);
            if (StringUtils.isNotEmpty(runModeConfigDTO.getResourcePoolId())) {
                runRequest.setPool(GenerateHashTreeUtil.isResourcePool(runModeConfigDTO.getResourcePoolId()));
                runRequest.setPoolId(runModeConfigDTO.getResourcePoolId());
                BaseSystemConfigDTO baseInfo = systemParameterService.getBaseInfo();
                runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, null));
            } else {
                // 加载自定义JAR
                ClassLoader loader = ProjectClassLoader.getClassLoader(new ArrayList<>() {{
                    this.add(request.getProjectId());
                }});
                Thread.currentThread().setContextClassLoader(loader);
            }
            // 开始执行
            jMeterService.run(runRequest);
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
        }
    }
}
