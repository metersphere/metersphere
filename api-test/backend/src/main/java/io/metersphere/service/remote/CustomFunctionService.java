package io.metersphere.service.remote;


import io.metersphere.api.dto.CustomFunctionRequest;
import io.metersphere.base.domain.CustomFunction;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.MicroService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CustomFunctionService {
    @Resource
    protected MicroService microService;

    public Object get(String requestURI) {
        return microService.getForData(MicroServiceName.PROJECT_MANAGEMENT, requestURI);
    }

    public Object getPage(String requestURI, CustomFunctionRequest params) {
        return microService.postForDataArray(MicroServiceName.PROJECT_MANAGEMENT, requestURI, params, CustomFunction.class);
    }
}
