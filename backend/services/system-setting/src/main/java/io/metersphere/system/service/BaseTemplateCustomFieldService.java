package io.metersphere.system.service;

import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.TemplateCustomField;
import io.metersphere.system.domain.TemplateCustomFieldExample;
import io.metersphere.system.mapper.TemplateCustomFieldMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import io.metersphere.system.uid.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseTemplateCustomFieldService {
    @Resource
    private TemplateCustomFieldMapper templateCustomFieldMapper;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;

    public void deleteByTemplateId(String templateId) {
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria().andTemplateIdEqualTo(templateId);
        templateCustomFieldMapper.deleteByExample(example);
    }

    public void deleteByTemplateIds(List<String> projectTemplateIds) {
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria().andTemplateIdIn(projectTemplateIds);
        templateCustomFieldMapper.deleteByExample(example);
    }

    public void addByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests) {
        if (CollectionUtils.isEmpty(customFieldRequests)) {
            return;
        }
        AtomicReference<Integer> pos = new AtomicReference<>(0);
        List<TemplateCustomField> templateCustomFields = customFieldRequests.stream().map(field -> {
            TemplateCustomField templateCustomField = new TemplateCustomField();
            templateCustomField.setId(UUID.randomUUID().toString());
            BeanUtils.copyBean(templateCustomField, field);
            templateCustomField.setTemplateId(id);
            templateCustomField.setPos(pos.getAndSet(pos.get() + 1));
            return templateCustomField;
        }).toList();

        // 过滤下不存在的字段
        List<String> ids = templateCustomFields.stream().map(TemplateCustomField::getFieldId).toList();
        Set<String> fieldIdSet = baseCustomFieldService.getByIds(ids).stream().map(CustomField::getId).collect(Collectors.toSet());
        templateCustomFields = templateCustomFields.stream().filter(item -> fieldIdSet.contains(item.getFieldId())).toList();

        if (templateCustomFields.size() > 0) {
            templateCustomFieldMapper.batchInsert(templateCustomFields);
        }
    }

    public List<TemplateCustomField> getByTemplateId(String id) {
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria().andTemplateIdEqualTo(id);
        return templateCustomFieldMapper.selectByExample(example);
    }
}
