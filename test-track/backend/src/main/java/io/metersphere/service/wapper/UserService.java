package io.metersphere.service.wapper;

import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    UserMapper userMapper;

    public UserDTO.PlatformInfo getCurrentPlatformInfo(String workspaceId) {
        String currentPlatformInfoStr = getCurrentPlatformInfoStr(workspaceId);
        if (StringUtils.isNotBlank(currentPlatformInfoStr)) {
            return JSON.parseObject(currentPlatformInfoStr, UserDTO.PlatformInfo.class);
        }
        return null;
    }

    public String  getCurrentPlatformInfoStr(String workspaceId) {
        User user = userMapper.selectByPrimaryKey(SessionUtils.getUserId());
        if (user == null) {
            return null;
        }
        String platformInfoStr = user.getPlatformInfo();
        if (StringUtils.isBlank(workspaceId) || StringUtils.isBlank(platformInfoStr) || platformInfoStr.equals("null")) {
            return null;
        }
        Map platformInfos = JSON.parseMap(platformInfoStr);
        Map platformInfo = (Map) platformInfos.get(workspaceId);
        if (platformInfo == null) {
            return null;
        }
        return JSON.toJSONString(platformInfo);
    }
}
