package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.StatusItem;
import io.metersphere.system.domain.StatusItemExample;
import io.metersphere.system.mapper.ExtStatusItemMapper;
import io.metersphere.system.mapper.StatusItemMapper;
import io.metersphere.system.uid.UUID;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.metersphere.system.controller.handler.result.CommonResultCode.STATUS_ITEM_EXIST;
import static io.metersphere.system.controller.handler.result.CommonResultCode.STATUS_ITEM_NOT_EXIST;

/**
 * @author jianxing
 * @date : 2023-10-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseStatusItemService {

    @Resource
    private StatusItemMapper statusItemMapper;
    @Resource
    private ExtStatusItemMapper extStatusItemMapper;

    public List<StatusItem> getStatusItems(String scopeId, String scene) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria()
                .andScopeIdEqualTo(scopeId)
                .andSceneEqualTo(scene);
        return statusItemMapper.selectByExample(example);
    }

    public List<StatusItem> translateInternalStatusItem(List<StatusItem> statusItems) {
        statusItems.forEach(item -> {
            if (item.getInternal()) {
                item.setName(translateInternalStatusItem(item.getName()));
            }
        });
        return statusItems;
    }

    public String translateInternalStatusItem(String statusName) {
        return Translator.get("status_item." + statusName);
    }

    public StatusItem getWithCheck(String id) {
        checkResourceExist(id);
        return statusItemMapper.selectByPrimaryKey(id);
    }

    /**
     * 校验状态项是否存在
     * @param scopeId
     * @param statusIds
     */
    public void checkStatusScope(String scopeId, List<String> statusIds) {
        if (CollectionUtils.isEmpty(statusIds)) {
            return;
        }
        StatusItemExample example = new StatusItemExample();
        example.createCriteria()
                .andScopeIdEqualTo(scopeId)
                .andIdIn(statusIds);
        if (statusItemMapper.selectByExample(example).size() < statusIds.size()) {
            throw new MSException(STATUS_ITEM_NOT_EXIST);
        }
    }

    public StatusItem baseAdd(StatusItem statusItem) {
        statusItem.setId(UUID.nextStr());
        statusItemMapper.insert(statusItem);
        return statusItem;
    }

    public StatusItem add(StatusItem statusItem) {
        checkAddExist(statusItem);
        statusItem.setInternal(false);
        return baseAdd(statusItem);
    }

    public List<StatusItem> batchAdd(List<StatusItem> statusItems) {
        if (CollectionUtils.isEmpty(statusItems)) {
            return List.of();
        }
        statusItems.forEach(statusItem -> statusItem.setId(UUID.nextStr()));
        statusItemMapper.batchInsert(statusItems);
        return statusItems;
    }

    public StatusItem update(StatusItem statusItem) {
        statusItem.setScopeType(null);
        statusItem.setScene(null);
        statusItem.setInternal(null);
        statusItem.setScopeId(null);
        statusItemMapper.updateByPrimaryKeySelective(statusItem);
        return statusItemMapper.selectByPrimaryKey(statusItem.getId());
    }

    private void checkAddExist(StatusItem statusItem) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria()
                .andNameEqualTo(statusItem.getName())
                .andScopeIdEqualTo(statusItem.getScopeId())
                .andSceneEqualTo(statusItem.getScene());
        if (CollectionUtils.isNotEmpty(statusItemMapper.selectByExample(example))) {
            throw new MSException(STATUS_ITEM_EXIST);
        }
    }

    public void checkUpdateExist(StatusItem statusItem) {
        if (StringUtils.isBlank(statusItem.getName())) {
            return;
        }
        StatusItem originStatusItem = statusItemMapper.selectByPrimaryKey(statusItem.getId());
        StatusItemExample example = new StatusItemExample();
        example.createCriteria()
                .andIdNotEqualTo(statusItem.getId())
                .andNameEqualTo(statusItem.getName())
                .andScopeIdEqualTo(originStatusItem.getScopeId())
                .andSceneEqualTo(statusItem.getScene());
        if (CollectionUtils.isNotEmpty(statusItemMapper.selectByExample(example))) {
            throw new MSException(STATUS_ITEM_EXIST);
        }
    }

    public void delete(String id) {
        statusItemMapper.deleteByPrimaryKey(id);
    }


    private StatusItem checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(statusItemMapper.selectByPrimaryKey(id), "permission.status_item.name");
    }

    public List<StatusItem> getByScopeIdsAndScene(List<String> scopeIds, String scene) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andScopeIdIn(scopeIds).andSceneEqualTo(scene);
        return statusItemMapper.selectByExample(example);
    }

    public List<StatusItem> getByScopeId(String scopeId) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andScopeIdEqualTo(scopeId);
        return statusItemMapper.selectByExample(example);
    }

    public List<StatusItem> getByScopeIdAndScene(String scopeId, String scene) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andScopeIdEqualTo(scopeId).andSceneEqualTo(scene);
        return statusItemMapper.selectByExample(example);
    }

    public List<StatusItem> getByRefId(String refId) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andRefIdEqualTo(refId);
        return statusItemMapper.selectByExample(example);
    }

    public void deleteByRefId(String refId) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andRefIdEqualTo(refId);
        statusItemMapper.deleteByExample(example);
    }

    public List<String> getStatusItemIdByRefId(String orgStatusItemId) {
       return extStatusItemMapper.getStatusItemIdByRefId(orgStatusItemId);
    }

    public void deleteByScopeId(String scopeId) {
        StatusItemExample example = new StatusItemExample();
        example.createCriteria().andScopeIdEqualTo(scopeId);
        statusItemMapper.deleteByExample(example);
    }
}