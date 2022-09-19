package io.metersphere.service;

import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.domain.CustomFieldTemplateExample;
import io.metersphere.base.mapper.CustomFieldMapper;
import io.metersphere.base.mapper.CustomFieldTemplateMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldTemplateMapper;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldTemplateDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

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
    @Resource
    private CustomFieldMapper customFieldMapper;

    public List<String> getCustomFieldIds(String templateId) {
        return extCustomFieldTemplateMapper.getCustomFieldIds(templateId);
    }

    public List<CustomFieldTemplate> getCustomFields(String templateId) {
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria().andTemplateIdEqualTo(templateId);
        example.setOrderByClause("`order` asc");
        return customFieldTemplateMapper.selectByExampleWithBLOBs(example);
    }

    public List<CustomFieldTemplateDao> list(CustomFieldTemplate request) {
        return extCustomFieldTemplateMapper.list(request);
    }

    public List<CustomFieldDao> lisSimple(CustomFieldTemplate request) {
        return extCustomFieldTemplateMapper.lisSimple(request);
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

    public List<CustomFieldTemplate> getSystemFieldCreateTemplate(CustomFieldDao customField, String templateId) {
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria().andTemplateIdEqualTo(templateId);
        // 获取全局模板的关联关系
        List<CustomFieldTemplate> fieldTemplates = customFieldTemplateMapper.selectByExample(example);
        for (int i = 0; i < fieldTemplates.size(); i++) {
            // 将全局字段替换成项目下的字段
            if (StringUtils.equals(fieldTemplates.get(i).getFieldId(), customField.getOriginGlobalId())) {
                fieldTemplates.get(i).setFieldId(customField.getId());
                break;
            }
        }
        return fieldTemplates;
    }

    public void updateFieldIdByTemplate(String templateId, String originId, String fieldId) {
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria()
                .andTemplateIdEqualTo(templateId)
                .andFieldIdEqualTo(originId);
        CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
        customFieldTemplate.setFieldId(fieldId);
        customFieldTemplateMapper.updateByExampleSelective(customFieldTemplate, example);
    }

    public List<CustomFieldTemplate> getFieldTemplateByFieldIds(List<String> fieldIds) {
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
            Long nextOrder = ServiceUtils.getNextOrder(templateId, extCustomFieldTemplateMapper::getLastOrder);
            for (CustomFieldTemplate item : customFields) {
                item.setId(UUID.randomUUID().toString());
                item.setTemplateId(templateId);
                item.setScene(scene);
                if (item.getRequired() == null) {
                    item.setRequired(false);
                }
                nextOrder += ServiceUtils.ORDER_STEP;
                item.setOrder((int) nextOrder.longValue());
                customFieldTemplateMapper.insert(item);
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }


    public void update(CustomFieldTemplate request) {
        customFieldTemplateMapper.updateByPrimaryKeySelective(request);
    }

    public CustomField getCustomField(String id) {
        CustomFieldTemplate customFieldTemplate = customFieldTemplateMapper.selectByPrimaryKey(id);
        String fieldId = customFieldTemplate.getFieldId();
        return customFieldMapper.selectByPrimaryKey(fieldId);
    }

    /**
     * 将原来全局字段与模板的关联
     * 改为项目下字段与模板的关联
     *
     * @param customField
     * @param templateIds
     * @return
     */
    public int updateProjectTemplateGlobalField(CustomFieldDao customField, List<String> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return 0;
        }
        CustomFieldTemplateExample example = new CustomFieldTemplateExample();
        example.createCriteria()
                .andFieldIdEqualTo(customField.getOriginGlobalId())
                .andTemplateIdIn(templateIds);
        CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
        customFieldTemplate.setFieldId(customField.getId());
        return customFieldTemplateMapper.updateByExampleSelective(customFieldTemplate, example);
    }

    public List<String> getSystemCustomField(String templateId, String fieldName) {
        return extCustomFieldTemplateMapper.getSystemCustomField(templateId, fieldName);
    }
}
