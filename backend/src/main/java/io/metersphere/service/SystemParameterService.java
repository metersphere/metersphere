package io.metersphere.service;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.SystemParameterExample;
import io.metersphere.base.mapper.SystemParameterMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.EncryptUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.springframework.mail.javamail.JavaMailSenderImpl;


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

    public void editMail(List<SystemParameter> parameters) {
        List<SystemParameter> paramList = this.getParamList(ParamConstants.Classify.MAIL.getValue());
        boolean empty = paramList.size() < 2;
        parameters.forEach(parameter -> {
            if (parameter.getParamKey().equals(ParamConstants.MAIL.PASSWORD.getKey())) {
                String string = EncryptUtils.aesEncrypt(parameter.getParamValue()).toString();
                parameter.setParamValue(string);
            }
            if (empty) {
                systemParameterMapper.insert(parameter);
            } else {
                systemParameterMapper.updateByPrimaryKey(parameter);
            }
        });
    }

    public List<SystemParameter> getParamList(String type) {
        SystemParameterExample example = new SystemParameterExample();
        example.createCriteria().andParamKeyLike(type + "%");
        return systemParameterMapper.selectByExample(example);
    }

    public void testConnection(HashMap<String, String> hashMap) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(hashMap.get(ParamConstants.MAIL.PORT.getKey()));
        javaMailSender.setPort(Integer.valueOf(hashMap.get(ParamConstants.MAIL.PORT.getKey())));
        javaMailSender.setUsername(hashMap.get(ParamConstants.MAIL.ACCOUNT.getKey()));
        javaMailSender.setPassword(hashMap.get(ParamConstants.MAIL.PASSWORD.getKey()));
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        if (BooleanUtils.toBoolean(hashMap.get(ParamConstants.MAIL.SSL.getKey()))) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        if (BooleanUtils.toBoolean(hashMap.get(ParamConstants.MAIL.TLS.getKey()))) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        javaMailSender.setJavaMailProperties(props);

    }
}
