package io.metersphere.bug.service;

import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.OperationHistory;
import io.metersphere.system.domain.OperationHistoryExample;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.BaseOperationHistoryMapper;
import io.metersphere.system.mapper.OperationHistoryMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.service.UserToolService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugHistoryService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserToolService userToolService;
    @Resource
    private OperationHistoryMapper operationHistoryMapper;
    @Resource
    private BaseOperationHistoryMapper baseOperationHistoryMapper;

    /**
     * 缺陷变更历史分页列表
     * @param request 请求参数
     * @return 变更历史集合
     */
    public List<OperationHistoryDTO> list(OperationHistoryRequest request) {
        OperationHistoryExample example = new OperationHistoryExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andModuleIn(List.of(OperationLogModule.BUG_MANAGEMENT_INDEX, OperationLogModule.BUG_MANAGEMENT_RECYCLE))
                .andSourceIdEqualTo(request.getSourceId());
        List<OperationHistory> history = operationHistoryMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(history)) {
            return List.of();
        }
        List<String> userIds = history.stream().map(OperationHistory::getCreateUser).toList();
        Map<String, String> userMap = userToolService.getUserMapByIds(userIds);
        Long latestVersionId = baseOperationHistoryMapper.selectLatestIdByOperationId(request.getSourceId());
        return history.stream().map(h -> {
            OperationHistoryDTO dto = new OperationHistoryDTO();
            BeanUtils.copyBean(dto, h);
            dto.setCreateUserName(userMap.get(h.getCreateUser()) == null ? h.getCreateUser() : userMap.get(h.getCreateUser()));
            if (StringUtils.equals(dto.getType(), OperationLogType.ADD.name())) {
                dto.setType(Translator.get("add"));
            } else if (StringUtils.equals(dto.getType(), OperationLogType.UPDATE.name())) {
                dto.setType(Translator.get("update"));
            } else if (StringUtils.equals(dto.getType(), OperationLogType.DELETE.name())) {
                dto.setType(Translator.get("delete"));
            } else if (StringUtils.equals(dto.getType(), OperationLogType.RECOVER.name())) {
                dto.setType(Translator.get("recover"));
            }
            dto.setLatest(latestVersionId != null && latestVersionId.longValue() == dto.getId().longValue());
            return dto;
        }).toList();
    }
}
