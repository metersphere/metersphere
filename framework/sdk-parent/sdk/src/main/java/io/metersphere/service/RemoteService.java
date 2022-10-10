package io.metersphere.service;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class RemoteService {

    protected String serviceName;
    @Resource
    protected MicroService microService;

    public RemoteService(String serviceName) {
        this.serviceName = serviceName;
    }

    public RemoteService() {}

    /**
     * 转发到对应的服务
     * @param url
     * @return
     */
    public Object get(String url) {
        // 返回null，前端会报错
        return Optional.ofNullable(microService.getForData(serviceName, url)).orElse("");
    }

    /**
     * 转发到对应的服务
     * @param url
     * @return
     */
    public Object post(String url, Object param) {
        return Optional.ofNullable(microService.postForData(serviceName, url, param)).orElse("");
    }
}
