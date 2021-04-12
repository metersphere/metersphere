package io.metersphere.service;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldMapper;
import io.metersphere.controller.request.QueryCustomFieldRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldService {

    @Resource
    ExtCustomFieldMapper extCustomFieldMapper;

    @Resource
    CustomFieldMapper customFieldMapper;

    public void add(CustomField customField) {
        customField.setId(UUID.randomUUID().toString());
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customFieldMapper.insert(customField);
    }

    public List<CustomField> list(QueryCustomFieldRequest request) {
        return extCustomFieldMapper.list(request);
    }

    public void delete(String id) {
        customFieldMapper.deleteByPrimaryKey(id);
    }

    public void update(CustomField customField) {
        customField.setUpdateTime(System.currentTimeMillis());
        customFieldMapper.updateByPrimaryKeySelective(customField);
    }
}
