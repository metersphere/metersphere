package io.metersphere.system.service;

import io.metersphere.sdk.dto.request.StatusFlowUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusFlowExample;
import io.metersphere.system.mapper.StatusFlowMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseStatusFlowService {
    @Resource
    private StatusFlowMapper statusFlowMapper;


    public List<StatusFlow> getStatusFlows(List<String> statusIds) {
        if (CollectionUtils.isEmpty(statusIds)) {
            return List.of();
        }
        StatusFlowExample example = new StatusFlowExample();
        example.createCriteria()
                .andFromIdIn(statusIds);
        StatusFlowExample.Criteria criteria = example.createCriteria()
                .andToIdIn(statusIds);
        example.or(criteria);
        return statusFlowMapper.selectByExample(example);
    }

    public void updateStatusFlow(List<String> deleteStatusItemIds, List<StatusFlowUpdateRequest.StatusFlowRequest> statusFlowRequests) {
        // 先删除
        StatusFlowExample example = new StatusFlowExample();
        example.createCriteria().andFromIdIn(deleteStatusItemIds);
        statusFlowMapper.deleteByExample(example);
        example.clear();
        example.createCriteria().andToIdIn(deleteStatusItemIds);
        statusFlowMapper.deleteByExample(example);

        // 再添加
        List<StatusFlow> statusFlows = statusFlowRequests.stream().map(request -> {
            StatusFlow statusFlow = new StatusFlow();
            BeanUtils.copyBean(statusFlow, request);
            statusFlow.setId(IDGenerator.nextStr());
            return statusFlow;
        }).toList();
        statusFlowMapper.batchInsert(statusFlows);
    }

    public static List<String> getStatusIds(List<StatusFlowUpdateRequest.StatusFlowRequest> statusFlows) {
        Set<String> statusIds = new HashSet<>();
        statusFlows.forEach(statusFlow -> {
            statusIds.add(statusFlow.getFromId());
            statusIds.add(statusFlow.getToId());
        });
        return statusIds.stream().collect(Collectors.toList());
    }

    public void deleteByStatusId(String statusId) {
        StatusFlowExample example = new StatusFlowExample();
        example.createCriteria()
                .andFromIdEqualTo(statusId);
        StatusFlowExample.Criteria criteria = example.createCriteria()
                .andToIdEqualTo(statusId);
        example.or(criteria);
        statusFlowMapper.deleteByExample(example);
    }

    public void deleteByStatusIds(List<String> statusItemIds) {
        if (CollectionUtils.isEmpty(statusItemIds)) {
            return;
        }
        StatusFlowExample example = new StatusFlowExample();
        example.createCriteria()
                .andFromIdIn(statusItemIds);
        StatusFlowExample.Criteria criteria = example.createCriteria()
                .andToIdIn(statusItemIds);
        example.or(criteria);
        statusFlowMapper.deleteByExample(example);
    }

    public void batchAdd(List<StatusFlow> statusFlows) {
        if (CollectionUtils.isEmpty(statusFlows)) {
            return;
        }
        statusFlows.forEach(statusFlow -> {
            statusFlow.setId(IDGenerator.nextStr());
        });
        statusFlowMapper.batchInsert(statusFlows);
    }
}