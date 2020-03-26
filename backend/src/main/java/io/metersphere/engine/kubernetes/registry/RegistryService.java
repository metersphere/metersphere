package io.metersphere.engine.kubernetes.registry;


import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.SystemParameterExample;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.engine.kubernetes.provider.AbstractClientProvider;
import io.metersphere.engine.kubernetes.provider.DockerRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistryService {

    @Resource
    private SystemParameterMapper parameterMapper;

    /**
     * 获取镜像仓库地址
     * 地址+项目
     *
     * @return eg: registry.demo.com/demo/
     */
    public String getRegistry() {
        StringBuilder sb = new StringBuilder();
        Map<String, String> paramMap = getRegistryInfo();
        String url = paramMap.getOrDefault(ParamConstants.Registry.URL.getValue(), "");
        String project = paramMap.getOrDefault(ParamConstants.Registry.REPO.getValue(), "");
        if (url.startsWith("http://") || url.startsWith("https://")) {
            url = url.replace("http://", "");
            url = url.replace("https://", "");
        }
        sb.append(url);
        if (!url.endsWith("/")) {
            sb.append("/");
        }
        if (StringUtils.isNotEmpty(project)) {
            sb.append(project);
            sb.append("/");
        }
        return sb.toString();
    }

    public String getRegistryUrl() {
        Map<String, String> paramMap = getRegistryInfo();
        String url = paramMap.get(ParamConstants.Registry.URL.getValue());
        if (url.startsWith("http://") || url.startsWith("https://")) {
            url = url.replace("http://", "");
            url = url.replace("https://", "");
        }
        return url;
    }

    public String getRegistryUsername() {
        Map<String, String> paramMap = getRegistryInfo();
        return paramMap.get(ParamConstants.Registry.USERNAME.getValue());
    }

    public String getRegistryPassword() {
        Map<String, String> paramMap = getRegistryInfo();
        return paramMap.get(ParamConstants.Registry.PASSWORD.getValue());
    }

    public void dockerRegistry(AbstractClientProvider clientProvider, String namespace) {
        DockerRegistry registry = new DockerRegistry();
        registry.setUrl(this.getRegistryUrl());
        registry.setUsername(this.getRegistryUsername());
        registry.setPassword(this.getRegistryPassword());
        registry.setNamespace(namespace);
        clientProvider.dockerRegistry(registry);
    }

    private Map<String, String> getRegistryInfo() {
        Map<String, String> map = new HashMap<>();
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(ParamConstants.Classify.REGISTRY.getValue() + "%");
        List<SystemParameter> parameters = parameterMapper.selectByExample(example);
        for (SystemParameter parameter : parameters) {
            if (StringUtils.equalsIgnoreCase(ParamConstants.Type.PASSWORD.getValue(), parameter.getType())) {
                parameter.setParamValue(EncryptUtils.aesDecrypt(parameter.getParamValue()).toString());
            }
            parameters.forEach(param -> map.put(param.getParamKey(), param.getParamValue()));
        }
        return map;
    }

}
