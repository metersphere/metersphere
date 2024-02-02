package io.metersphere.bug.service;

import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.OperationHistory;
import io.metersphere.system.domain.OperationHistoryExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.mapper.OperationHistoryMapper;
import io.metersphere.system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugHistoryService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private OperationHistoryMapper operationHistoryMapper;

    /**
     * 缺陷变更历史分页列表
     * @param request 请求参数
     * @return 变更历史集合
     */
    public List<OperationHistoryDTO> list(OperationHistoryRequest request) {
        OperationHistoryExample example = new OperationHistoryExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andModuleEqualTo(OperationLogModule.BUG_MANAGEMENT)
                .andSourceIdEqualTo(request.getSourceId());
        List<OperationHistory> history = operationHistoryMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(history)) {
            return List.of();
        }
        List<String> userIds = history.stream().map(OperationHistory::getCreateUser).toList();
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        Map<String, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        return history.stream().map(h -> {
            OperationHistoryDTO dto = new OperationHistoryDTO();
            BeanUtils.copyBean(dto, h);
            dto.setCreateUserName(userMap.get(h.getCreateUser()) == null ? h.getCreateUser() : userMap.get(h.getCreateUser()));
            return dto;
        }).toList();
    }
}
