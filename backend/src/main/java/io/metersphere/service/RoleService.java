package io.metersphere.service;

import io.metersphere.base.domain.Role;
import io.metersphere.base.mapper.ext.ExtRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService {

    @Resource
    private ExtRoleMapper extRoleMapper;

    public List<Role> getRoleList(String sign) {
        return extRoleMapper.getRoleList(sign);
    }
}
