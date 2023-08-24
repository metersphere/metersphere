package io.metersphere.system.service;

import io.metersphere.sdk.dto.TableBatchProcessDTO;
import io.metersphere.sdk.mapper.BaseUserMapper;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户工具服务
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserToolService {
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;

    public List<User> selectByIdList(List<String> userIdList) {
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIdList);
        return userMapper.selectByExample(example);
    }

    public List<String> getBatchUserIds(TableBatchProcessDTO request) {
        if (request.isSelectAll()) {
            List<User> userList = baseUserMapper.selectByKeyword(request.getCondition().getKeyword(), true);
            List<String> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                userIdList.removeAll(request.getExcludeIds());
            }
            return userIdList;
        } else {
            return request.getSelectIds();
        }
    }
}
