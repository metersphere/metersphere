package io.metersphere.service;

import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.mapper.CustomFieldTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseCustomFieldTemplateService {
    @Resource
    private CustomFieldTemplateMapper customFieldTemplateMapper;

    public void update(CustomFieldTemplate request) {
        customFieldTemplateMapper.updateByPrimaryKeySelective(request);
    }
}
