package io.metersphere.system.service;

import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.sdk.util.CodingUtil;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.UserMaintainRequest;
import io.metersphere.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;

    private void validateUserInfo(List<User> userList) {
        //判断参数内是否含有重复邮箱
        List<String> emailList = new ArrayList<>();
        List<String> repeatEmailList = new ArrayList<>();
        var userInDbMap = baseUserMapper.selectUserIdByEmailList(
                        userList.stream().map(User::getEmail).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getEmail, User::getId));
        for (User user : userList) {
            if (emailList.contains(user.getEmail())) {
                repeatEmailList.add(user.getEmail());
            } else {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(user.getEmail())) {
                    repeatEmailList.add(user.getEmail());
                } else {
                    emailList.add(user.getEmail());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(repeatEmailList)) {
            throw new MSException(Translator.get("user.email.repeat", repeatEmailList.toString()));
        }
    }

    public UserMaintainRequest add(UserMaintainRequest userCreateDTO) {
        this.validateUserInfo(userCreateDTO.getUserInfoList());
        long createTime = System.currentTimeMillis();
        for (User user : userCreateDTO.getUserInfoList()) {
            user.setId(UUID.randomUUID().toString());
            if (StringUtils.isEmpty(user.getPassword())) {
                user.setPassword(CodingUtil.md5(user.getEmail()));
            }
            user.setCreateTime(createTime);
            user.setUpdateTime(createTime);
            userMapper.insertSelective(user);
        }
        return userCreateDTO;
    }


    public UserDTO getByEmail(String email) {
        return baseUserMapper.selectByEmail(email);
    }

}
