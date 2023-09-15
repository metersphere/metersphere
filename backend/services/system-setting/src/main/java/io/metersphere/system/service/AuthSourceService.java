package io.metersphere.system.service;


import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.uid.UUID;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.domain.AuthSourceExample;
import io.metersphere.system.dto.AuthSourceDTO;
import io.metersphere.system.mapper.AuthSourceMapper;
import io.metersphere.system.request.AuthSourceRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthSourceService {
    @Resource
    private AuthSourceMapper authSourceMapper;

    public List<AuthSource> list() {
        AuthSourceExample example = new AuthSourceExample();
        return authSourceMapper.selectByExample(example);
    }

    public AuthSource addAuthSource(AuthSourceRequest authSource) {
        checkAuthSource(authSource);
        AuthSource source = delRequestToDB(authSource);
        authSourceMapper.insertSelective(source);
        return source;
    }

    private AuthSource delRequestToDB(AuthSourceRequest authSource) {
        long createTime = System.currentTimeMillis();
        AuthSource source = new AuthSource();
        source.setName(authSource.getName());
        source.setConfiguration(authSource.getConfiguration().getBytes(StandardCharsets.UTF_8));
        source.setDescription(authSource.getDescription());
        source.setType(authSource.getType());
        source.setCreateTime(createTime);
        source.setUpdateTime(createTime);
        source.setId(UUID.randomUUID().toString());
        return source;
    }

    public void checkAuthSource(AuthSourceRequest authSource) {
        String resourcePoolName = authSource.getName();

        AuthSourceExample example = new AuthSourceExample();
        AuthSourceExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(resourcePoolName);
        if (StringUtils.isNotBlank(authSource.getId())) {
            criteria.andIdNotEqualTo(authSource.getId());
        }

        if (authSourceMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("authsource_name_already_exists"));
        }
        if (StringUtils.isBlank(authSource.getConfiguration().toString())) {
            throw new MSException(Translator.get("authsource_configuration_is_null"));
        }
    }

    public void deleteAuthSource(String id) {
        authSourceMapper.deleteByPrimaryKey(id);
    }

    public AuthSourceDTO getAuthSource(String id) {
        AuthSource source = authSourceMapper.selectByPrimaryKey(id);
        AuthSourceDTO authSourceDTO = new AuthSourceDTO();
        BeanUtils.copyBean(authSourceDTO, source);
        authSourceDTO.setConfiguration(new String(source.getConfiguration(), StandardCharsets.UTF_8));
        return authSourceDTO;
    }

    public AuthSourceRequest updateAuthSource(AuthSourceRequest authSource) {
        checkAuthSource(authSource);
        AuthSource source = authSourceMapper.selectByPrimaryKey(authSource.getId());
        if (source != null) {
            source.setName(authSource.getName());
            source.setDescription(authSource.getDescription());
            source.setConfiguration(authSource.getConfiguration().getBytes());
            source.setType(authSource.getType());
            source.setUpdateTime(System.currentTimeMillis());
            authSourceMapper.updateByPrimaryKeySelective(source);
        }
        return authSource;
    }

    public AuthSource updateStatus(String id, Boolean status) {
        AuthSource record = new AuthSource();
        record.setId(id);
        record.setEnable(BooleanUtils.toBooleanDefaultIfNull(status,false));
        record.setUpdateTime(System.currentTimeMillis());
        authSourceMapper.updateByPrimaryKeySelective(record);
        return record;
    }
}
