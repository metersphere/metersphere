package io.metersphere.system.service;


import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.domain.AuthSourceExample;
import io.metersphere.system.mapper.AuthSourceMapper;
import io.metersphere.system.request.AuthSourceRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthSourceService {
    @Resource
    private AuthSourceMapper authSourceMapper;

    public List<AuthSource> list() {
        AuthSourceExample example = new AuthSourceExample();
        return authSourceMapper.selectByExample(example);
    }

    public void addAuthSource(AuthSourceRequest authSource) {
        checkAuthSource(authSource);
        AuthSource source = delRequestToDB(authSource);
        authSourceMapper.insertSelective(source);
    }

    private AuthSource delRequestToDB(AuthSourceRequest authSource) {
        long createTime = System.currentTimeMillis();
        AuthSource source = new AuthSource();
        source.setName(authSource.getName());
        source.setConfiguration(authSource.getConfiguration().getBytes());
        source.setDescription(authSource.getDescription());
        source.setType(authSource.getType());
        source.setCreateTime(createTime);
        source.setUpdateTime(createTime);
        source.setId(UUID.randomUUID().toString());
        return source;
    }

    public void checkAuthSource(AuthSourceRequest authSource) {
        String resourcePoolName = authSource.getName();
        if (StringUtils.isBlank(resourcePoolName)) {
            throw new MSException(Translator.get("authsource_name_is_null"));
        }

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

    public AuthSource getAuthSource(String id) {
        return authSourceMapper.selectByPrimaryKey(id);
    }

    public void updateAuthSource(AuthSourceRequest authSource) {
        checkAuthSource(authSource);
        AuthSource source = authSourceMapper.selectByPrimaryKey(authSource.getId());
        if (source != null) {
            source.setName(authSource.getName());
            source.setDescription(authSource.getDescription());
            source.setConfiguration(authSource.getConfiguration().getBytes());
            source.setUpdateTime(System.currentTimeMillis());
            authSourceMapper.updateByPrimaryKeySelective(source);
        }
    }

    public void updateStatus(String id, String status) {
        AuthSource record = new AuthSource();
        record.setId(id);
        record.setEnable(Boolean.parseBoolean(status));
        record.setUpdateTime(System.currentTimeMillis());
        authSourceMapper.updateByPrimaryKeySelective(record);
    }
}
