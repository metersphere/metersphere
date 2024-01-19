package io.metersphere.api.service;

import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.service.CustomFunctionRunService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-15  15:01
 */
@Service
public class CustomFunctionRunServiceImpl implements CustomFunctionRunService {

    @Resource
    ApiExecuteService apiExecuteService;

    @Override
    public String run(CustomFunctionRunRequest runRequest) {
        return apiExecuteService.runScript(runRequest);
    }
}
