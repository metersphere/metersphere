package io.metersphere.system.service;

import io.metersphere.system.domain.OperationHistory;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.BaseOperationHistoryMapper;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.OperationHistoryMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperationHistoryService {
    @Resource
    private BaseOperationHistoryMapper baseOperationHistoryMapper;

    @Resource
    private BaseUserMapper baseUserMapper;

    @Resource
    private OperationHistoryMapper operationHistoryMapper;


    public List<OperationHistoryDTO> list(OperationHistoryRequest request) {
        List<OperationHistoryDTO> list = baseOperationHistoryMapper.list(request);

        if (CollectionUtils.isNotEmpty(list)) {
            List<String> userIds = list.stream().distinct()
                    .map(OperationHistoryDTO::getCreateUser).toList();

            Map<String, String> userMap = baseUserMapper.selectUserOptionByIds(userIds).stream()
                    .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));

            list.forEach(item -> item.setCreateUserName(userMap.getOrDefault(item.getCreateUser(), StringUtils.EMPTY)));

        }
        return list;
    }


    public List<OperationHistoryDTO> listWidthTable(OperationHistoryRequest request, String table) {
        List<OperationHistoryDTO> list = baseOperationHistoryMapper.listWidthTable(request, table);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> userIds = list.stream().distinct()
                    .map(OperationHistoryDTO::getCreateUser).toList();

            Map<String, String> userMap = baseUserMapper.selectUserOptionByIds(userIds).stream()
                    .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));

            list.forEach(item -> item.setCreateUserName(userMap.getOrDefault(item.getCreateUser(), StringUtils.EMPTY)));

        }
        return list;
    }


    public void associationRefId(Long refLogId, Long logId) {
        OperationHistory operationHistory = operationHistoryMapper.selectByPrimaryKey(logId);
        operationHistory.setRefId(refLogId);
        operationHistoryMapper.updateByPrimaryKeySelective(operationHistory);
    }

}
