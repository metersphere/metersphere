package io.metersphere.service;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.SystemParameterExample;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.commons.constants.ParamConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.Resource;

@Service
public class SystemParameterService {

    @Resource
    private SystemParameterMapper systemParameterMapper;

    public String getSystemLanguage() {
        String result = StringUtils.EMPTY;
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyEqualTo(ParamConstants.I18n.LANGUAGE.getValue());
        List<SystemParameter> list = systemParameterMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            String value = list.get(0).getParamValue();
            if (StringUtils.isNotBlank(value)) {
                result = value;
            }
        }
        return result;
    }
}
