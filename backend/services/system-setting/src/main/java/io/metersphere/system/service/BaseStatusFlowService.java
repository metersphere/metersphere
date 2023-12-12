package io.metersphere.system.service;

import io.metersphere.system.domain.StatusFlow;
import io.metersphere.system.domain.StatusFlowExample;
import io.metersphere.system.mapper.StatusFlowMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public void add(StatusFlow statusFlow) {
        statusFlow.setId(IDGenerator.nextStr());
        statusFlowMapper.insert(statusFlow);
    }

    public void delete(String fromId, String toId) {
        StatusFlowExample example = new StatusFlowExample();
        example.createCriteria()
                .andFromIdEqualTo(fromId)
                .andToIdEqualTo(toId);
        statusFlowMapper.deleteByExample(example);
    }

    public void deleteByFromIdsAndToIds(List<String> subProjectFromIds, List<String> subProjectToIds) {
        if (CollectionUtils.isEmpty(subProjectFromIds) || CollectionUtils.isEmpty(subProjectToIds)) {
            return;
        }
        StatusFlowExample example = new StatusFlowExample();
        example.createCriteria()
                .andFromIdIn(subProjectFromIds)
                .andToIdIn(subProjectToIds);
        statusFlowMapper.deleteByExample(example);
    }

    public List<StatusFlow> getNextStatusFlows(String statusId) {
        StatusFlowExample example = new StatusFlowExample();
        example.createCriteria().andFromIdEqualTo(statusId);
        return statusFlowMapper.selectByExample(example);
    }
}