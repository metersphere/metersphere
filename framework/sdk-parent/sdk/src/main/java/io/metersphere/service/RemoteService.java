package io.metersphere.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
        return Optional.ofNullable(microService.getForData(serviceName, url)).orElse(StringUtils.EMPTY);
    }

    /**
     * 转发到对应的服务
     * @param url
     * @return
     */
    public Object post(String url, Object param) {
        return Optional.ofNullable(microService.postForData(serviceName, url, param)).orElse(StringUtils.EMPTY);
    }

    public Object get(HttpServletRequest request) {
        // 返回null，前端会报错
        return Optional.ofNullable(microService.getForData(serviceName, wrapperQuery(request))).orElse(StringUtils.EMPTY);
    }


    public Object post(HttpServletRequest request, Object param) {
        // 返回null，前端会报错
        return Optional.ofNullable(microService.postForData(serviceName, wrapperQuery(request), param)).orElse(StringUtils.EMPTY);
    }

    private String wrapperQuery(HttpServletRequest request) {
        String url = request.getRequestURI();
        if (StringUtils.isNotBlank(request.getQueryString())) {
            url += "?" + request.getQueryString();
        }
        return url;
    }
}
