package io.metersphere.service;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.SystemParameterExample;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class RegistryParamService {
    @Resource
    private SystemParameterMapper parameterMapper;

    public List<SystemParameter> getRegistry(String type) {
        List<SystemParameter> paramList = this.getParamList(type);
        for (SystemParameter parameter : paramList) {
            if (StringUtils.equalsIgnoreCase(ParamConstants.Type.PASSWORD.getValue(), parameter.getType())) {
                parameter.setParamValue(EncryptUtils.aesDecrypt(parameter.getParamValue()).toString());
            }
        }
        paramList.sort(Comparator.comparingInt(SystemParameter::getSort));
        return paramList;
    }

    public void updateRegistry(List<SystemParameter> parameters) {
        for (SystemParameter parameter : parameters) {
            if (StringUtils.equalsIgnoreCase(ParamConstants.Type.PASSWORD.getValue(), parameter.getType())) {
                parameter.setParamValue(EncryptUtils.aesEncrypt(parameter.getParamValue()).toString());
            }
            parameterMapper.updateByPrimaryKey(parameter);
        }
    }

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return parameterMapper.selectByExample(example);
    }
}
