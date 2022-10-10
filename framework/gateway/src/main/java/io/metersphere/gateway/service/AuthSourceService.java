package io.metersphere.gateway.service;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.domain.AuthSourceExample;
import io.metersphere.base.mapper.AuthSourceMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AuthSourceService {
    @Resource
    private AuthSourceMapper authSourceMapper;

    public List<AuthSource> listAllEnable() {
        AuthSourceExample example = new AuthSourceExample();
        example.createCriteria().andStatusEqualTo("ENABLE");
        return authSourceMapper.selectByExampleWithBLOBs(example);
    }

    public AuthSource getAuthSource(String id) {
        return authSourceMapper.selectByPrimaryKey(id);
    }

}
