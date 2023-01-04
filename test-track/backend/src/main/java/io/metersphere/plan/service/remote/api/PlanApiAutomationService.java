package io.metersphere.plan.service.remote.api;

import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.plan.request.api.RunScenarioRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlanApiAutomationService extends ApiTestService {

    private static final String BASE_UEL = "/api/automation";


    public List<MsExecResponseDTO> run(RunScenarioRequest request) {
        try {
            return microService.postForDataArray(serviceName, BASE_UEL + "/plan/run", request, MsExecResponseDTO.class);
        } catch (Exception e) {
            LogUtil.error("调用API服务执行场景用例失败", e);
            return new ArrayList<>();
        }
    }

    public ApiScenarioWithBLOBs get(@PathVariable String id) {
        return microService.getForData(serviceName, BASE_UEL + "/get/" + id, ApiScenarioWithBLOBs.class);
    }
}
