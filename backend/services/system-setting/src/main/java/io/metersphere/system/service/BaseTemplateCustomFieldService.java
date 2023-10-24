package io.metersphere.system.service;

import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.TemplateCustomField;
import io.metersphere.system.domain.TemplateCustomFieldExample;
import io.metersphere.system.dto.CustomFieldDao;
import io.metersphere.system.mapper.TemplateCustomFieldMapper;
import io.metersphere.system.resolver.field.AbstractCustomFieldResolver;
import io.metersphere.system.resolver.field.CustomFieldResolverFactory;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import io.metersphere.system.uid.IDGenerator;
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
        if (CollectionUtils.isEmpty(projectTemplateIds)) {
            return;
        }
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria().andTemplateIdIn(projectTemplateIds);
        templateCustomFieldMapper.deleteByExample(example);
    }

    public void addByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests) {
        if (CollectionUtils.isEmpty(customFieldRequests)) {
            return;
        }

        // 过滤下不存在的字段
        List<String> ids = customFieldRequests.stream().map(TemplateCustomFieldRequest::getFieldId).toList();
        Set<String> fieldIdSet = baseCustomFieldService.getByIds(ids)
                .stream()
                .map(CustomField::getId)
                .collect(Collectors.toSet());
        customFieldRequests = customFieldRequests.stream()
                .filter(item -> fieldIdSet.contains(item.getFieldId()))
                .toList();

        AtomicReference<Integer> pos = new AtomicReference<>(0);
        List<TemplateCustomField> templateCustomFields = customFieldRequests.stream().map(field -> {
            TemplateCustomField templateCustomField = new TemplateCustomField();
            templateCustomField.setId(IDGenerator.nextStr());
            BeanUtils.copyBean(templateCustomField, field);
            templateCustomField.setTemplateId(id);
            templateCustomField.setPos(pos.getAndSet(pos.get() + 1));
            templateCustomField.setDefaultValue(parseDefaultValue(field));
            return templateCustomField;
        }).toList();

        if (templateCustomFields.size() > 0) {
            templateCustomFieldMapper.batchInsert(templateCustomFields);
        }
    }

    private String parseDefaultValue(TemplateCustomFieldRequest field) {
        CustomField customField = baseCustomFieldService.getWithCheck(field.getFieldId());
        AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
        CustomFieldDao customFieldDao = BeanUtils.copyBean(new CustomFieldDao(), customField);
        customFieldDao.setRequired(false);
        customFieldResolver.validate(customFieldDao, field.getDefaultValue());
        return customFieldResolver.parse2String(field.getDefaultValue());
    }

    public List<TemplateCustomField> getByTemplateId(String id) {
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria().andTemplateIdEqualTo(id);
        return templateCustomFieldMapper.selectByExample(example);
    }

    public List<TemplateCustomField> getByTemplateIds(List<String> projectTemplateIds) {
        if (CollectionUtils.isEmpty(projectTemplateIds)) {
            return new ArrayList(0);
        }
        TemplateCustomFieldExample example = new TemplateCustomFieldExample();
        example.createCriteria().andTemplateIdIn(projectTemplateIds);
        return templateCustomFieldMapper.selectByExample(example);
    }
}
