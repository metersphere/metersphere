package io.metersphere.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.HttpHeaderUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.handler.ResultHolder;
import io.metersphere.dto.UserDTO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Service
public class MicroService {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private LoadBalancerClient loadBalancerClient;

    /**
     * GET 方法
     *
     * @param serviceId 服务ID
     * @param url       请求url
     * @return ResultHolder
     */
    public ResultHolder getForResultHolder(String serviceId, String url) {
        HttpHeaders httpHeaders = HttpHeaderUtils.getHttpHeaders();
        return getForResultHolder(serviceId, url, httpHeaders);
    }

    private ResultHolder getForResultHolder(String serviceId, String url, HttpHeaders httpHeaders) {
        if (!validateServiceAndUrl(serviceId, url)) {
            MSException.throwException("serviceId和请求url都不能为空");
        }
        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<ResultHolder> entity = restTemplate.exchange(chooseService(serviceId, url), HttpMethod.GET, httpEntity, ResultHolder.class);
            return entity.getBody();
        } catch (Exception e) {
            if (e instanceof HttpServerErrorException) {
                this.handleHttpServerErrorException((HttpServerErrorException) e);
                return null;
            }
            String massage = "服务调用出错[serviceId:" + serviceId + ",url:" + url + "],错误信息:" + e.getMessage();
            LogUtil.error(massage, e);
            MSException.throwException(massage);
        }
        return null;
    }

    /**
     * 获取 object 对象
     * 少进行一次序列化
     */
    public Object getForData(String serviceId, String url) {
        return getForData(serviceId, url, HttpHeaderUtils.getHttpHeaders());
    }

    public Object getForData(String serviceId, String url, HttpHeaders httpHeaders) {
        return this.getForResultHolder(serviceId, url, httpHeaders).getData();
    }

    public <T> T getForData(String serviceId, String url, Class<T> clazz) {
        return getForData(serviceId, url, HttpHeaderUtils.getHttpHeaders(), clazz);
    }

    public <T> T getForData(String serviceId, String url, TypeReference<T> typeReference) {
        ResultHolder resultHolder = getForResultHolder(serviceId, url, HttpHeaderUtils.getHttpHeaders());
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), typeReference);
    }

    public <T> T getForData(String serviceId, String url, HttpHeaders httpHeaders, Class<T> clazz) {
        ResultHolder resultHolder = getForResultHolder(serviceId, url, httpHeaders);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), clazz);
    }

    public <T> List<T> getForDataArray(String serviceId, String url, Class<T> clazz) {
        return this.getForDataArray(serviceId, url, HttpHeaderUtils.getHttpHeaders(), clazz);
    }

    public <T> List<T> getForDataArray(String serviceId, String url, TypeReference<T> typeReference) {
        return this.getForDataArray(serviceId, url, HttpHeaderUtils.getHttpHeaders(), typeReference);
    }

    public <T> List<T> getForDataArray(String serviceId, String url, HttpHeaders httpHeaders, Class<T> clazz) {
        ResultHolder resultHolder = getForResultHolder(serviceId, url, httpHeaders);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), clazz);
    }

    public <T> List<T> getForDataArray(String serviceId, String url, HttpHeaders httpHeaders, TypeReference<T> typeReference) {
        ResultHolder resultHolder = getForResultHolder(serviceId, url, httpHeaders);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), typeReference);
    }

    public MicroService runAsUser(UserDTO user) {
        HttpHeaderUtils.runAsUser(user);
        return this;
    }

    /**
     * 批量GET
     *
     * @param serviceIds 服务list
     * @param url        请求url
     * @return List<ResultHolder>
     */
    public List<ResultHolder> getForResultHolder(List<String> serviceIds, String url, long... timeoutSeconds) {
        if (CollectionUtils.isEmpty(serviceIds)) {
            return new ArrayList<>();
        }
        HttpHeaders httpHeaders = HttpHeaderUtils.getHttpHeaders();
        List<Callable<ResultHolder>> callableList = new ArrayList<>();
        for (String serviceId : serviceIds) {
            callableList.add(() -> getForResultHolder(serviceId, url, httpHeaders));
        }

        return executeResultHolder(callableList, timeoutSeconds);
    }

    private List<ResultHolder> executeResultHolder(List<Callable<ResultHolder>> callableList, long... timeoutSeconds) {
        List<ResultHolder> resultList = new ArrayList<>();
        long timeout = 60L;
        if (ArrayUtils.isNotEmpty(timeoutSeconds)) {
            timeout = timeoutSeconds[0];
            if (timeout < 5) {
                timeout = 5;
            }
        }
        ExecutorService executorService = Executors.newFixedThreadPool(callableList.size());
        try {
            List<Future<ResultHolder>> futureList = executorService.invokeAll(callableList, timeout, TimeUnit.SECONDS);
            for (Future<ResultHolder> future : futureList) {
                try {
                    resultList.add(future.get());
                } catch (Exception e) {
                    LogUtil.error("服务调用，future.get出错，错误信息:" + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }
        return resultList;
    }

    /**
     * POST 方法
     *
     * @param serviceId 服务ID
     * @param url       请求url
     * @param param     请求参数
     * @return ResultHolder
     */
    public ResultHolder postForResultHolder(String serviceId, String url, Object param) {
        HttpHeaders httpHeaders = HttpHeaderUtils.getHttpHeaders();
        return postForResultHolder(serviceId, url, httpHeaders, param);
    }

    private ResultHolder postForResultHolder(String serviceId, String url, HttpHeaders httpHeaders, Object param) {
        if (!validateServiceAndUrl(serviceId, url)) {
            MSException.throwException("serviceId和请求url都不能为空");
        }

        try {
            HttpEntity<Object> httpEntity = new HttpEntity<>(param, httpHeaders);
            ResponseEntity<ResultHolder> entity = restTemplate.exchange(chooseService(serviceId, url), HttpMethod.POST, httpEntity, ResultHolder.class);
            return entity.getBody();
        } catch (Exception e) {
            if (e instanceof HttpServerErrorException) {
                this.handleHttpServerErrorException((HttpServerErrorException) e);
                return null;
            }
            String massage = "服务调用出错[serviceId:" + serviceId + ",url:" + url + "],错误信息:" + e.getMessage();
            LogUtil.error(massage, e);
            MSException.throwException(massage);
        }
        return null;
    }

    private void handleHttpServerErrorException(HttpServerErrorException e) {
        String resp = e.getResponseBodyAsString();
        LogUtil.error(resp, e);
        ResultHolder holder = null;
        try {
            holder = JSON.parseObject(resp, ResultHolder.class);
        } catch (Exception ex) {
            // nothing
        }
        if (holder != null) {
            MSException.throwException(holder.getMessage());
        } else {
            MSException.throwException(resp);
        }
    }

    /**
     * 获取 object 对象
     * 少进行一次序列化
     */
    public Object postForData(String serviceId, String url, Object param) {
        return postForData(serviceId, url, HttpHeaderUtils.getHttpHeaders(), param);
    }

    public Object postForData(String serviceId, String url, HttpHeaders httpHeaders, Object param) {
        return this.postForResultHolder(serviceId, url, httpHeaders, param).getData();
    }

    public <T> T postForData(String serviceId, String url, Object param, Class<T> clazz) {
        return postForData(serviceId, url, HttpHeaderUtils.getHttpHeaders(), param, clazz);
    }

    public <T> T postForData(String serviceId, String url, Object param, TypeReference<T> typeReference) {
        ResultHolder resultHolder = postForResultHolder(serviceId, url, HttpHeaderUtils.getHttpHeaders(), param);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), typeReference);
    }

    public <T> T postForData(String serviceId, String url, HttpHeaders httpHeaders, Object param, Class<T> clazz) {
        ResultHolder resultHolder = postForResultHolder(serviceId, url, httpHeaders, param);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), clazz);
    }

    public <T> List<T> postForDataArray(String serviceId, String url, Object param, Class<T> clazz) {
        return this.postForDataArray(serviceId, url, HttpHeaderUtils.getHttpHeaders(), param, clazz);
    }

    public <T> List<T> postForDataArray(String serviceId, String url, Object param, TypeReference<T> typeReference) {
        return this.postForDataArray(serviceId, url, HttpHeaderUtils.getHttpHeaders(), param, typeReference);
    }

    public <T> List<T> postForDataArray(String serviceId, String url, HttpHeaders httpHeaders, Object param, Class<T> clazz) {
        ResultHolder resultHolder = postForResultHolder(serviceId, url, httpHeaders, param);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), clazz);
    }

    public <T> List<T> postForDataArray(String serviceId, String url, HttpHeaders httpHeaders, Object param, TypeReference<T> typeReference) {
        ResultHolder resultHolder = postForResultHolder(serviceId, url, httpHeaders, param);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), typeReference);
    }

    /**
     * POST 批量调用
     *
     * @param serviceIds 服务ID list
     * @param url        请求url
     * @param param      参数
     * @return List<ResultHolder>
     */
    public List<ResultHolder> postForResultHolder(List<String> serviceIds, String url, Object param, long... timeoutSeconds) {
        if (CollectionUtils.isEmpty(serviceIds)) {
            return new ArrayList<>();
        }
        HttpHeaders httpHeaders = HttpHeaderUtils.getHttpHeaders();
        List<Callable<ResultHolder>> callableList = new ArrayList<>();
        for (String serviceId : serviceIds) {
            callableList.add(() -> postForResultHolder(serviceId, url, httpHeaders, param));
        }
        return executeResultHolder(callableList, timeoutSeconds);
    }

    private String chooseService(String serviceId, String url) {
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        if (serviceInstance == null) {
            throw new RuntimeException("No available instance for service: " + serviceId);
        }
        return serviceInstance.getUri().toString() + url;
    }

    private boolean validateServiceAndUrl(String serviceId, String url) {
        return !StringUtils.isBlank(serviceId) && !StringUtils.isBlank(url);
    }

}
