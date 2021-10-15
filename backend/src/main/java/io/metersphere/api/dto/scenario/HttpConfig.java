package io.metersphere.api.dto.scenario;

import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiModuleExample;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class HttpConfig {
    private String apiEnvironmentid;
    private String socket;
    private String domain;
    private String protocol = "https";
    private int port;
    private boolean isMock;
    private List<HttpConfigCondition> conditions;
    private List<KeyValue> headers;

    private ApiModuleMapper apiModuleMapper;

    public HttpConfig initHttpConfig(HttpConfigCondition configCondition) {
        HttpConfig config = new HttpConfig();
        config.isMock = this.isMock;
        BeanUtils.copyBean(config, configCondition);
        config.setHeaders(configCondition.getHeaders());
        return config;
    }

    public HttpConfig getPathCondition(String path, HttpConfigCondition configCondition) {
        if (CollectionUtils.isNotEmpty(configCondition.getDetails())) {
            List<KeyValue> details = configCondition.getDetails().stream().filter(detail -> (detail.getValue().equals("contains") && StringUtils.contains(path, detail.getName())) || (detail.getValue().equals("equals") && StringUtils.equals(path, detail.getName()))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(details)) {
                return initHttpConfig(configCondition);
            }
        }
        return null;
    }

    private void getAllChild(String moduleId, List<String> allChild) {
        // 找出所有子模块
        if (apiModuleMapper == null) {
            apiModuleMapper = CommonBeanFactory.getBean(ApiModuleMapper.class);
        }
        allChild.add(moduleId);
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andParentIdEqualTo(moduleId);
        List<ApiModule> modules = apiModuleMapper.selectByExample(example);
        for (ApiModule module : modules) {
            getAllChild(module.getId(), allChild);
        }
    }

//    public HttpConfig getModuleCondition(String moduleId, HttpConfigCondition configCondition) {
//        List<String> moduleIds = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(configCondition.getDetails())) {
//            if (CollectionUtils.isEmpty(configCondition.getModuleIds())) {
//                for (KeyValue keyValue : configCondition.getDetails()) {
//                    getAllChild(keyValue.getValue(), moduleIds);
//                }
//                configCondition.setModuleIds(moduleIds);
//            } else {
//                moduleIds = configCondition.getModuleIds();
//            }
//
//            if (moduleIds.contains(moduleId)) {
//                return initHttpConfig(configCondition);
//            }
//        }
//        return null;
//    }

    public HttpConfig getModuleCondition(String moduleId, HttpConfigCondition configCondition) {
        if (CollectionUtils.isNotEmpty(configCondition.getDetails())) {
            List<KeyValue> details = configCondition.getDetails().stream().filter(detail -> StringUtils.contains(detail.getValue(), moduleId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(details)) {
                return initHttpConfig(configCondition);
            }
        }
        return null;
    }
}
