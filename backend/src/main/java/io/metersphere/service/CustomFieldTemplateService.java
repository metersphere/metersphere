package io.metersphere.service;


import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.domain.CustomFieldTemplateExample;
import io.metersphere.base.mapper.CustomFieldTemplateMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldTemplateMapper;
import io.metersphere.dto.CustomFieldTemplateDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldTemplateService {

    @Resource
    CustomFieldTemplateMapper customFieldTemplateMapper;
    @Resource
    ExtCustomFieldTemplateMapper extCustomFieldTemplateMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    CustomFieldService customFieldService;

    public List<String> getCustomFieldIds(String templateId) {
        return extCustomFieldTemplateMapper.getCustomFieldIds(templateId);
    }

    public List<CustomFieldTemplate> getCustomFields(String templateId) {
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria().andTemplateIdEqualTo(templateId);
        return customFieldTemplateMapper.selectByExample(example);
    }

    public List<CustomFieldTemplateDao> list(CustomFieldTemplate request) {
        return extCustomFieldTemplateMapper.list(request);
    }

    public void deleteByTemplateId(String templateId) {
        if (StringUtils.isNotBlank(templateId)) {
            CustomFieldTemplateExample example = new CustomFieldTemplateExample();
            example.createCriteria().andTemplateIdEqualTo(templateId);
            customFieldTemplateMapper.deleteByExample(example);
        }
    }

    public void deleteByFieldId(String fieldId) {
        if (StringUtils.isNotBlank(fieldId)) {
            CustomFieldTemplateExample example = new CustomFieldTemplateExample();
            example.createCriteria().andFieldIdEqualTo(fieldId);
            customFieldTemplateMapper.deleteByExample(example);
        }
    }

    public  List<CustomFieldTemplate> getSystemFieldCreateTemplate(CustomField customField, String scene) {
        List<CustomField> globalField = customFieldService.getGlobalField(scene);
        for (int i = 0; i < globalField.size(); i++) {
            // 替换全局的字段
            if (StringUtils.equals(globalField.get(i).getName(), customField.getName())) {
                globalField.set(i, customField);
                break;
            }
        }
        List<String> fieldIds = globalField.stream().map(CustomField::getId).collect(Collectors.toList());
        return getFieldTemplateByFieldIds(fieldIds);
    }

    public  void updateFieldIdByTemplate(String templateId, String originId , String fieldId) {
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria()
                .andTemplateIdEqualTo(templateId)
                .andFieldIdEqualTo(originId);
        CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
        customFieldTemplate.setFieldId(fieldId);
        customFieldTemplateMapper.updateByExampleSelective(customFieldTemplate, example);
    }

    public  List<CustomFieldTemplate> getFieldTemplateByFieldIds(List<String> fieldIds) {
        if (CollectionUtils.isNotEmpty(fieldIds)) {
            CustomFieldTemplateExample example = new CustomFieldTemplateExample();
            example.createCriteria().andFieldIdIn(fieldIds);
            return customFieldTemplateMapper.selectByExample(example);
        }
        return null;
    }

    public void create(List<CustomFieldTemplate> customFields, String templateId, String scene) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CustomFieldTemplateMapper customFieldTemplateMapper =
                sqlSession.getMapper(CustomFieldTemplateMapper.class);
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields.forEach(item -> {
                item.setId(UUID.randomUUID().toString());
                item.setTemplateId(templateId);
                item.setScene(scene);
                if (item.getRequired() == null) {
                    item.setRequired(false);
                }
                customFieldTemplateMapper.insert(item);
            });
        }
        sqlSession.flushStatements();
        sqlSession.close();
    }


    public void update(CustomFieldTemplate request) {
        customFieldTemplateMapper.updateByPrimaryKeySelective(request);
    }
}
