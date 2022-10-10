package io.metersphere.xpack.module.service;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.SystemParameterExample;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.xpack.module.dto.Module;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListModuleService {
    private static final String PREFIX = "metersphere.module.";
    @Resource
    private SystemParameterMapper systemParameterMapper;

    public List<Module> listModules() {
        List<Module> result = new ArrayList<>();
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(PREFIX + "%");
        List<SystemParameter> paramList = systemParameterMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(paramList)) {
            for (SystemParameter systemParameter : paramList) {
                Module module = new Module();
                module.setKey(StringUtils.removeStart(systemParameter.getParamKey(), PREFIX));
                module.setStatus(systemParameter.getParamValue());
                result.add(module);
            }
        }
        return result;
    }

}
