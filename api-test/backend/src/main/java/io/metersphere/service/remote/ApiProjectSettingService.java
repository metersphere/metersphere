package io.metersphere.service.remote;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.MicroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiProjectSettingService {

    @Resource
    protected MicroService microService;

    /**
     * 转发到对应的服务
     *
     * @param url
     * @return
     */
    public Object get(String url) {
        try {
            return microService.getForData(MicroServiceName.PROJECT_MANAGEMENT, url);
        } catch (Exception e) {
            LogUtil.info("测试项目服务调用失败", e);
            return null;
        }
    }

    public Object post(String url, Object params) {
        try {
            return microService.postForData(MicroServiceName.PROJECT_MANAGEMENT, url, params);
        } catch (Exception e) {
            LogUtil.info("测试项目服务调用失败", e);
            return null;
        }
    }
}
