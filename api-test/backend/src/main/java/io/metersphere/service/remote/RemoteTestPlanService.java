package io.metersphere.service.remote;

import io.metersphere.api.dto.QueryTestPlanRequest;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.MicroService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class RemoteTestPlanService {
    @Resource
    protected MicroService microService;

    /**
     * 转发到对应的服务
     *
     * @param url
     * @return
     */
    public List get(String url) {
        try {
            return microService.getForData(MicroServiceName.TEST_TRACK, url, List.class);
        } catch (Exception e) {
            LogUtil.info("测试计划服务调用失败", e);
            return new ArrayList();
        }
    }

    public List post(String url, QueryTestPlanRequest params) {
        try {
            return microService.postForDataArray(MicroServiceName.TEST_TRACK, url, params, TestPlan.class);
        } catch (Exception e) {
            LogUtil.info("测试计划服务调用失败", e);
            return new ArrayList();
        }
    }

    public Object planListAll(String url, int goPage, int pageSize, QueryTestPlanRequest request) {
        try {
            return microService.postForData(MicroServiceName.TEST_TRACK, url + String.format("/list/all/%s/%s", goPage, pageSize), request);
        } catch (Exception e) {
            LogUtil.info("测试计划服务调用失败", e);
            return new ArrayList();
        }
    }
}
