package io.metersphere.ui.service;

import io.metersphere.ui.domain.UiElement;
import io.metersphere.ui.domain.UiElementExample;
import io.metersphere.ui.dto.LocationType;
import io.metersphere.ui.mapper.UiElementMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class UiElementService {
    @Resource
    private UiElementMapper uiElementMapper;

    public List<UiElement> list() {
        return uiElementMapper.selectByExample(new UiElementExample());

    }

    public UiElement add(UiElement uiElement) {
        uiElement.setId(UUID.randomUUID().toString());
        uiElement.setCreateTime(System.currentTimeMillis());
        uiElement.setUpdateTime(System.currentTimeMillis());
        uiElement.setRefId(uiElement.getId());
        uiElementMapper.insertSelective(uiElement);
        return uiElement;
    }

    public UiElement update(UiElement uiElement) {
        uiElementMapper.updateByPrimaryKeySelective(uiElement);
        return uiElementMapper.selectByPrimaryKey(uiElement.getId());
    }
}
