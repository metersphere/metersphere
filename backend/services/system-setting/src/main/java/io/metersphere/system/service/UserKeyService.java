package io.metersphere.system.service;


import com.alibaba.excel.util.BooleanUtils;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserKey;
import io.metersphere.system.domain.UserKeyExample;
import io.metersphere.system.dto.UserKeyDTO;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.UserKeyMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserKeyService {

    @Resource
    private UserKeyMapper userKeyMapper;

    @Resource
    private UserLoginService userLoginService;
    @Resource
    private OperationLogService operationLogService;

    public List<UserKey> getUserKeysInfo(String userId) {
        UserKeyExample userKeysExample = new UserKeyExample();
        userKeysExample.createCriteria().andCreateUserEqualTo(userId);
        userKeysExample.setOrderByClause("create_time");
        return userKeyMapper.selectByExample(userKeysExample);
    }

    public void add(String userId) {
        if (userLoginService.getUserDTO(userId) == null) {
            throw new MSException(Translator.get("user_not_exist") + userId);
        }
        UserKeyExample userKeysExample = new UserKeyExample();
        userKeysExample.createCriteria().andCreateUserEqualTo(userId);
        List<UserKey> userKeysList = userKeyMapper.selectByExample(userKeysExample);

        if (!CollectionUtils.isEmpty(userKeysList) && userKeysList.size() >= 5) {
            throw new MSException(Translator.get("user_apikey_limit"));
        }

        UserKey userKeys = new UserKey();
        userKeys.setId(IDGenerator.nextStr());
        userKeys.setCreateUser(userId);
        userKeys.setEnable(true);
        userKeys.setAccessKey(RandomStringUtils.randomAlphanumeric(16));
        userKeys.setSecretKey(RandomStringUtils.randomAlphanumeric(16));
        userKeys.setCreateTime(System.currentTimeMillis());
        userKeys.setForever(true);
        userKeyMapper.insert(userKeys);

        LogDTO dto = LogDTOBuilder.builder()
                .projectId(OperationLogConstants.SYSTEM)
                .organizationId(OperationLogConstants.SYSTEM)
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.PERSONAL_INFORMATION_APIKEYS)
                .method(HttpMethodConstants.GET.name())
                .path("/user/api/key/add")
                .sourceId(userKeys.getId())
                .content(userKeys.getAccessKey())
                .originalValue(JSON.toJSONBytes(userKeys))
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void deleteUserKey(String id) {
        checkUserKey(id);
        userKeyMapper.deleteByPrimaryKey(id);
    }

    public void enableUserKey(String id) {
        checkUserKey(id);
        UserKey userKeys = new UserKey();
        userKeys.setId(id);
        userKeys.setEnable(true);
        userKeyMapper.updateByPrimaryKeySelective(userKeys);
    }

    public void disableUserKey(String id) {
        checkUserKey(id);
        UserKey userKeys = new UserKey();
        userKeys.setId(id);
        userKeys.setEnable(false);
        userKeyMapper.updateByPrimaryKeySelective(userKeys);
    }

    public UserKey getUserKey(String accessKey) {
        UserKeyExample userKeyExample = new UserKeyExample();
        userKeyExample.createCriteria().andAccessKeyEqualTo(accessKey).andEnableEqualTo(true);
        List<UserKey> userKeysList = userKeyMapper.selectByExample(userKeyExample);
        if (!CollectionUtils.isEmpty(userKeysList)) {
            return userKeysList.getFirst();
        }
        return null;
    }

    public void updateUserKey(UserKeyDTO userKeyDTO) {
        UserKey userKey = checkUserKey(userKeyDTO.getId());
        userKey.setId(userKeyDTO.getId());
        userKey.setForever(userKeyDTO.getForever());
        if (BooleanUtils.isFalse(userKeyDTO.getForever())) {
            if (userKeyDTO.getExpireTime() == null) {
                throw new MSException(Translator.get("expire_time_not_null"));
            }
            userKey.setExpireTime(userKeyDTO.getExpireTime());
        } else {
            userKey.setExpireTime(null);
        }
        userKey.setDescription(userKeyDTO.getDescription());
        userKeyMapper.updateByPrimaryKeySelective(userKey);
    }

    public UserKey checkUserKey(String id) {
        UserKey userKey = userKeyMapper.selectByPrimaryKey(id);
        if (userKey == null) {
            throw new MSException(Translator.get("api_key_not_exist"));
        }
        return userKey;
    }

    public void checkUserKeyOwner(String id, String userId) {
        UserKey userKey = checkUserKey(id);
        if (!StringUtils.equals(userKey.getCreateUser(), userId)) {
            throw new MSException(Translator.get("current_user_can_not_operation_api_key"));
        }
    }
}
