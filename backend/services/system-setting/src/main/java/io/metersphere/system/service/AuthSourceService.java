package io.metersphere.system.service;


import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.AuthSource;
import io.metersphere.system.domain.AuthSourceExample;
import io.metersphere.system.mapper.AuthSourceMapper;
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

    public void addAuthSource(AuthSource authSource) {
        checkAuthSource(authSource);
        long createTime = System.currentTimeMillis();
        authSource.setCreateTime(createTime);
        authSource.setUpdateTime(createTime);
        authSource.setId(UUID.randomUUID().toString());
        authSourceMapper.insertSelective(authSource);
    }

    public void checkAuthSource(AuthSource authSource) {
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

    public void updateAuthSource(AuthSource authSource) {
        checkAuthSource(authSource);
        authSource.setCreateTime(null);
        authSource.setUpdateTime(System.currentTimeMillis());
        authSourceMapper.updateByPrimaryKeySelective(authSource);
    }

}
