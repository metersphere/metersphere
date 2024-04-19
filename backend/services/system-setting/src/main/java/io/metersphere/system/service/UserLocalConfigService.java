package io.metersphere.system.service;


import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserLocalConfig;
import io.metersphere.system.domain.UserLocalConfigExample;
import io.metersphere.system.dto.UserLocalConfigAddRequest;
import io.metersphere.system.dto.UserLocalConfigUpdateRequest;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.UserLocalConfigMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserLocalConfigService {

    @Resource
    private UserLocalConfigMapper userLocalConfigMapper;
    @Resource
    private OperationLogService operationLogService;

    public UserLocalConfig add(UserLocalConfigAddRequest request, String userId) {
        checkResource(userId, request.getType());
        UserLocalConfig userLocalConfig = new UserLocalConfig();
        userLocalConfig.setId(IDGenerator.nextStr());
        userLocalConfig.setCreateUser(userId);
        userLocalConfig.setType(request.getType());
        userLocalConfig.setUserUrl(request.getUserUrl());
        userLocalConfigMapper.insertSelective(userLocalConfig);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(OperationLogConstants.SYSTEM)
                .organizationId(OperationLogConstants.SYSTEM)
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.PERSONAL_INFORMATION_LOCAL_EXECUTE)
                .method(HttpMethodConstants.POST.name())
                .path("/user/local/config/add")
                .sourceId(userLocalConfig.getId())
                .originalValue(JSON.toJSONBytes(userLocalConfig))
                .build().getLogDTO();
        operationLogService.add(dto);
        return userLocalConfig;
    }

    public void checkResource(String userId, String type) {
        UserLocalConfigExample userLocalConfigExample = new UserLocalConfigExample();
        userLocalConfigExample.createCriteria().andCreateUserEqualTo(userId).andTypeEqualTo(type);
        if (userLocalConfigMapper.countByExample(userLocalConfigExample) > 0) {
            throw new MSException(Translator.get("current_user_local_config_exist"));
        }
    }

    public UserLocalConfig checkResourceById(String id) {
        UserLocalConfig userLocalConfig = userLocalConfigMapper.selectByPrimaryKey(id);
        if (userLocalConfig == null) {
            throw new MSException(Translator.get("current_user_local_config_not_exist"));
        }
        return userLocalConfig;

    }

    public List<UserLocalConfig> get(String userId) {
        UserLocalConfigExample userLocalConfigExample = new UserLocalConfigExample();
        userLocalConfigExample.createCriteria().andCreateUserEqualTo(userId);
        return userLocalConfigMapper.selectByExample(userLocalConfigExample);
    }

    public void enable(String id) {
        UserLocalConfig userLocalConfig = checkResourceById(id);
        userLocalConfig.setEnable(true);
        userLocalConfigMapper.updateByPrimaryKeySelective(userLocalConfig);
    }

    public void disable(String id) {
        UserLocalConfig userLocalConfig = checkResourceById(id);
        userLocalConfig.setEnable(false);
        userLocalConfigMapper.updateByPrimaryKeySelective(userLocalConfig);
    }

    public void update(UserLocalConfigUpdateRequest request) {
        UserLocalConfig userLocalConfig = checkResourceById(request.getId());
        userLocalConfig.setUserUrl(request.getUserUrl());
        userLocalConfigMapper.updateByPrimaryKey(userLocalConfig);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(OperationLogConstants.SYSTEM)
                .organizationId(OperationLogConstants.SYSTEM)
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PERSONAL_INFORMATION_LOCAL_EXECUTE)
                .method(HttpMethodConstants.POST.name())
                .path("/user/local/config/update")
                .sourceId(userLocalConfig.getId())
                .originalValue(JSON.toJSONBytes(userLocalConfig))
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
